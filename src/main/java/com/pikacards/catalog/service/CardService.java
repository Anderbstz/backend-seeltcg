package com.pikacards.catalog.service;

import com.pikacards.catalog.dto.CardListResponse;
import com.pikacards.catalog.dto.CardResponse;
import com.pikacards.catalog.model.Card;
import com.pikacards.catalog.repository.CardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CardService {
    private final CardRepository cardRepository;
    public CardService(CardRepository cardRepository) { this.cardRepository = cardRepository; }

    public CardListResponse getCards(int page, int pageSize) {
        Page<Card> pageResult = cardRepository.findAll(PageRequest.of(page - 1, pageSize));
        return new CardListResponse(page, pageSize, pageResult.getTotalElements(),
                pageResult.getContent().stream().map(CardResponse::fromEntity).toList());
    }

    public List<CardResponse> searchCards(String query) {
        return cardRepository.findByNameContainingIgnoreCase(query).stream().map(CardResponse::fromEntity).toList();
    }

    public List<CardResponse> searchAdvanced(String name, String artist, String type, String rarity, String set) {
        List<Card> qs = cardRepository.findAll();
        if (name != null && !name.isEmpty()) qs = qs.stream().filter(c -> c.getName().toLowerCase().contains(name.toLowerCase())).toList();
        if (artist != null && !artist.isEmpty()) qs = qs.stream().filter(c -> c.getArtist() != null && c.getArtist().toLowerCase().contains(artist.toLowerCase())).toList();
        if (type != null && !type.isEmpty()) qs = qs.stream().filter(c -> c.getTypes() != null && c.getTypes().toLowerCase().contains(type.toLowerCase())).toList();
        if (rarity != null && !rarity.isEmpty()) qs = qs.stream().filter(c -> c.getRarity() != null && c.getRarity().toLowerCase().contains(rarity.toLowerCase())).toList();
        if (set != null && !set.isEmpty()) qs = qs.stream().filter(c -> c.getSetId() != null && c.getSetId().toLowerCase().contains(set.toLowerCase())).toList();
        return qs.stream().map(CardResponse::fromEntity).toList();
    }

    public CardResponse getCardDetail(String cardId) {
        return CardResponse.fromEntity(cardRepository.findByCardId(cardId).orElseThrow(() -> new NoSuchElementException("Card not found")));
    }

    public List<CardResponse> filterCards(String name, String type, String rarity, String artist, String set) {
        List<Card> qs = cardRepository.findAll();
        if (name != null) qs = qs.stream().filter(c -> c.getName().toLowerCase().contains(name.toLowerCase())).toList();
        if (type != null) qs = qs.stream().filter(c -> c.getTypes() != null && c.getTypes().toLowerCase().contains(type.toLowerCase())).toList();
        if (rarity != null) qs = qs.stream().filter(c -> c.getRarity() != null && c.getRarity().toLowerCase().contains(rarity.toLowerCase())).toList();
        if (artist != null) qs = qs.stream().filter(c -> c.getArtist() != null && c.getArtist().toLowerCase().contains(artist.toLowerCase())).toList();
        if (set != null) qs = qs.stream().filter(c -> c.getSetId() != null && c.getSetId().toLowerCase().contains(set.toLowerCase())).toList();
        return qs.stream().map(CardResponse::fromEntity).toList();
    }

    public List<CardResponse> getByType(String type) { return cardRepository.findByTypesContainingIgnoreCase(type).stream().map(CardResponse::fromEntity).toList(); }
    public List<CardResponse> getByRarity(String rarity) { return cardRepository.findByRarityContainingIgnoreCase(rarity).stream().map(CardResponse::fromEntity).toList(); }
    public List<CardResponse> getBySet(String setId) { return cardRepository.findBySetIdContainingIgnoreCase(setId).stream().map(CardResponse::fromEntity).toList(); }
    public List<CardResponse> getByArtist(String artist) { return cardRepository.findByArtistContainingIgnoreCase(artist).stream().map(CardResponse::fromEntity).toList(); }

    public List<String> listTypes() {
        Set<String> types = new HashSet<>();
        for (String s : cardRepository.findDistinctTypes()) {
            if (s != null) for (String t : s.split(",")) types.add(t.trim());
        }
        return types.stream().filter(t -> !t.isEmpty()).sorted().toList();
    }

    public List<String> listRarities() { return cardRepository.findDistinctRarities().stream().filter(r -> r != null && !r.isEmpty()).sorted().toList(); }
    public List<String> listSets() { return cardRepository.findDistinctSets().stream().filter(s -> s != null && !s.isEmpty()).sorted().toList(); }

    public double getCardPrice(Card card) {
        String seed = (card.getCardId() != null ? card.getCardId() : card.getName());
        int hash = 0;
        for (int i = 0; i < seed.length(); i++) hash += seed.charAt(i) * (i + 1);
        return Math.round((5 + (hash % 100) / 5.0) * 100.0) / 100.0;
    }
}
