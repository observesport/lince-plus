package com.lince.observer.data.service.example;

/**
 * Created by Alberto Soto. 18/3/24
 */
public class HelloWorldServiceForWeb implements IHelloWorldService {
    @Override
    public String sayHi() {
        return "Hi " + getClass().getName();
    }
}
