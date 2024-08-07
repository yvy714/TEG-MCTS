package marsdomain.plan;

import agent.plan.Step;

public enum MoveStrategy {
    Q_LEARNING_BASED, HUMAN_AUTHORED;

    public Step[] getMoveSteps(int[] source, int[] dest) {
        if (this == Q_LEARNING_BASED)
            return QLearningMovePlan.getPlan(source, dest);
        else
            return HumanAuthoredMovePlan.getPlan(source, dest);
    }
}