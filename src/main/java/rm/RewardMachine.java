package rm;

import agent.plan.Step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class RewardMachine extends Step implements Cloneable {
    /**
     * The negative infinity used in this program, the reason we do not use Integer.MIN_VALUE is to avoid overflow.
     */
    public static final int NEGATIVE_INFINITY = -10000;

    public HashSet<Integer> states = new HashSet<>();
    public HashMap<Integer, ArrayList<Transition>> transitions = new HashMap<>();
    public int currentState = 0;
    /**
     * Denotes if a non-zero transition happens
     */
    public boolean dropped = false;

    public RewardMachine(int stateNumber) {
        for (int i = 0; i < stateNumber; i++) {
            states.add(i);
        }
        // initialize empty transition list for each state
        for (Integer state : states) {
            transitions.put(state, new ArrayList<>());
        }
    }

    private RewardMachine() {
    }

    public void addTransition(int state, Transition transition) {
        transitions.get(state).add(transition);
    }


    @Override
    public boolean isAction() {
        return false;
    }

    @Override
    public Object clone() {
        RewardMachine clone = new RewardMachine();
        clone.states = states;
        clone.transitions = transitions;
        // mutable data types
        clone.currentState = currentState;
        clone.dropped = dropped;
        return clone;
    }

}
