package com.skillvault.skillvault_backend.repository;

import com.skillvault.skillvault_backend.model.TimeCapsuleSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeCapsuleSnapshotRepository extends JpaRepository<TimeCapsuleSnapshot, Long> {

    List<TimeCapsuleSnapshot> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    Optional<TimeCapsuleSnapshot> findByIdAndUser_Id(Long snapshotId, UUID userId);
}
