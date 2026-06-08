package com.pikacards.catalog.repository;

import com.pikacards.catalog.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardId(String cardId);
    List<Card> findByNameContainingIgnoreCase(String name);
    List<Card> findByTypesContainingIgnoreCase(String type);
    List<Card> findByRarityContainingIgnoreCase(String rarity);
    List<Card> findBySetIdContainingIgnoreCase(String setId);
    List<Card> findByArtistContainingIgnoreCase(String artist);
    @Query("SELECT DISTINCT c.types FROM Card c WHERE c.types IS NOT NULL AND c.types <> ''")
    List<String> findDistinctTypes();
    @Query("SELECT DISTINCT c.rarity FROM Card c WHERE c.rarity IS NOT NULL AND c.rarity <> ''")
    List<String> findDistinctRarities();
    @Query("SELECT DISTINCT c.setId FROM Card c WHERE c.setId IS NOT NULL AND c.setId <> ''")
    List<String> findDistinctSets();
}
