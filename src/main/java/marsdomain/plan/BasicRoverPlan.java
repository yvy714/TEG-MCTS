package marsdomain.plan;

import agent.plan.Plan;
import agent.plan.Step;
import belief.Event;
import belief.Predicate;
import marsdomain.RoverAgent;
import marsdomain.action.Recharge;
import rm.RewardMachine;
import rm.Transition;

public class BasicRoverPlan extends Plan {
    public BasicRoverPlan(RoverAgent agent, Event planGoal) {
        String planGoalName = planGoal.conjunction[0].getPredicateName();
        int[] dest;
        RewardMachine subGoal;
        switch (planGoalName) {
            case "at_base":
                dest = agent.environment.baseLocation;
                subGoal = generateAtSubGoal(dest);
                planBody = new Step[]{subGoal};
                break;
            case "batt_full":
                planBody = new Step[]{new Recharge()};
                break;
            default:
                System.err.println("Unrecognized planGoal " + planGoalName);
                break;
        }
    }


    private RewardMachine generateAtSubGoal(int[] dest) {
        RewardMachine rm = new RewardMachine(3);

        Event event1 = new Event(new Predicate("at", dest.clone()));
        rm.addTransition(0, new Transition(event1, Double.MIN_VALUE, 1, true));
        Event event2 = new Event(new Predicate("hole"));
        rm.addTransition(0, new Transition(event2, RewardMachine.NEGATIVE_INFINITY, 2, false));
        return rm;
    }
}
