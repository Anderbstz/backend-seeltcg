package com.pikacards.catalog.dto;

import com.pikacards.catalog.model.Card;
import java.util.List;

public class CardResponse {
    private String id; private String name; private String supertype;
    private List<String> subtypes; private String hp; private List<String> types;
    private String rarity; private String artist; private String setId; private String image;

    public static CardResponse fromEntity(Card card) {
        CardResponse r = new CardResponse();
        r.id = card.getCardId(); r.name = card.getName(); r.supertype = card.getSupertype();
        r.subtypes = card.getSubtypes() != null && !card.getSubtypes().isEmpty() ? List.of(card.getSubtypes().split(",")) : List.of();
        r.hp = card.getHp();
        r.types = card.getTypes() != null && !card.getTypes().isEmpty() ? List.of(card.getTypes().split(",")) : List.of();
        r.rarity = card.getRarity(); r.artist = card.getArtist(); r.setId = card.getSetId(); r.image = card.getImage();
        return r;
    }

    public String getId() { return id; } public String getName() { return name; }
    public String getSupertype() { return supertype; } public List<String> getSubtypes() { return subtypes; }
    public String getHp() { return hp; } public List<String> getTypes() { return types; }
    public String getRarity() { return rarity; } public String getArtist() { return artist; }
    public String getSetId() { return setId; } public String getImage() { return image; }
}
