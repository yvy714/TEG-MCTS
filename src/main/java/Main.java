import marsdomain.MarsRoverDomain;
import marsdomain.RoverAgent;
import marsdomain.mcts.MCTSScheduler;
import marsdomain.plan.HumanAuthoredMovePlan;
import marsdomain.plan.QLearningMovePlan;


public class Main {
    public static void main(String[] args) {
        // generate human-authored plans and load reinforcement learning plans
        HumanAuthoredMovePlan.generatePlans();
        QLearningMovePlan.loadPlan("rl_plan.txt");

        int i, NUM_OF_RUNS = 100;
        double r = 0;
        for (i = 0; i < NUM_OF_RUNS; i++) {
            MCTSScheduler.reInitGlobalVars();
            MarsRoverDomain marsRoverDomain = new MarsRoverDomain();
            RoverAgent agent = new RoverAgent(marsRoverDomain);
            agent.run();
            r += agent.reward;
            System.out.println("\nAgent reward; " + agent.reward + "\n");
            System.out.println("Belief base\n" + agent.beliefBase);
        }

    }
}
