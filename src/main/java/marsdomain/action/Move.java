package marsdomain.action;

import agent.action.Action;
import belief.Predicate;
import marsdomain.MarsRoverDomain;
import marsdomain.RoverAgent;

import java.util.Arrays;

public class Move extends Action {
    private final Direction direction;

    public Move(Direction direction) {
        this.direction = direction;
    }

    @Override
    public boolean act(RoverAgent agent) {
        MarsRoverDomain domain = agent.environment;
        int[] currentLocation = domain.agentLocation;
        int[] nextLocation = getNextLocation(currentLocation);

        if (nextLocation[0] < domain.GRID_WIDTH && nextLocation[1] < domain.GRID_HEIGHT
                && nextLocation[0] > -1 && nextLocation[1] > -1 && agent.batt_level > 0) {
            domain.agentLocation = nextLocation;
            agent.batt_level--;
            return true;
        }
        return false;
    }

    @Override
    public boolean simulationAct(RoverAgent agent) {
        MarsRoverDomain domain = agent.environment;

        int[] currentLocation = agent.beliefBase.at.getVariables();
        int[] nextLocation = getNextLocation(currentLocation);

        if (nextLocation[0] < domain.GRID_WIDTH && nextLocation[1] < domain.GRID_HEIGHT
                && nextLocation[0] > -1 && nextLocation[1] > -1 && agent.batt_level > 0) {

            agent.beliefBase.update(new Predicate("at", nextLocation));

            // update battery belief
            agent.beliefBase.update(new Predicate("batt_full", false));

            agent.beliefBase.update(new Predicate("batt", new int[]{agent.batt_level - 1}));
            agent.batt_level--;

            if (agent.batt_level <= 0)
                agent.beliefBase.update(new Predicate("batt_empty"));

            // hole
            Predicate atHolePredicate = agent.beliefBase.hole_at.
                    get(new Predicate("hole_at", nextLocation).getPredicateKey());
            if (atHolePredicate != null)
                agent.beliefBase.update(new Predicate("hole"));

            // at_base
            boolean atBase = Arrays.equals(nextLocation, agent.beliefBase.base_at.getVariables());
            agent.beliefBase.update(new Predicate("at_base", atBase));

            return true;
        }
        return false;
    }

    private int[] getNextLocation(int[] currentLocation) {
        int[] nextLocation = currentLocation.clone();
        switch (direction) {
            case UP:
                nextLocation[1]++;
                break;
            case DOWN:
                nextLocation[1]--;
                break;
            case LEFT:
                nextLocation[0]--;
                break;
            case RIGHT:
                nextLocation[0]++;
                break;
        }
        return nextLocation;
    }

    @Override
    public String toString() {
        return this.direction.toString();
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}
