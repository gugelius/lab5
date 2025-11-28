package org.arkasha.jwtspringmaven.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "jwt-authentication")
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class JwtAuthentication {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "jwt_id")
    private UUID id;
    @Column(name = "token")
    private String token;
    @Column(name = "refresh-token")
    private String refreshToken;
}
