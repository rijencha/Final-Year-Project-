package com.example.photoGroupe.model.share;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shares", indexes = {
        @Index(name = "idx_share_entity", columnList = "entityType, entityId")
})
@Getter @Setter
@NoArgsConstructor
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShareableType entityType;

    @Column(nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User sharedBy;

    @Column(nullable = false)
    private String shareLink;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Share(ShareableType entityType, Long entityId, User sharedBy, String shareLink) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.sharedBy = sharedBy;
        this.shareLink = shareLink;
    }
}