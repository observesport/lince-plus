package com.lince.observer.desktop.spring.config;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Created by Alberto Soto. 5/11/24
 */
@Configuration
public class EncodingConfig {

    @PostConstruct
    public void setFileEncoding() {
        System.setProperty("file.encoding", "UTF-8");
    }
}
