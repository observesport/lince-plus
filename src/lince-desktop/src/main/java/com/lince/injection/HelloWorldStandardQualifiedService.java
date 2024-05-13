package com.lince.injection;

import com.lince.observer.data.service.example.IHelloWorldService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Created by Alberto Soto. 18/3/24
 */
@Service
@Qualifier("example")
public class HelloWorldStandardQualifiedService implements IHelloWorldService {
    @Override
    public String sayHi() {
        return "oh mama!";
    }
}
