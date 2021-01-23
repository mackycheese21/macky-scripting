package macky.scripting;

import org.derive4j.Data;

import java.util.Optional;

@Data
public abstract class Operator {

    public interface Cases<X> {
        X basic(BasicOperator basicOperator);

        X assignment(Optional<BasicOperator> transformation);
    }

    public abstract <X> X match(Cases<X> cases);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

}
