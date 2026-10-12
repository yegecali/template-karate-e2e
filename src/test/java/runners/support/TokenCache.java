package runners.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache de tokens en memoria, compartida por todo el run de Maven/JUnit.
 * Evita pedir un token nuevo (client_credentials, password, etc.) en cada escenario:
 * se pide una sola vez por servicio y se reutiliza mientras dure la ejecucion.
 *
 * Se accede desde common/headers.js via: Java.type('runners.support.TokenCache')
 */
public final class TokenCache {

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private TokenCache() {
    }

    public static String get(String key) {
        return CACHE.get(key);
    }

    public static synchronized void put(String key, String token) {
        CACHE.put(key, token);
    }
}
