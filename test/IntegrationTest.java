import messages.RedisActorProtocol;
import org.junit.*;

import play.Application;

import java.util.HashMap;
import java.util.Map;

import static play.test.Helpers.*;
import static org.junit.Assert.*;

public class IntegrationTest {

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void test() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, browser -> {
            browser.goTo("http://localhost:3333");
            assertTrue(browser.pageSource().contains("Your new application is ready."));
        });
    }

    @Test
    public void testRedisController() {
        /**
         * Make sure these configuration values match your local setup.
         * In particular, you should have Redis running locally on port 6379.
         */
        Map<String, Object> testConfig = new HashMap<>();
        testConfig.put("redis.host", "localhost");
        testConfig.put("redis.channel", "samplechannel");
        testConfig.put("redis.port", 6379);
        testConfig.put("redis.database", 0);
        testConfig.put("redis.timeout", 2000);

        Application fakeApp = fakeApplication(testConfig);

        running(testServer(3333, fakeApp), HTMLUNIT, browser -> {
            browser.goTo("http://localhost:3333/display");
            assertTrue(browser.pageSource().contains("Messages seen so far: "));

            browser.goTo("http://localhost:3333/publish?message=hello");
            assertTrue(browser.pageSource().contains(RedisActorProtocol.PublishAcknowledged.class.getCanonicalName()));

            browser.goTo("http://localhost:3333/display");
            assertTrue(browser.pageSource().contains("Messages seen so far: hello"));
        });
    }

}
