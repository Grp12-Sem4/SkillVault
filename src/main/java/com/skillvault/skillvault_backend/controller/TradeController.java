package com.skillvault.skillvault_backend.controller;

import com.skillvault.skillvault_backend.model.TradeSession;
import com.skillvault.skillvault_backend.service.TradeService;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    // Create a trade request
    @PostMapping
    public TradeSession createTrade(@RequestBody TradeSession tradeSession) {
        return tradeService.createTradeRequest(tradeSession);
    }

    // Accept a trade request
    @PutMapping("/{tradeId}/accept")
    public TradeSession acceptTrade(@PathVariable UUID tradeId) {
        return tradeService.acceptTrade(tradeId);
    }

    // Complete a trade session
    @PutMapping("/{tradeId}/complete")
    public TradeSession completeTrade(
            @PathVariable UUID tradeId,
            @RequestParam int rating
    ) {
        return tradeService.completeTrade(tradeId, rating);
    }
}