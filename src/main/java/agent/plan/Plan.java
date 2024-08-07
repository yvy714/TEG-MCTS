package agent.plan;

public abstract class Plan implements Cloneable {
    protected Step[] planBody;
    /**
     * The index of the next step to be executed in the plan body;
     */
    protected int currentStepIndex = 0;

    public Step getNextStep() {
        return planBody[currentStepIndex++];
    }

    public Step peekNextStep() {
        return planBody[currentStepIndex];
    }

    /**
     * If this plan is finished
     */
    public boolean isFinished() {
        return currentStepIndex == planBody.length;
    }

    public boolean isFirstStep() {
        return currentStepIndex == 0;
    }

    @Override
    public Plan clone() {
        try {
            Plan clone = (Plan) super.clone();
            clone.planBody = new Step[planBody.length];
            for (int i = 0; i < planBody.length; i++) {
                clone.planBody[i] = (Step) planBody[i].clone();
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
