package com.lince.injection;

import com.lince.observer.data.LinceQualifier;
//import com.lince.observer.data.qualifier.DesktopQualifier;
import com.lince.observer.data.service.example.IHelloWorldService;
import org.springframework.stereotype.Service;

/**
 * Created by Alberto Soto. 18/3/24
 */
@Service
@LinceQualifier.DesktopQualifier
public class QualifiedHelloWorldService implements IHelloWorldService {
    @Override
    public String sayHi() {
        return "oh mama!";
    }
}
