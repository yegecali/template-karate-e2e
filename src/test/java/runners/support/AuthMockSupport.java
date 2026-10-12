package runners.support;

import com.intuit.karate.core.MockServer;

/**
 * Levanta un servidor OAuth2 (client_credentials) de mentira, local, en un puerto libre,
 * usando el mock server embebido de Karate (classpath:mocks/oauth-mock.feature).
 *
 * Esto es SOLO para que esta plantilla sea ejecutable de punta a punta sin depender de
 * un IDP real. En un proyecto real, karate-config.js debe apuntar "tokenUrl" al
 * Identity Provider real (Keycloak, Okta, Azure AD, etc.) y este mock deja de usarse.
 *
 * La URL del mock queda disponible via la system property "mock.auth.url", que
 * karate-config.js lee con karate.properties['mock.auth.url'].
 */
public final class AuthMockSupport {

    private static MockServer server;

    private AuthMockSupport() {
    }

    public static synchronized void start() {
        if (server != null) {
            return;
        }
        server = MockServer
                .feature("classpath:mocks/oauth-mock.feature")
                .http(0)
                .build();
        System.setProperty("mock.auth.url", "http://localhost:" + server.getPort());
    }
}
