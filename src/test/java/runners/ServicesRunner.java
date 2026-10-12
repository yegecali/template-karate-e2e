package runners;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeAll;
import runners.support.AuthMockSupport;

/**
 * Runners por servicio, usando tags. Al agregar un nuevo servicio
 * (carpeta services/<nombre> + tag @<nombre> en sus features),
 * solo hay que sumar un metodo aqui.
 */
class ServicesRunner {

    @BeforeAll
    static void beforeAll() {
        AuthMockSupport.start();
    }

    @Karate.Test
    Karate testUsers() {
        return Karate.run("classpath:services").tags("@users");
    }

    @Karate.Test
    Karate testPosts() {
        return Karate.run("classpath:services").tags("@posts");
    }
}
