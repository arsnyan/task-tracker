package com.arsnyan.account.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "jwt")
public record RsaKeyProperties(
        RSAPrivateKey privateKey,
        RSAPublicKey publicKey,
        long expiration,
        String issuer
) {}
