package com.skillvault.skillvault_backend.repository;

import com.skillvault.skillvault_backend.model.CreditTransaction;
import com.skillvault.skillvault_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {

    List<CreditTransaction> findByUser(User user);

}
