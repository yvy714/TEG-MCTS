package marsdomain.plan;

import agent.plan.Plan;
import agent.plan.Step;
import belief.Event;
import marsdomain.RoverAgent;
import marsdomain.RoverBeliefBase;
import marsdomain.action.PerformExp;

public class ExpPlan extends Plan {
    final int[] dest;
    final private MoveStrategy moveStrategy;
    public RoverBeliefBase beliefBase;
    int[] source;
    private boolean planGenerationLock;

    public ExpPlan(MoveStrategy moveStrategy, RoverAgent agent, Event planGoal) {
        this.beliefBase = agent.beliefBase;
        this.dest = planGoal.conjunction[0].getVariables();
        this.moveStrategy = moveStrategy;
        this.planGenerationLock = true;
    }

    private void getPlan() {
        this.source = beliefBase.at.getVariables().clone();
        planBody = moveStrategy.getMoveSteps(source, dest);
        Step[] steps = new Step[planBody.length + 1];
        System.arraycopy(planBody, 0, steps, 0, planBody.length);
        steps[steps.length - 1] = new PerformExp(dest);
        planBody = steps;
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
