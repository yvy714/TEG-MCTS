package marsdomain.plan;

import agent.plan.Plan;
import agent.plan.Step;
import belief.Event;
import marsdomain.RoverAgent;
import marsdomain.RoverBeliefBase;

public class MovePlan extends Plan {
    final int[] dest;
    final private MoveStrategy moveStrategy;
    public RoverBeliefBase beliefBase;
    int[] source;
    private boolean planGenerationLock;

    public MovePlan(MoveStrategy moveStrategy, RoverAgent agent, Event planGoal) {
        this.beliefBase = agent.beliefBase;
        this.dest = planGoal.conjunction[0].getVariables();
        this.moveStrategy = moveStrategy;
        this.planGenerationLock = true;
    }


    private void getPlan() {
        this.source = beliefBase.at.getVariables().clone();
        planBody = moveStrategy.getMoveSteps(source, dest);
    }


    @Override
    public Step getNextStep() {
        if (planGenerationLock) {
            getPlan();
            planGenerationLock = false;
        }
        return planBody[currentStepIndex++];
    }

    @Override
    public Step peekNextStep() {
        if (planGenerationLock) {
            getPlan();
            planGenerationLock = false;
        }
        return planBody[currentStepIndex];
    }

}
