package com.lince.observer.desktop.component;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Alberto Soto. 30/3/24
 */
@Configuration
@ComponentScan(basePackages = "com.lince.observer")
public class SpringComponentScan {
    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = new AnnotationConfigApplicationContext(SpringComponentScan.class);

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            System.out.println(beanName);
        }
    }
}
