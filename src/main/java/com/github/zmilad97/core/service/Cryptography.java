package com.github.zmilad97.core.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Cryptography {
      private final MessageDigest md;

      public Cryptography() {
        try {
          md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
          throw new IllegalStateException(e);
        }
      }

      public byte[] getSha(String input) {
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
      }

    public String toHexString(byte[] hash) {

        BigInteger number = new BigInteger(1, hash);

        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32)
            hexString.insert(0, '0');

        return hexString.toString();
    }
}
