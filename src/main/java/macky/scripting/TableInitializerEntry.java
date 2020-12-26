package macky.scripting;

import org.derive4j.Data;

@Data
public abstract class TableInitializerEntry {
    public interface Cases<X> {
        X map(AstNode key, AstNode value);

        X list(AstNode value);
    }

    public abstract <X> X match(Cases<X> cases);
}
