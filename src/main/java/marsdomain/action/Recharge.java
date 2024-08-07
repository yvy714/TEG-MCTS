package marsdomain.action;

import agent.action.Action;
import belief.Predicate;
import marsdomain.MarsRoverDomain;
import marsdomain.RoverAgent;

import java.util.Arrays;

public class Recharge extends Action {

    @Override
    public boolean act(RoverAgent agent) {
        MarsRoverDomain domain = agent.environment;
        int[] currentLocation = domain.agentLocation;
        int[] baseLocation = domain.baseLocation;

        if (Arrays.equals(currentLocation, baseLocation)) {
            agent.batt_level = domain.MAX_BATTERY_CAPACITY;
            return true;
        }
        return false;
    }

    @Override
    public boolean simulationAct(RoverAgent agent) {
        int[] currentLocation = agent.beliefBase.at.getVariables();
        int[] baseLocation = agent.beliefBase.base_at.getVariables();

        if (Arrays.equals(currentLocation, baseLocation)) {
            // batt_full
            agent.beliefBase.update(new Predicate("batt_full"));

            // batt_empty
            agent.beliefBase.update(new Predicate("batt_empty", false));

            // batt(_)
            agent.batt_level = agent.environment.MAX_BATTERY_CAPACITY;
            agent.beliefBase.update(new Predicate("batt", new int[]{agent.batt_level}));

            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "RECHARGE";
    }
}
