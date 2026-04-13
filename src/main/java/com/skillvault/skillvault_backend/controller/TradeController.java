package com.skillvault.skillvault_backend.controller;

import com.skillvault.skillvault_backend.model.TradeSession;
import com.skillvault.skillvault_backend.model.User;
import com.skillvault.skillvault_backend.repository.UserRepository;
import com.skillvault.skillvault_backend.service.TradeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;
    private final UserRepository userRepository;

    public TradeController(TradeService tradeService, UserRepository userRepository) {
        this.tradeService = tradeService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public TradeSession createTrade(@RequestBody TradeSession tradeSession, Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        User authenticatedUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        tradeSession.setRequester(authenticatedUser);
        return tradeService.createTradeRequest(tradeSession);
    }

    @PutMapping("/{tradeId}/accept")
    public TradeSession acceptTrade(@PathVariable UUID tradeId) {
        return tradeService.acceptTrade(tradeId);
    }

    @PutMapping("/{tradeId}/complete")
    public TradeSession completeTrade(@PathVariable UUID tradeId, @RequestParam int rating) {
        return tradeService.completeTrade(tradeId, rating);
    }
}
