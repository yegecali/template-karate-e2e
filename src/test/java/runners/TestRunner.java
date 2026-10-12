package runners;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeAll;
import runners.support.AuthMockSupport;

/**
 * Runner general. Ejecuta todos los features bajo services/.
 * Para filtrar por tag al vuelo (sin tocar codigo) usar:
 *   mvn test -Dkarate.env=calidad -Dkarate.options="--tags @users"
 */
class TestRunner {

    @BeforeAll
    static void beforeAll() {
        AuthMockSupport.start();
    }

    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:services");
    }
}
