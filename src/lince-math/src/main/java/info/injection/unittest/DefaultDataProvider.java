package info.injection.unittest;

/**
 * Created by Alberto Soto. 17/2/25
 */
public class DefaultDataProvider implements DataProvider {
    @Override
    public String transform(String input) {
        return input.toUpperCase();
    }
}
