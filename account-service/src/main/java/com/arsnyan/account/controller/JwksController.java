package com.arsnyan.account.controller;

import com.arsnyan.account.security.RsaKeyProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "JWKS", description = "JSON Web Key Set endpoint for JWT validation")
public class JwksController {
    private final RsaKeyProperties rsaKeyProperties;

    @Operation(summary = "Get JWKS", description = "Returns the JSON Web Key Set containing public keys for JWT validation")
    @ApiResponse(responseCode = "200", description = "JWKS retrieved successfully")
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        var rsaKey = new RSAKey.Builder(rsaKeyProperties.publicKey())
                .keyID("account-service-key")
                .build();

        return new JWKSet(rsaKey).toJSONObject();
    }
}
