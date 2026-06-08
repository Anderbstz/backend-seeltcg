package com.pikacards.catalog.controller;

import com.pikacards.catalog.dto.CardListResponse;
import com.pikacards.catalog.dto.CardRequest;
import com.pikacards.catalog.dto.CardResponse;
import com.pikacards.catalog.model.Card;
import com.pikacards.catalog.repository.CardRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {

    private final CardRepository cardRepository;

    public AdminCardController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @GetMapping
    public CardListResponse listCards(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "20") int pageSize) {
        Page<Card> pageResult = cardRepository.findAll(PageRequest.of(page - 1, pageSize));
        return new CardListResponse(page, pageSize, pageResult.getTotalElements(),
                pageResult.getContent().stream().map(CardResponse::fromEntity).toList());
    }

    @PostMapping
    public ResponseEntity<?> createCard(@Valid @RequestBody CardRequest request) {
        if (cardRepository.findByCardId(request.getCardId()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ya existe una carta con ese cardId"));
        }

        Card card = new Card();
        card.setCardId(request.getCardId());
        card.setName(request.getName());
        card.setSupertype(request.getSupertype());
        card.setSubtypes(request.getSubtypes());
        card.setHp(request.getHp());
        card.setTypes(request.getTypes());
        card.setRarity(request.getRarity());
        card.setArtist(request.getArtist());
        card.setSetId(request.getSetId());
        card.setImage(request.getImage());
        card.setStock(request.getStock() != null ? request.getStock() : 50);

        Card saved = cardRepository.save(card);
        return ResponseEntity.status(HttpStatus.CREATED).body(CardResponse.fromEntity(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCard(@PathVariable Long id, @Valid @RequestBody CardRequest request) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Carta no encontrada"));

        if (!card.getCardId().equals(request.getCardId()) &&
                cardRepository.findByCardId(request.getCardId()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ya existe una carta con ese cardId"));
        }

        card.setCardId(request.getCardId());
        card.setName(request.getName());
        card.setSupertype(request.getSupertype());
        card.setSubtypes(request.getSubtypes());
        card.setHp(request.getHp());
        card.setTypes(request.getTypes());
        card.setRarity(request.getRarity());
        card.setArtist(request.getArtist());
        card.setSetId(request.getSetId());
        card.setImage(request.getImage());
        if (request.getStock() != null) card.setStock(request.getStock());

        Card saved = cardRepository.save(card);
        return ResponseEntity.ok(CardResponse.fromEntity(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        if (!cardRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("error", "Carta no encontrada"));
        }
        cardRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Carta eliminada correctamente"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
}
