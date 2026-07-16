package com.example.photoGroupe.repo.payout;

import com.example.photoGroupe.model.payout.PayoutAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PayoutAccountRepository extends JpaRepository<PayoutAccount, Long> {
    List<PayoutAccount> findByPhotographerIdOrderByCreatedAtAsc(Long photographerId);
}