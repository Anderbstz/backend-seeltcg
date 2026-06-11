package com.pikacards.catalog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pikacards.catalog.model.Card;
import com.pikacards.catalog.repository.CardRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        System.out.println("📦 Importando cartas...");

        JsonNode root = null;
        String[] filenames = {"big_cards_5000.json", "cards_500.json"};
        String[] locations = {"data/", "../data/",
            "C:\\PikaCards\\Backend-Seeltcg\\data\\"};

        for (String fname : filenames) {
            if (root != null) break;
            for (String loc : locations) {
                File f = new File(loc + fname);
                if (f.exists()) { root = objectMapper.readTree(f); break; }
            }
            if (root == null) {
                try {
                    ClassPathResource r = new ClassPathResource("data/" + fname);
                    if (r.exists()) {
                        try (InputStream is = r.getInputStream()) { root = objectMapper.readTree(is); }
                    }
                } catch (Exception ignored) {}
            }
        }

        if (root == null) { System.out.println("⚠️  No se encontró dataset. Saltando."); return; }

        JsonNode cardsArray = root.has("cards") && root.get("cards").isArray() ? root.get("cards")
                : root.isArray() ? root
                : root.has("data") && root.get("data").isArray() ? root.get("data") : null;

        if (cardsArray == null) { System.out.println("⚠️  Formato JSON no reconocido."); return; }

        Set<String> seenIds = new HashSet<>();
        List<Card> batch = new ArrayList<>();
        int skipped = 0;

        for (JsonNode item : cardsArray) {
            String cardId = getText(item, "id");
            if (cardId == null || cardId.isEmpty() || !seenIds.add(cardId)) {
                if (cardId != null && !cardId.isEmpty()) skipped++;
                continue;
            }

            String types = "";
            JsonNode tn = item.get("types");
            if (tn != null && tn.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode n : tn) { if (sb.length() > 0) sb.append(","); sb.append(n.asText()); }
                types = sb.toString();
            }

            String subtypes = "";
            JsonNode sn = item.get("subtypes");
            if (sn != null && sn.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode n : sn) { if (sb.length() > 0) sb.append(","); sb.append(n.asText()); }
                subtypes = sb.toString();
            } else {
                subtypes = getText(item, "subtype");
            }

            String image = getText(item, "imageUrl");
            if (image.isEmpty()) {
                JsonNode imgs = item.get("images");
                if (imgs != null && imgs.has("small")) image = imgs.get("small").asText();
                else image = getText(item, "image");
            }

            Card card = new Card();
            card.setCardId(cardId);
            card.setName(getText(item, "name"));
            card.setSupertype(getText(item, "supertype"));
            card.setSubtypes(subtypes);
            card.setHp(getText(item, "hp"));
            card.setTypes(types);
            card.setRarity(getText(item, "rarity"));
            card.setArtist(getText(item, "artist"));
            card.setSetId(getText(item, "setCode"));
            card.setImage(image);
            card.setStock(50);
            batch.add(card);
        }

        // Batch insert de a 500
        int total = 0;
        for (int i = 0; i < batch.size(); i += 500) {
            int end = Math.min(i + 500, batch.size());
            cardRepository.saveAll(batch.subList(i, end));
            total += end - i;
            System.out.println("   → " + total + " cartas insertadas...");
        }

        System.out.println("✅ Importación completa: " + total + " cartas cargadas"
                + (skipped > 0 ? " (" + skipped + " duplicados omitidos)" : ""));
    }

    private String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null ? value.asText() : "";
    }
}
