package macky.scripting;

import org.derive4j.Data;

@Data
public abstract class MultiplicativeOperation {

    public interface Cases<X> {
        X mul();
        X div();
    }

    public abstract <X> X match(Cases<X> cases);

}
