package com.skillvault.skillvault_backend.repository;

import com.skillvault.skillvault_backend.model.Notification;
import com.skillvault.skillvault_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUser(User user);

}
