package agent.intention;


import agent.plan.Step;
import rm.RewardMachine;

import java.util.ArrayList;

public class IntentionStack implements Cloneable {
    /**
     * This variable denotes that if this top level goal is suitable for execution,
     * e.g., if it is a maintenance goal, it is not suitable for execution
     */
    public boolean executable;

    /**
     * Denotes if this intention is failed.
     */
    public boolean failed;


    /**
     * This array list servers as the intention stack.
     * The tail of the list servers as the top of the stack.
     * i.e., index 0 is the bottom of the stack, index (length - 1) is the top of the stack.
     */
    public ArrayList<Intent> intents = new ArrayList<>(5);

    public IntentionStack(Intent topLevelGoal, boolean executable) {
        intents.add(topLevelGoal);
        this.executable = executable;
        failed = false;
    }

    public IntentionStack(Intent topLevelGoal) {
        this(topLevelGoal, true);
    }


    public void push(Step rm) {
        Intent intent = new Intent((RewardMachine) rm);
        intents.add(intent);
    }

    /**
     * Peek the intent at the top of the stack.
     */
    public Intent peek() {
        return intents.get(intents.size() - 1);
    }

    /**
     * An Intention(top level goal) is not achievable if it is not executable (e.g., maintenance goal),
     * or it is executable and the intention stack is empty, or it has failed.
     */
    public boolean notAchievable() {
        return !executable || intents.isEmpty() || failed;
    }


    public boolean isFinished() {
        return !executable || (intents.isEmpty() && !failed);
    }

    /**
     * Denotes if this top-level-goal is violated (as a maintenance goal)
     * or completed (as an achievement) goal.
     */
//    public boolean isDropped() {
//        return totalReward < 0 || intents.isEmpty();
//    }
    @Override
    public IntentionStack clone() {
        try {
            IntentionStack clone = (IntentionStack) super.clone();
            clone.intents = new ArrayList<>(5);
            for (Intent intent : intents) {
                clone.intents.add(intent.clone());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
