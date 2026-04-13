package com.skillvault.skillvault_backend.service;

import com.skillvault.skillvault_backend.enums.CreditTransactionType;
import com.skillvault.skillvault_backend.model.CreditTransaction;
import com.skillvault.skillvault_backend.model.TradeSession;
import com.skillvault.skillvault_backend.model.User;
import com.skillvault.skillvault_backend.repository.CreditTransactionRepository;
import com.skillvault.skillvault_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CreditService {
    private final UserRepository userRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    public CreditService(UserRepository userRepository,CreditTransactionRepository creditTransactionRepository) {
        this.userRepository = userRepository;
        this.creditTransactionRepository = creditTransactionRepository;
    }

    public int calculateCredits(TradeSession tradeSession) {
        Integer creditValue = tradeSession.getSkill().getCreditValue();
        Integer duration = tradeSession.getDuration();
        if (creditValue == null) {
            creditValue = 0;
        }
        if (duration == null || duration <= 0) {
            duration = 1;
        }
        return creditValue * duration;
    }

    public void transferCredits(TradeSession tradeSession) {
        User requester = tradeSession.getRequester();
        User provider = tradeSession.getProvider();

        int credits = calculateCredits(tradeSession);

        if (requester.getCreditBalance() == null) {
            requester.setCreditBalance(0);
        }

        if (provider.getCreditBalance() == null) {
            provider.setCreditBalance(0);
        }

        if (requester.getCreditBalance() < credits) {
            throw new RuntimeException("Insufficient credits.");
        }

        requester.setCreditBalance(requester.getCreditBalance() - credits);
        provider.setCreditBalance(provider.getCreditBalance() + credits);

        userRepository.save(requester);
        userRepository.save(provider);

        CreditTransaction debit = new CreditTransaction();
        debit.setAmount(credits);
        debit.setType(CreditTransactionType.SPENT);
        debit.setCreatedAt(LocalDateTime.now());
        debit.setUser(requester);
        debit.setTrade(tradeSession);

        CreditTransaction credit = new CreditTransaction();
        credit.setAmount(credits);
        credit.setType(CreditTransactionType.EARNED);
        credit.setCreatedAt(LocalDateTime.now());
        credit.setUser(provider);
        credit.setTrade(tradeSession);

        creditTransactionRepository.save(debit);
        creditTransactionRepository.save(credit);
    }

}
