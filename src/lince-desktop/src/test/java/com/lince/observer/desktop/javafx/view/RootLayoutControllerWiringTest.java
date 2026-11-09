package com.lince.observer.desktop.javafx.view;

import com.lince.observer.data.service.ExportService;
import com.lince.observer.data.service.ImportService;
import com.lince.observer.desktop.javafx.JavaFXLoader;
import com.lince.observer.desktop.spring.service.CurrentProjectProvider;
import com.lince.observer.math.service.DataHubService;
import com.lince.observer.desktop.spring.service.ExternalLinkService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 * The root layout controller is instantiated by {@code FXMLLoader}, not by Spring, so its {@code @Autowired}
 * fields stay null unless {@link JavaFXLoader} autowires the controller through the application context (LO-349:
 * every export and import menu entry failed with an NPE on {@code currentProjectProvider}).
 */
class RootLayoutControllerWiringTest {

    @Configuration
    static class Beans {
        @Bean ImportService importService() { return mock(ImportService.class); }
        @Bean ExportService exportService() { return mock(ExportService.class); }
        @Bean CurrentProjectProvider currentProjectProvider() { return mock(CurrentProjectProvider.class); }
        @Bean DataHubService dataHubService() { return mock(DataHubService.class); }
        @Bean ExternalLinkService externalLinkService() { return mock(ExternalLinkService.class); }
    }

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Beans.class);

    @AfterEach
    void close() {
        context.close();
    }

    @Test
    void fxmlControllerGetsItsSpringBeansInjected() {
        RootLayoutController controller = new RootLayoutController();

        JavaFXLoader.injectSpringBeans(controller, context);

        assertSame(context.getBean(ImportService.class), controller.importService);
        assertSame(context.getBean(ExportService.class), controller.exportService);
        assertSame(context.getBean(CurrentProjectProvider.class), controller.currentProjectProvider);
        assertSame(context.getBean(DataHubService.class), controller.dataHubService);
        assertSame(context.getBean(ExternalLinkService.class), controller.externalLinkService);
    }
}
