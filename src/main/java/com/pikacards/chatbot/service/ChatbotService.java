package com.pikacards.chatbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pikacards.auth.model.User;
import com.pikacards.catalog.model.Card;
import com.pikacards.catalog.repository.CardRepository;
import com.pikacards.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Value("${pikacards.deepseek.api-key}") private String apiKey;
    @Value("${pikacards.deepseek.model}") private String model;
    private final CardRepository cardRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final int MAX_CHARS = 20000;

    private static final String SYSTEM_PROMPT = """
        Eres SeaTgc, el asistente oficial de la tienda PikaCards, especializada en cartas Pokémon TCG.
        
        SOLO respondés preguntas relacionadas con:
        - Cartas Pokémon TCG (nombres, tipos, rarezas, sets, artistas, precios, stock)
        - La tienda PikaCards (pedidos, inventario, compras)
        - Información general sobre Pokémon TCG (reglas, lore básico, colecciones)
        
        Si el usuario pregunta sobre temas FUERA de eso (matemáticas, código, historia, tareas,
        texto de imágenes o archivos, poesía, otros juegos, etc.), respondé exactamente:
        "Solo puedo ayudarte con temas relacionados a PikaCards y Pokémon TCG."
        
        No respondas preguntas sobre otras cosas aunque el usuario insista.
        Sé amable, conciso y respondé siempre en español.
        """;

    private static final int DAILY_LIMIT = 15;
    private final Map<Long, UserQuota> dailyQuotas = new ConcurrentHashMap<>();

    private static final Set<String> EASTER_EGGS = Set.of(
        "yash", "yashuri", "nicole", "nicol", "coni"
    );
    private static final String EASTER_RESPONSE = "Ya me contaron de ti yash ;)";

    private static final Set<String> CARD_KEYWORDS = Set.of(
        "carta", "cartas", "card", "cards", "pokemon", "pokémon", "tcg", "pikacards",
        "precio", "precios", "stock", "rareza", "rarezas", "tipo", "tipos", "set", "sets",
        "artista", "artistas", "compra", "comprar", "compré", "pedido", "historial",
        "name", "nombre", "hp", "rarity", "artist", "image", "imagen", "colección",
        "coleccion", "expansion", "expansión", "booster", "sobre"
    );

    public ChatbotService(CardRepository cardRepository, OrderRepository orderRepository) {
        this.cardRepository = cardRepository; this.orderRepository = orderRepository;
        this.restTemplate = new RestTemplate(); this.objectMapper = new ObjectMapper();
    }

    public String chat(String userMessage, boolean useFullDb, User user) {
        String lower = userMessage.toLowerCase();

        // Easter egg
        if (EASTER_EGGS.stream().anyMatch(lower::contains)) {
            return EASTER_RESPONSE;
        }

        // Rate limit
        UserQuota quota = dailyQuotas.computeIfAbsent(user.getId(), id -> new UserQuota());
        if (!quota.canProceed()) {
            throw new IllegalArgumentException("Límite diario alcanzado (15 preguntas). Volvé mañana.");
        }
        quota.increment();

        boolean mentionsCards = CARD_KEYWORDS.stream().anyMatch(lower::contains);
        String purchaseHistory = getPurchaseHistoryJson(user);

        String userContent;
        if (mentionsCards) {
            String cardsJson = getCardsJson(useFullDb);
            userContent = """
                BASE DE DATOS DE CARTAS:
                %s
                
                HISTORIAL DE COMPRAS DEL USUARIO:
                %s
                
                PREGUNTA DEL USUARIO:
                %s
                
                Respondé solo si está relacionado a PikaCards o Pokémon TCG.
                """.formatted(cardsJson, purchaseHistory, userMessage);
        } else {
            userContent = userMessage;
        }

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user", "content", userContent)
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.deepseek.com/chat/completions", entity, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "Lo siento, no pude procesar tu mensaje.";
        } catch (Exception e) {
            System.err.println("Error DeepSeek: " + e.getMessage());
            throw new RuntimeException("Error interno procesando la solicitud", e);
        }
    }

    private String getCardsJson(boolean useFullDb) {
        List<Card> allCards = cardRepository.findAll();
        try {
            String json;
            if (!useFullDb) {
                json = objectMapper.writeValueAsString(allCards.stream().map(c -> Map.of(
                        "id", c.getCardId(), "name", c.getName(), "types", c.getTypes()
                )).toList());
            } else {
                json = objectMapper.writeValueAsString(allCards.stream().map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getCardId()); m.put("name", c.getName());
                    m.put("types", c.getTypes() != null ? List.of(c.getTypes().split(",")) : List.of());
                    m.put("rarity", c.getRarity()); m.put("image", c.getImage());
                    m.put("artist", c.getArtist()); m.put("set_id", c.getSetId()); m.put("hp", c.getHp());
                    return m;
                }).toList());
            }
            if (json.length() > MAX_CHARS) {
                json = json.substring(0, MAX_CHARS) + "\n...(base de datos truncada)...";
            }
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando cartas", e);
        }
    }

    private String getPurchaseHistoryJson(User user) {
        var orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        List<String> purchased = orders.stream()
                .flatMap(o -> o.getItems().stream())
                .map(i -> i.getProductName() + " x" + i.getQuantity())
                .toList();
        return purchased.isEmpty() ? "Sin compras anteriores." : String.join("\n", purchased);
    }

    private static class UserQuota {
        private LocalDate date = LocalDate.now();
        private int count = 0;

        boolean canProceed() {
            resetIfNewDay();
            return count < DAILY_LIMIT;
        }

        void increment() {
            resetIfNewDay();
            count++;
        }

        private void resetIfNewDay() {
            if (!LocalDate.now().equals(date)) {
                date = LocalDate.now();
                count = 0;
            }
        }
    }
}
