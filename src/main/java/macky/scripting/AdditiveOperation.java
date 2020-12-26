package macky.scripting;

import org.derive4j.Data;

@Data
public abstract class AdditiveOperation {

    public interface Cases<X> {
        X add();
        X sub();
    }

    public abstract <X> X match(Cases<X> cases);

}
