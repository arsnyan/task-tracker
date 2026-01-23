package com.arsnyan.account.controller;

import com.arsnyan.account.security.RsaKeyProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwksController {
    private final RsaKeyProperties rsaKeyProperties;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        var rsaKey = new RSAKey.Builder(rsaKeyProperties.publicKey())
                .keyID("account-service-key")
                .build();

        return new JWKSet(rsaKey).toJSONObject();
    }
}
