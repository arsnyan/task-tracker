package com.arsnyan.account.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record RsaKeyProperties(
        RSAPrivateKey privateKey,
        RSAPublicKey publicKey,
        Duration expiration,
        String issuer
) {}
