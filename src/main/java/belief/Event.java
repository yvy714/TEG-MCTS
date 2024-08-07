package belief;


public class Event {
    /**
     * The literal array represents conjunction(^) between literals
     */
    public Predicate[] conjunction;

    public Event(Predicate... predicates) {
        this.conjunction = predicates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Predicate predicate : conjunction)
            sb.append(predicate.toString()).append(" ∧ ");
        sb.delete(sb.length() - 3, sb.length());
        return sb.toString();
    }
}
