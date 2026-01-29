package dev.dannytaylor.streamtimer.util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;

public class Crypt {
    private static final String algorithm = "AES";

    public static SecretKey createKey() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(algorithm);
        generator.init(128);
        return generator.generateKey();
    }

    public static String encrypt(String data, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return encode(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decrypt(String data, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(decode(data)), StandardCharsets.UTF_8);
    }

    public static String encode(byte[] data) {
        return "ENC=" + Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decode(String data) throws IllegalArgumentException {
        return Base64.getDecoder().decode(data.substring(4));
    }

    public static boolean isEncrpyted(String data) {
        return data.startsWith("ENC=");
    }

    public static void toFile(Path path, SecretKey key) throws IOException {
        byte[] keyBytes = key.getEncoded();
        try {
            Files.write(path, keyBytes);
            try {
                // Linux and MacOS
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
                Files.setPosixFilePermissions(path, perms);
            } catch (UnsupportedOperationException e) {
                // Windows
                path.toFile().setReadable(true, true);
                path.toFile().setWritable(true, true);
            }
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }

    public static SecretKey fromFile(Path path) throws IOException {
        return new SecretKeySpec(Files.readAllBytes(path), algorithm);
    }

    public static boolean fileExists(Path path) {
        return Files.exists(path);
    }
}
