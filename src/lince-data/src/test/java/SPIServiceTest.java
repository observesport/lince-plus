import com.lince.observer.data.service.example.IHelloWorldService;

import java.util.ServiceLoader;

/**
 * Created by Alberto Soto. 18/3/24
 */
public class SPIServiceTest {
    public static void main(String[] args) {
        ServiceLoader<IHelloWorldService> services = ServiceLoader.load(IHelloWorldService.class);
        services.forEach(t->{
            System.out.println(t.sayHi());
        });
    }
}
