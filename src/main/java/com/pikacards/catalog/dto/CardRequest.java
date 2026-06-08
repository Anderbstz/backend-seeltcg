package com.pikacards.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CardRequest {

    @NotBlank
    private String cardId;

    @NotBlank
    private String name;

    private String supertype;
    private String subtypes;
    private String hp;
    private String types;
    private String rarity;
    private String artist;
    private String setId;
    private String image;

    @NotNull
    private Integer stock;

    public @NotBlank String getCardId() { return cardId; }
    public void setCardId(@NotBlank String cardId) { this.cardId = cardId; }
    public @NotBlank String getName() { return name; }
    public void setName(@NotBlank String name) { this.name = name; }
    public String getSupertype() { return supertype; }
    public void setSupertype(String supertype) { this.supertype = supertype; }
    public String getSubtypes() { return subtypes; }
    public void setSubtypes(String subtypes) { this.subtypes = subtypes; }
    public String getHp() { return hp; }
    public void setHp(String hp) { this.hp = hp; }
    public String getTypes() { return types; }
    public void setTypes(String types) { this.types = types; }
    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public @NotNull Integer getStock() { return stock; }
    public void setStock(@NotNull Integer stock) { this.stock = stock; }
}
