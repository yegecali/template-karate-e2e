package runners;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeAll;
import runners.support.AuthMockSupport;

/**
 * Runners por tipo de caso, transversales a todos los servicios.
 */
class SuiteRunner {

    @BeforeAll
    static void beforeAll() {
        AuthMockSupport.start();
    }

    @Karate.Test
    Karate testSuccessCases() {
        return Karate.run("classpath:services").tags("@success");
    }

    @Karate.Test
    Karate testFailureCases() {
        return Karate.run("classpath:services").tags("@failure");
    }
}
