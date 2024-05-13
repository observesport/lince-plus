package com.lince.observer.data;

import com.lince.observer.data.service.example.HelloWorldServiceForDesktop;
import com.lince.observer.data.service.example.IHelloWorldService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Alberto Soto. 18/3/24
 */
public class SPIServiceTest {

    @Test
    public void testHelloWorldServiceLoadForDesktop(){
        //given
        AtomicReference<String> txt = new AtomicReference<>(StringUtils.EMPTY);
        String classTarget = HelloWorldServiceForDesktop.class.getName();
        //when
        ServiceLoader<IHelloWorldService> services = ServiceLoader.load(IHelloWorldService.class);
        services.findFirst().ifPresent(t-> txt.set(t.sayHi()));
        //then
        Assertions.assertTrue(StringUtils.contains(String.valueOf(txt), classTarget));
    }
}
