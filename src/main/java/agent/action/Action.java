package agent.action;

import agent.plan.Step;
import marsdomain.RoverAgent;

public abstract class Action extends Step {
    public abstract boolean act(RoverAgent agent);

    public abstract boolean simulationAct(RoverAgent agent);

    @Override
    public boolean isAction() {
        return true;
    }

    @Override
    public Object clone() {
        return this;
    }
}
