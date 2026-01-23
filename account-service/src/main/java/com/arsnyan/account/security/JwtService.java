package com.arsnyan.account.security;

import com.arsnyan.account.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final RsaKeyProperties rsaKeyProperties;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;

    public String generateToken(User user) {
        var now = Instant.now(clock);
        var expiresAt = now.plusMillis(rsaKeyProperties.expiration());

        var claims = JwtClaimsSet.builder()
                .issuer(rsaKeyProperties.issuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("userId", user.getUserId())
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
