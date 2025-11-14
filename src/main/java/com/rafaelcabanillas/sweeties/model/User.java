package com.rafaelcabanillas.sweeties.model;

import com.rafaelcabanillas.sweeties.model.Theme;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Data // Generates getters/setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank private String name;
    @NotBlank private String username;
    @NotBlank private String email;
    @NotBlank private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private Role role = Role.GUEST;

    private String bio;
    private String imageUrl;
    private String imagePublicId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum Role { ADMIN, EDITOR, VIEWER, GUEST }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "theme_id")
    private Theme theme;
}
