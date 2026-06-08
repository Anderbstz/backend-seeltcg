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
import java.util.*;

@Service
public class ChatbotService {

    @Value("${pikacards.deepseek.api-key}") private String apiKey;
    @Value("${pikacards.deepseek.model}") private String model;
    private final CardRepository cardRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final int MAX_CHARS = 20000;

    public ChatbotService(CardRepository cardRepository, OrderRepository orderRepository) {
        this.cardRepository = cardRepository; this.orderRepository = orderRepository;
        this.restTemplate = new RestTemplate(); this.objectMapper = new ObjectMapper();
    }

    public String chat(String userMessage, boolean useFullDb, User user) {
        var orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        List<String> purchased = orders.stream().flatMap(o -> o.getItems().stream()).map(i -> i.getProductName()).toList();
        List<Card> allCards = cardRepository.findAll();
        String cardsJson;

        try {
            if (!useFullDb) {
                cardsJson = objectMapper.writeValueAsString(allCards.stream().map(c -> Map.of("id", c.getCardId(), "name", c.getName(), "types", c.getTypes())).toList());
            } else {
                cardsJson = objectMapper.writeValueAsString(allCards.stream().map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getCardId()); m.put("name", c.getName());
                    m.put("types", c.getTypes() != null ? List.of(c.getTypes().split(",")) : List.of());
                    m.put("rarity", c.getRarity()); m.put("image", c.getImage());
                    m.put("artist", c.getArtist()); m.put("set_id", c.getSetId()); m.put("hp", c.getHp());
                    return m;
                }).toList());
            }
        } catch (JsonProcessingException e) { throw new RuntimeException("Error serializando cartas", e); }

        String truncatedNotice = "";
        if (cardsJson.length() > MAX_CHARS) { cardsJson = cardsJson.substring(0, MAX_CHARS); truncatedNotice = "\n...(base de datos truncada)..."; }

        String prompt = "Eres SeaTgc, asistente oficial de la tienda PikaCards.\nEres amable, conciso y experto en cartas Pokémon TCG.\n\nBASE DE DATOS DE CARTAS:\n%s%s\n\nHISTORIAL DE COMPRAS:\n%s\n\nMENSAJE: %s\n\nResponde en español."
                .formatted(cardsJson, truncatedNotice, purchased, userMessage);

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "Eres SeaTgc, asistente de PikaCards."),
                    Map.of("role", "user", "content", prompt)
            ));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity("https://api.deepseek.com/chat/completions", entity, Map.class);
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
}
