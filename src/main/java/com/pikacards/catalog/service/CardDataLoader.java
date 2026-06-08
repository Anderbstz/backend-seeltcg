package com.pikacards.catalog.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pikacards.catalog.model.Card;
import com.pikacards.catalog.repository.CardRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@Component
public class CardDataLoader implements CommandLineRunner {

    private final CardRepository cardRepository;
    private final ObjectMapper objectMapper;

    public CardDataLoader(CardRepository cardRepository, ObjectMapper objectMapper) {
        this.cardRepository = cardRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        if (cardRepository.count() > 0) {
            System.out.println("📦 Cartas ya cargadas: " + cardRepository.count() + " — saltando importación");
            return;
        }

        System.out.println("📦 Importando cartas desde cards_500.json...");

        // Try multiple locations
        JsonNode root = null;

        // 1. Try from data/ directory next to the JAR/project root
        File dataFile = new File("data/cards_500.json");
        if (dataFile.exists()) {
            root = objectMapper.readTree(dataFile);
            System.out.println("   → Encontrado en: data/cards_500.json");
        }

        // 2. Try from classpath:/data/cards_500.json
        if (root == null) {
            try {
                ClassPathResource resource = new ClassPathResource("data/cards_500.json");
                if (resource.exists()) {
                    try (InputStream is = resource.getInputStream()) {
                        root = objectMapper.readTree(is);
                        System.out.println("   → Encontrado en: classpath:data/cards_500.json");
                    }
                }
            } catch (Exception ignored) {}
        }

        // 3. Try from ../data/cards_500.json (when running from backend/ subdir)
        if (root == null) {
            File parentDataFile = new File("../data/cards_500.json");
            if (parentDataFile.exists()) {
                root = objectMapper.readTree(parentDataFile);
                System.out.println("   → Encontrado en: ../data/cards_500.json");
            }
        }

        // 4. Try absolute path
        if (root == null) {
            File absPath = new File("C:\\PikaCards\\Backend-Seeltcg\\data\\cards_500.json");
            if (absPath.exists()) {
                root = objectMapper.readTree(absPath);
                System.out.println("   → Encontrado en ruta absoluta");
            }
        }

        if (root == null) {
            System.out.println("⚠️  No se encontró cards_500.json en ninguna ubicación. Saltando importación.");
            return;
        }

        // Normalize: could be { "cards": [...] } or just [...]
        JsonNode cardsArray;
        if (root.has("cards") && root.get("cards").isArray()) {
            cardsArray = root.get("cards");
        } else if (root.isArray()) {
            cardsArray = root;
        } else if (root.has("data") && root.get("data").isArray()) {
            cardsArray = root.get("data");
        } else {
            System.out.println("⚠️  Formato JSON no reconocido. Saltando.");
            return;
        }

        int total = 0;
        if (cardsArray != null) {
            for (JsonNode item : cardsArray) {
                String cardId = getText(item, "id");
                if (cardId == null || cardId.isEmpty()) continue;

                String name = getText(item, "name");
                String supertype = getText(item, "supertype");

                // subtypes: "subtypes" (array) or "subtype" (string)
                String subtypes = "";
                JsonNode subtypesNode = item.get("subtypes");
                if (subtypesNode != null && subtypesNode.isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode n : subtypesNode) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(n.asText());
                    }
                    subtypes = sb.toString();
                } else {
                    subtypes = getText(item, "subtype");
                }

                String hp = getText(item, "hp");

                // types: array
                String types = "";
                JsonNode typesNode = item.get("types");
                if (typesNode != null && typesNode.isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode n : typesNode) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(n.asText());
                    }
                    types = sb.toString();
                }

                String rarity = getText(item, "rarity");
                String artist = getText(item, "artist");

                // setCode directly (most datasets use setCode, not set.id)
                String setId = getText(item, "setCode");

                // image: imageUrl or images.small
                String image = getText(item, "imageUrl");
                if (image == null || image.isEmpty()) {
                    JsonNode images = item.get("images");
                    if (images != null && images.has("small")) {
                        image = images.get("small").asText();
                    } else {
                        image = getText(item, "image");
                    }
                }

                Card card = new Card();
                card.setCardId(cardId);
                card.setName(name != null ? name : "");
                card.setSupertype(supertype);
                card.setSubtypes(subtypes);
                card.setHp(hp);
                card.setTypes(types);
                card.setRarity(rarity);
                card.setArtist(artist);
                card.setSetId(setId);
                card.setImage(image != null ? image : "");
                card.setStock(50);

                cardRepository.save(card);
                total++;
            }
        }

        System.out.println("✅ Importación completa: " + total + " cartas cargadas");
    }

    private String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null ? value.asText() : "";
    }
}
