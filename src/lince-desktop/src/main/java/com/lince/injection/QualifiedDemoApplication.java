package com.lince.injection;

import com.lince.observer.data.LinceQualifier;
//import com.lince.observer.data.qualifier.DesktopQualifier;
import com.lince.observer.data.service.example.IHelloWorldService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ServiceLoader;

/**
 * Created by Alberto Soto. 29/3/24
 */
@SpringBootApplication
public class QualifiedDemoApplication {
    final
    IHelloWorldService helloWorldService;

    public QualifiedDemoApplication(
            @LinceQualifier.DesktopQualifier
            IHelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    public static void main(String[] args) {
        SpringApplication.run(QualifiedDemoApplication.class, args);
    }

    @Bean
    public IHelloWorldService getHelloWorldService() {
        ServiceLoader<IHelloWorldService> services = ServiceLoader.load(IHelloWorldService.class);
        return services.findFirst().orElse(null);
    }
}
