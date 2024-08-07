package marsdomain.mcts;

import agent.intention.Choice;
import marsdomain.RoverAgent;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MCTSScheduler {
    public static final int ALPHA = 100;
    public static final int BETA = 10;
    public static final ArrayList<Long> clock = new ArrayList<>();
    public static double bestSimResult = Double.NEGATIVE_INFINITY;
    public static LinkedList<Choice> bestChoices = new LinkedList<>();

    TreeNode root;

    public MCTSScheduler(RoverAgent agent) {
        root = new TreeNode(agent);
    }

    /**
     * public static method to re-init the scheduler for multiple runs
     */
    public static void reInitGlobalVars() {
        clock.clear();
        bestSimResult = Double.NEGATIVE_INFINITY;
        bestChoices = new LinkedList<>();
    }

    public static void removeChoices(int numberOfChoices) {
        if (numberOfChoices > bestChoices.size()) {
            return;
        }

        for (int i = 0; i < numberOfChoices; i++) {
            bestChoices.removeFirst();
        }
    }

    public List<Choice> schedule() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (!bean.isCurrentThreadCpuTimeSupported()) {
            System.err.println("CPU time measurement is not supported.");
        }
        long startCpuTime = bean.getCurrentThreadCpuTime();

        for (int i = 0; i < ALPHA; i++) {
            TreeNode maxUCTLeaf = root.select();
            if (i == ALPHA - 1)
                System.out.print("");
            maxUCTLeaf.expand();
            TreeNode newLeaf = maxUCTLeaf.getRandomChild();
            double bestResult = -100;
            for (int j = 0; j < BETA; j++) {
                double result = newLeaf.simulate();
                if (bestResult < result)
                    bestResult = result;
            }
            newLeaf.backPropagate(bestResult);
        }

        long endCpuTime = bean.getCurrentThreadCpuTime();
        long cpuTimeUsedMs = (endCpuTime - startCpuTime) / 1_000_000;
        clock.add(cpuTimeUsedMs);

        return bestChoices;
    }
}
