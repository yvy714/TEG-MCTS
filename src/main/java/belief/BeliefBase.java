package belief;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class BeliefBase {
    /**
     * The outer key is the predicate name, and the inner key is the predicate's string key representation.
     * e.g. ¬at(1, 3), the outer key is "at", inner key is "at(1, 3)".
     */
    HashMap<String, HashMap<String, Predicate>> predicates = new HashMap<>();

    public BeliefBase(Collection<Predicate> initialPredicates) {
        for (Predicate predicate : initialPredicates) {
            predicates.computeIfAbsent(predicate.predicateName, k -> new HashMap<>());
            predicates.get(predicate.predicateName).put(predicate.getPredicateKey(), predicate);
        }
    }

    public HashMap<String, Predicate> query(String predicateName) {
        return predicates.get(predicateName);
    }

    public Predicate query(String predicateName, String predicateKey) {
        return predicates.get(predicateName).get(predicateKey);
    }

    public void update(Predicate predicate) {
        Map<String, Predicate> predicateMap = predicates.get(predicate.predicateName);
        if (predicateMap == null) {
            predicates.put(predicate.predicateName, new HashMap<>());
            predicateMap = predicates.get(predicate.predicateName);
        }
        predicateMap.put(predicate.getPredicateKey(), predicate);
    }

    public void update(Predicate... predicates) {
        for (Predicate predicate : predicates) {
            update(predicate);
        }
    }

    public void update(Collection<Predicate> predicates) {
        for (Predicate predicate : predicates) {
            update(predicate);
        }
    }

    public boolean verify(Event event) {
        for (Predicate predicate : event.conjunction) {
            // if the predicate name does not exist in the literals
            Map<String, Predicate> predicateMap = predicates.get(predicate.predicateName);
            if (predicateMap == null)
                return false;

            Predicate storedPredicate = predicateMap.get(predicate.getPredicateKey());

            if (predicate.getTruthVal()) { // this predicate must exist in the map and equals
                if (storedPredicate == null || !storedPredicate.equals(predicate))
                    return false;
            } else { // this predicate may not exist in the map, or it must equal to the existed predicate
                if (storedPredicate != null && !storedPredicate.equals(predicate))
                    return false;
            }

        }
        return true;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, HashMap<String, Predicate>> outerEntry : predicates.entrySet()) {
            HashMap<String, Predicate> innerMap = outerEntry.getValue();

            for (Map.Entry<String, Predicate> innerEntry : innerMap.entrySet()) {
                Predicate predicate = innerEntry.getValue();
                if (!predicate.truthVal && predicate.variables.length > 0)
                    continue;
                sb.append(predicate).append(", ");
            }
            sb.replace(sb.length() - 2, sb.length(), "");
            sb.append("\n\n");
        }

        return sb.toString();
    }
}
