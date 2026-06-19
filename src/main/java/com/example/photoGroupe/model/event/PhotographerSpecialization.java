package com.example.photoGroupe.model.event;

import com.example.photoGroupe.model.Category;
import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "photographer_specializations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"photographer_id", "custom_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotographerSpecialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    private User photographer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "event_type")
//    private EventType eventType;

    @Column(name = "custom_type")
    private String customType;

    private String note;
}