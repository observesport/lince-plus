package com.deicos.lince.app.test;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;
import java.util.ResourceBundle;

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
@ExtendWith(SpringExtension.class)
@SpringBootTest()
public class BasicSpringBootAppTest {

    @Test
    public void fxLocaleTest() {
        Locale locale = Locale.getDefault();
        String lang = locale.getDisplayLanguage();
        String country = locale.getDisplayCountry();
        Assertions.assertNotNull(locale);
    }

    @Test
    public void fxLocalizedResourcesTest(){
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        Assertions.assertTrue(StringUtils.contains(bundle.getString("about"), "Lince Plus"));
    }
    @Test
    public void fxLocalizedFailResourcesTest(){
        Locale locale = Locale.FRANCE;
        ResourceBundle bundle = ResourceBundle.getBundle("messages",locale);
        Assertions.assertTrue(StringUtils.contains(bundle.getString("about"), "Lince Plus"));
    }
}
