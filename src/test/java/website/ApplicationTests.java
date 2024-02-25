package website;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {
    @Test
    public void test() {
        System.out.println(System.getProperty("java.library.path"));
    }
}
