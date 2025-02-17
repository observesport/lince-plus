package info.injection.unittest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Created by Alberto Soto. 17/2/25
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MyServiceImplTest.TestConfig.class)
public class MyServiceImplTest {
    @Configuration
    static class TestConfig {
        @Bean
        public DataProvider dataProvider() {
            return new DefaultDataProvider();
        }

        @Bean
        public MyService myService(DataProvider dataProvider) {
            return new MyServiceImpl(dataProvider);
        }
    }

    @Autowired
    private MyService myService;

    @Test
    void shouldProcessData() {
        // Given
        String input = "test";

        // When
        String result = myService.processData(input);

        // Then
        Assertions.assertEquals("TEST", result);
    }
}
