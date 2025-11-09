package com.rafaelcabanillas.sweeties.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
