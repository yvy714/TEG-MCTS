package marsdomain;

import belief.Event;
import belief.Predicate;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RoverBeliefBase implements Cloneable {
    public HashMap<String, Predicate> hole_at;
    public HashMap<String, Predicate> exp;
    public Predicate batt;
    public Predicate hole;
    public Predicate batt_empty;
    public Predicate batt_full;
    public Predicate at_base;
    public Predicate base_at;
    public Predicate at;


    public RoverBeliefBase(Collection<Predicate> predicates) {
        hole_at = new HashMap<>();
        exp = new HashMap<>();
        update(predicates);
    }

    public void update(Predicate predicate) {
        switch (predicate.getPredicateName()) {
            case "at":
                at = predicate;
                break;
            case "hole":
                hole = predicate;
                break;
            case "batt":
                batt = predicate;
                break;
            case "batt_empty":
                batt_empty = predicate;
                break;
            case "batt_full":
                batt_full = predicate;
                break;
            case "at_base":
                at_base = predicate;
                break;
            case "base_at":
                base_at = predicate;
                break;
            case "hole_at":
                hole_at.put(predicate.getPredicateKey(), predicate);
                break;
            case "exp":
                exp.put(predicate.getPredicateKey(), predicate);
                break;
            default:
                System.err.println("Unknown predicate " + predicate.getPredicateName());
                break;
        }
    }

    public void update(Collection<Predicate> predicates) {
        for (Predicate predicate : predicates) {
            update(predicate);
        }
    }

    public boolean verify(Event event) {
        for (Predicate predicate : event.conjunction) {
            Predicate inMap;
            switch (predicate.getPredicateName()) {
                case "at":
                    if (predicate.getTruthVal()) {
                        if (!at.equals(predicate))
                            return false;
                    } else {
                        if (Arrays.equals(at.getVariables(), predicate.getVariables()))
                            return false;
                    }
                    break;

                case "hole":
                    if (!hole.equals(predicate))
                        return false;
                    break;

                case "batt":
                    if (predicate.getTruthVal()) {
                        if (!batt.equals(predicate))
                            return false;
                    } else {
                        if (Arrays.equals(batt.getVariables(), predicate.getVariables()))
                            return false;
                    }
                    break;

                case "batt_empty":
                    if (!batt_empty.equals(predicate))
                        return false;
                    break;

                case "batt_full":
                    if (!batt_full.equals(predicate))
                        return false;
                    break;

                case "at_base":
                    if (!at_base.equals(predicate))
                        return false;
                    break;

                case "base_at":
                    if (predicate.getTruthVal()) {
                        if (!base_at.equals(predicate))
                            return false;
                    } else {
                        if (Arrays.equals(base_at.getVariables(), predicate.getVariables()))
                            return false;
                    }
                    break;


                case "hole_at":
                    inMap = hole_at.get(predicate.getPredicateKey());
                    if (predicate.getTruthVal()) {
                        if (inMap == null || !inMap.equals(predicate))
                            return false;
                    } else {
                        if (inMap != null && !inMap.equals(predicate))
                            return false;
                    }
                    break;

                case "exp":
                    inMap = exp.get(predicate.getPredicateKey());
                    if (predicate.getTruthVal()) {
                        if (inMap == null || !inMap.equals(predicate))
                            return false;
                    } else {
                        if (inMap != null && !inMap.equals(predicate))
                            return false;
                    }
                    break;

                default:
                    System.err.println("Unknown predicate " + predicate.getPredicateName());
                    break;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(batt.toString()).append("\n");
        sb.append(hole.toString()).append("\n");
        sb.append(batt_empty.toString()).append("\n");
        sb.append(batt_full.toString()).append("\n");
        sb.append(at_base.toString()).append("\n");
        sb.append(base_at.toString()).append("\n");
        sb.append(at.toString()).append("\n");

        for (Predicate predicate : exp.values()) {
            sb.append(predicate.toString()).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "\n");
        sb.append("\n");

        for (Predicate predicate : hole_at.values()) {
            sb.append(predicate.toString()).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "\n");

        return sb.toString();
    }

    @Override
    public RoverBeliefBase clone() {
        try {
            RoverBeliefBase clone = (RoverBeliefBase) super.clone();

            clone.hole_at = new HashMap<>();
            for (Map.Entry<String, Predicate> stringPredicateEntry : hole_at.entrySet()) {
                clone.hole_at.put(stringPredicateEntry.getKey(), stringPredicateEntry.getValue().clone());
            }
            clone.exp = new HashMap<>();
            for (Map.Entry<String, Predicate> stringPredicateEntry : exp.entrySet()) {
                clone.exp.put(stringPredicateEntry.getKey(), stringPredicateEntry.getValue().clone());
            }

            clone.batt = batt.clone();
            clone.batt_empty = batt_empty.clone();
            clone.batt_full = batt_full.clone();
            clone.at_base = at_base.clone();
            clone.base_at = base_at.clone();
            clone.at = at.clone();
            clone.hole = hole.clone();

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
