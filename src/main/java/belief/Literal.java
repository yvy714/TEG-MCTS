package belief;

import java.util.Objects;

/**
 * Representation of propositional logic, deprecated in the grid world domain.
 */
@Deprecated
public class Literal {
    String literalName;
    boolean truthVal;

    public Literal(String literalName, boolean truthVal) {
        this.literalName = literalName;
        this.truthVal = truthVal;
    }

    /**
     * By providing only the literal name, the truth value will be default set to true
     */
    public Literal(String literalName) {
        this(literalName, true);
    }

    @Override
    public int hashCode() {
        return literalName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Literal literal = (Literal) o;
        return truthVal == literal.truthVal && Objects.equals(literalName, literal.literalName);
    }
}
