package info.injection.unittest;

import org.springframework.stereotype.Service;

/**
 * Created by Alberto Soto. 17/2/25
 */
@Service
public class MyServiceImpl implements MyService {
    private final DataProvider dataProvider;

    public MyServiceImpl(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public String processData(String input) {
        return dataProvider.transform(input);
    }
}
