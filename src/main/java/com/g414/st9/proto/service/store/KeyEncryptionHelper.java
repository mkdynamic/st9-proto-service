package com.g414.st9.proto.service.store;

import java.nio.ByteBuffer;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Hex;

public class KeyEncryptionHelper {
    private static final String keyString = System.getProperty(
            "key.encrypt.password", "changeme");
    private static final byte[] saltBytes = System.getProperty(
            "key.encrypt.salt", "asalt").getBytes();
    private static final byte[] ivBytes;

    static {
        try {
            ivBytes = Hex.decodeHex(System.getProperty("key.encrypt.iv",
                    "0123456789ABCDEF").toCharArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final AlgorithmParameterSpec paramSpec = new IvParameterSpec(
            ivBytes);
    private static ConcurrentHashMap<String, SecretKey> keyCache = new ConcurrentHashMap<String, SecretKey>();

    public static String encrypt(String type, Long id) throws Exception {
        StringBuilder encryptedIdentifier = new StringBuilder();
        encryptedIdentifier.append("@");
        encryptedIdentifier.append(type);
        encryptedIdentifier.append(":");

        byte[] plain = ByteBuffer.allocate(8).putLong(id).array();
        byte[] encrypted = getCipher(type, Cipher.ENCRYPT_MODE).doFinal(plain);

        encryptedIdentifier.append(Hex.encodeHexString(encrypted));

        return encryptedIdentifier.toString();
    }

    public static Key decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.length() == 0
                || !encryptedText.contains(":")) {
            throw new WebApplicationException(Response
                    .status(Status.BAD_REQUEST).entity("Invalid key").build());
        }

        if (!encryptedText.startsWith("@")) {
            return Key.valueOf(encryptedText);
        }

        String[] parts = encryptedText.substring(1).split(":");

        if (parts.length != 2 || parts[1].length() != 16) {
            throw new WebApplicationException(Response
                    .status(Status.BAD_REQUEST).entity("Invalid key").build());
        }

        String type = parts[0];

        byte[] encrypted = Hex.decodeHex(parts[1].toCharArray());
        byte[] decrypted = getCipher(type, Cipher.DECRYPT_MODE).doFinal(
                encrypted);

        Long id = ByteBuffer.allocate(8).put(decrypted).getLong(0);

        return new Key(type, id);
    }

    private static Cipher getCipher(String type, int mode) throws Exception {
        Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        cipher.init(mode, getKey(type), paramSpec);

        return cipher;
    }

    private static SecretKey getKey(String type) throws Exception {
        if (!keyCache.containsKey(type)) {
            SecretKeyFactory factory = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1");
            String password = keyString + ":" + type;
            KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes,
                    1024, 192);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey key = new SecretKeySpec(tmp.getEncoded(), "DESede");

            keyCache.put(type, key);
        }

        return keyCache.get(type);
    }
}
