package com.deicos.lince.app.test;

import com.deicos.lince.app.LinceApp;
import com.deicos.lince.math.service.DataHubService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * com.deicos.lince.app.test
 * Class BasicSpringBootAppTest
 * 05/11/2019
 *
 * Fonts
 *      https://www.baeldung.com/spring-boot-testing
 *      https://reflectoring.io/spring-boot-test/ç
 * @author berto (alberto.soto@gmail.com)
 *
 * breaks on install
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = LinceApp.class)
//@AutoConfigureMockMvc
public class BasicSpringBootAppTest {
    //@Autowired
    private DataHubService dataHubService;

    //@Test
    public void serviceWorks() {
        String name = "pepito";
        dataHubService.clearData();
        Assert.assertNotNull(name);
    }
}
