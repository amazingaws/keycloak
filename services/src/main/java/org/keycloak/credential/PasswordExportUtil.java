/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.credential;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.jboss.logging.Logger;

/**
 * AES-256-GCM encryption utility for password export.
 * Passwords are encrypted with a configurable secret key before being stored.
 *
 * Configure via environment variable: KC_PASSWORD_EXPORT_SECRET_KEY
 */
public class PasswordExportUtil {

    private static final Logger logger = Logger.getLogger(PasswordExportUtil.class);
    private static final String ENV_SECRET_KEY = "KC_PASSWORD_EXPORT_SECRET_KEY";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private PasswordExportUtil() {
    }

    /**
     * Returns true if password export is enabled (secret key is configured).
     */
    public static boolean isEnabled() {
        String key = System.getenv(ENV_SECRET_KEY);
        return key != null && !key.isEmpty();
    }

    /**
     * Encrypt a password using AES-256-GCM.
     * Returns base64-encoded string containing IV + ciphertext.
     */
    public static String encrypt(String password) {
        try {
            SecretKey secretKey = deriveKey();
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            logger.error("Failed to encrypt password for export", e);
            return null;
        }
    }

    /**
     * Decrypt a password that was encrypted with encrypt().
     */
    public static String decrypt(String encryptedBase64) {
        try {
            SecretKey secretKey = deriveKey();
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Failed to decrypt password for export", e);
            return null;
        }
    }

    private static SecretKey deriveKey() throws Exception {
        String secret = System.getenv(ENV_SECRET_KEY);
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("Environment variable " + ENV_SECRET_KEY + " is not set");
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }
}
