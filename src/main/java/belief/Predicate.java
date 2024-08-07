package belief;


import java.util.Arrays;
import java.util.Objects;

public class Predicate implements Cloneable {
    final String predicateName;
    final int[] variables;
    final boolean truthVal;

    public Predicate(String predicateName, int[] variables, boolean truthVal) {
        this.predicateName = predicateName;
        if (variables == null) {
            this.variables = new int[0];
        } else {
            this.variables = variables;
        }
        this.truthVal = truthVal;
    }

    public Predicate(String predicateName, int[] variables) {
        this(predicateName, variables, true);
    }

    public Predicate(String predicateName) {
        this(predicateName, null, true);
    }

    public Predicate(String predicateName, boolean truthVal) {
        this(predicateName, null, truthVal);
    }

    public String getPredicateName() {
        return predicateName;
    }

    public int[] getVariables() {
        return variables;
    }

    public boolean getTruthVal() {
        return truthVal;
    }

    public String getPredicateKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(predicateName);
        if (variables != null && variables.length > 0) {
            sb.append("(");
            for (int variable : variables)
                sb.append(variable).append(", ");
            sb.delete(sb.length() - 2, sb.length());
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Predicate predicate = (Predicate) o;
        return truthVal == predicate.truthVal &&
                Objects.equals(predicateName, predicate.predicateName) &&
                Arrays.equals(variables, predicate.variables);
    }


    @Override
    public String toString() {
        String prefix = truthVal ? "" : "¬";
        return prefix + getPredicateKey();
    }

    @Override
    public Predicate clone() {
        return new Predicate(predicateName, variables.clone(), truthVal);
    }
}
