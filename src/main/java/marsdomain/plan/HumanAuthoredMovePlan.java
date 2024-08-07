package marsdomain.plan;

import agent.plan.Step;
import marsdomain.MarsRoverDomain;
import marsdomain.action.Move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class HumanAuthoredMovePlan {
    private static final HashMap<String, Step[]> plans = new HashMap<>();

    public static Step[] getPlan(int[] src, int[] dest) {
        return plans.get(Arrays.toString(src) + Arrays.toString(dest));
    }

    public static void generatePlans() {
        for (int[] src : MarsRoverDomain.getLocations()) {
            for (int[] dest : MarsRoverDomain.getLocations()) {
                plans.put(Arrays.toString(src) + Arrays.toString(dest), generateSinglePlan(src, dest));
            }
        }
    }

    private static Step[] generateSinglePlan(int[] source, int[] dest) {
        int deltaX = dest[0] - source[0];
        int deltaY = dest[1] - source[1];
        ArrayList<Step> steps = new ArrayList<>(Math.abs(deltaX) + Math.abs(deltaY));

        while (deltaX != 0) {
            Move.Direction dir = deltaX > 0 ? Move.Direction.RIGHT : Move.Direction.LEFT;
            steps.add(new Move(dir));
            deltaX = deltaX > 0 ? deltaX - 1 : deltaX + 1;
        }

        while (deltaY != 0) {
            Move.Direction dir = deltaY > 0 ? Move.Direction.UP : Move.Direction.DOWN;
            steps.add(new Move(dir));
            deltaY = deltaY > 0 ? deltaY - 1 : deltaY + 1;
        }

        // shuffle the steps
        Collections.shuffle(steps);
        // Convert the ArrayList to an array
        return steps.toArray(new Step[0]);
    }
}
