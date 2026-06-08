package com.pikacards.catalog.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cards")
public class Card {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", unique = true, nullable = false, length = 50)
    private String cardId;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 50) private String supertype;
    @Column(length = 200) private String subtypes;
    @Column(length = 10) private String hp;
    @Column(length = 200) private String types;
    @Column(length = 50) private String rarity;
    @Column(length = 100) private String artist;
    @Column(name = "set_id", length = 50) private String setId;
    @Column(length = 500) private String image;
    @Column(nullable = false) private Integer stock = 50;

    // Getters & Setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getCardId() { return cardId; } public void setCardId(String cardId) { this.cardId = cardId; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getSupertype() { return supertype; } public void setSupertype(String s) { supertype = s; }
    public String getSubtypes() { return subtypes; } public void setSubtypes(String s) { subtypes = s; }
    public String getHp() { return hp; } public void setHp(String hp) { this.hp = hp; }
    public String getTypes() { return types; } public void setTypes(String types) { this.types = types; }
    public String getRarity() { return rarity; } public void setRarity(String rarity) { this.rarity = rarity; }
    public String getArtist() { return artist; } public void setArtist(String a) { artist = a; }
    public String getSetId() { return setId; } public void setSetId(String s) { setId = s; }
    public String getImage() { return image; } public void setImage(String i) { image = i; }
    public Integer getStock() { return stock; } public void setStock(Integer s) { stock = s; }
}
