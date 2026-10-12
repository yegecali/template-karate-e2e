package runners.support;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utilidades de hashing, Base64 y cifrado RSA, expuestas a los features
 * via common/crypto.js (Java.type('runners.support.CryptoUtils')).
 *
 * Pensado para campos que la API exige hasheados (ej. passwords, checksums),
 * codificados en Base64, o cifrados con la llave publica del servicio
 * (ej. numeros de documento, tarjetas, datos sensibles).
 */
public final class CryptoUtils {

    private CryptoUtils() {
    }

    // ------------------------------------------------------------------
    // Hashing
    // ------------------------------------------------------------------

    /** Hash en hexadecimal minuscula. algorithm: "MD5", "SHA-1", "SHA-256", "SHA-512". */
    public static String hashHex(String algorithm, String value) {
        byte[] digest = digest(algorithm, value);
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /** Hash codificado en Base64, util cuando la API espera el hash asi (no hex). */
    public static String hashBase64(String algorithm, String value) {
        return Base64.getEncoder().encodeToString(digest(algorithm, value));
    }

    private static byte[] digest(String algorithm, String value) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return md.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("no se pudo calcular hash " + algorithm, e);
        }
    }

    // ------------------------------------------------------------------
    // Base64
    // ------------------------------------------------------------------

    public static String base64Encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64Decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------
    // RSA (cifrado asimetrico con llave publica / descifrado con llave privada)
    // ------------------------------------------------------------------

    /** Cifra texto plano con una llave publica RSA (PEM, formato X.509 "BEGIN PUBLIC KEY"). Devuelve Base64. */
    public static String encryptRSA(String publicKeyPem, String plainText) {
        try {
            PublicKey publicKey = readPublicKey(publicKeyPem);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("no se pudo cifrar con la llave publica RSA", e);
        }
    }

    /** Descifra (Base64) con una llave privada RSA (PEM, formato PKCS8 "BEGIN PRIVATE KEY"). Solo para pruebas locales. */
    public static String decryptRSA(String privateKeyPem, String cipherTextBase64) {
        try {
            PrivateKey privateKey = readPrivateKey(privateKeyPem);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherTextBase64));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("no se pudo descifrar con la llave privada RSA", e);
        }
    }

    private static PublicKey readPublicKey(String pem) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(stripPemHeaders(pem));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static PrivateKey readPrivateKey(String pem) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(stripPemHeaders(pem));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static String stripPemHeaders(String pem) {
        return pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
    }
}
