package com.pikacards.catalog.controller;

import com.pikacards.catalog.dto.CardListResponse;
import com.pikacards.catalog.dto.CardResponse;
import com.pikacards.catalog.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;
    public CardController(CardService cardService) { this.cardService = cardService; }

    @GetMapping
    public CardListResponse getCards(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int pageSize) {
        return cardService.getCards(page, pageSize);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCards(@RequestParam String q) {
        if (q == null || q.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "La búsqueda está vacía"));
        return ResponseEntity.ok(cardService.searchCards(q));
    }

    @GetMapping("/search/advanced")
    public List<CardResponse> searchAdvanced(@RequestParam(required = false) String name,
                                             @RequestParam(required = false) String artist,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(required = false) String rarity,
                                             @RequestParam(required = false) String set) {
        return cardService.searchAdvanced(name, artist, type, rarity, set);
    }

    @GetMapping("/filter")
    public List<CardResponse> filterCards(@RequestParam(required = false) String name,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) String rarity,
                                          @RequestParam(required = false) String artist,
                                          @RequestParam(required = false) String set) {
        return cardService.filterCards(name, type, rarity, artist, set);
    }

    @GetMapping("/type/{type}") public List<CardResponse> byType(@PathVariable String type) { return cardService.getByType(type); }
    @GetMapping("/rarity/{rarity}") public List<CardResponse> byRarity(@PathVariable String rarity) { return cardService.getByRarity(rarity); }
    @GetMapping("/set/{setId}") public List<CardResponse> bySet(@PathVariable String setId) { return cardService.getBySet(setId); }
    @GetMapping("/artist/{artist}") public List<CardResponse> byArtist(@PathVariable String artist) { return cardService.getByArtist(artist); }
    @GetMapping("/types") public List<String> listTypes() { return cardService.listTypes(); }
    @GetMapping("/rarities") public List<String> listRarities() { return cardService.listRarities(); }
    @GetMapping("/sets") public List<String> listSets() { return cardService.listSets(); }

    @GetMapping("/{cardId}")
    public ResponseEntity<?> cardDetail(@PathVariable String cardId) {
        try { return ResponseEntity.ok(cardService.getCardDetail(cardId)); }
        catch (NoSuchElementException e) { return ResponseEntity.status(404).body(Map.of("error", "Card not found")); }
    }
}
