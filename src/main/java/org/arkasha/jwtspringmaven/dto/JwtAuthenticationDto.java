package org.arkasha.jwtspringmaven.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationDto {
    private String token;
    private String refreshToken;
}
