package com.skillvault.skillvault_backend.controller;

import com.skillvault.skillvault_backend.dto.CompleteTradeRequest;
import com.skillvault.skillvault_backend.dto.CreateTradeRequest;
import com.skillvault.skillvault_backend.dto.TradeResponse;
import com.skillvault.skillvault_backend.model.User;
import com.skillvault.skillvault_backend.repository.UserRepository;
import com.skillvault.skillvault_backend.service.TradeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
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

    @GetMapping
    public List<TradeResponse> getTrades(Principal principal) {
        return tradeService.getTradesForUser(getAuthenticatedUser(principal));
    }

    @PostMapping
    public TradeResponse createTrade(@RequestBody CreateTradeRequest request, Principal principal) {
        return tradeService.createTradeRequest(getAuthenticatedUser(principal), request);
    }

    @PutMapping("/{tradeId}/accept")
    public TradeResponse acceptTrade(@PathVariable UUID tradeId, Principal principal) {
        return tradeService.acceptTrade(tradeId, getAuthenticatedUser(principal));
    }

    @PutMapping("/{tradeId}/complete")
    public TradeResponse completeTrade(
            @PathVariable UUID tradeId,
            @RequestBody CompleteTradeRequest request,
            Principal principal
    ) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completion payload is required");
        }

        return tradeService.completeTrade(tradeId, getAuthenticatedUser(principal), request.rating());
    }

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
