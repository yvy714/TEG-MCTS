package marsdomain.action;

import agent.action.Action;
import belief.Predicate;
import marsdomain.MarsRoverDomain;
import marsdomain.RoverAgent;

import java.util.Arrays;

public class PerformExp extends Action {
    private final int[] expLocation;

    public PerformExp(int[] expLocation) {
        this.expLocation = expLocation.clone();
    }


    @Override
    public boolean simulationAct(RoverAgent agent) {
        int[] currentLocation = agent.beliefBase.at.getVariables().clone();

        // notice exp location cannot be the same as hole location
        if (agent.batt_level > 0 && Arrays.equals(expLocation, currentLocation)) {
            agent.beliefBase.update(new Predicate("exp", currentLocation));

            agent.beliefBase.update(new Predicate("batt_full", false));

            agent.beliefBase.update(new Predicate("batt", new int[]{agent.batt_level - 1}));
            agent.batt_level--;

            if (agent.batt_level <= 0)
                agent.beliefBase.update(new Predicate("batt_empty"));

            return true;
        }
        return false;
    }

    @Override
    public boolean act(RoverAgent agent) {
        MarsRoverDomain domain = agent.environment;
        int[] currentLocation = domain.agentLocation.clone();

        // notice exp location cannot be the same as hole location
        if (agent.batt_level > 0 && Arrays.equals(expLocation, currentLocation)) {
            agent.beliefBase.update(new Predicate("exp", currentLocation));
            agent.batt_level--;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "PERFORM EXP";
    }
}
