package com.bill.bill_chess.persistence;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Email
    @Indexed(unique = true)
    private String email;
    @NotBlank
    private String name;
    @Size(min = 6)
    private String password; // BCrypt
    @Builder.Default
    private Set<Role> roles = EnumSet.noneOf(Role.class); // ADMIN, PLAYER
    @DBRef
    @Builder.Default
    private Set<ChessEntity> chessEntities = new HashSet<>();
    @Builder.Default
    private boolean enabled = true;
    @Builder.Default
    @CreatedDate
    private Instant createdAt = Instant.now();
    @Builder.Default
    @LastModifiedDate
    private Instant lastModifiedAt = Instant.now();

    public enum Role {
        ADMIN, PLAYER
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public void addChessEntity(ChessEntity chessEntity) {
        this.chessEntities.add(chessEntity);
    }

    public void removeChessEntity(ChessEntity chessEntity) {
        this.chessEntities.remove(chessEntity);
    }
}
