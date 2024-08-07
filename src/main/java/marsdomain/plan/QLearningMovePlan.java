package marsdomain.plan;

import agent.plan.Step;
import marsdomain.MarsRoverDomain;
import marsdomain.action.Move;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QLearningMovePlan {
    private static final int GRID_SIZE = 21;
    private static final int ACTIONS_COUNT = 4; // Left, Right, Up, Down
    private static final double LEARNING_RATE = 0.1;
    private static final double DISCOUNT_FACTOR = 0.9;
    private static final double EPSILON = 0.1;
    private static final int EPISODES = 10000;

    private static final HashMap<String, Step[]> plans = new HashMap<>();

    private final double[][][] qTable;
    private final Random random;


    public QLearningMovePlan() {
        qTable = new double[GRID_SIZE][GRID_SIZE][ACTIONS_COUNT];
        random = new Random();
    }

    public static Step[] getPlan(int[] src, int[] dest) {
        return plans.get(Arrays.toString(src) + Arrays.toString(dest));
    }

    public static void outPutPlan(String file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String s : plans.keySet()) {
                writer.write(s + ": " + Arrays.toString(plans.get(s)));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Problem writing to file " + file);
        }
    }


    public static void loadPlan(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Split the line by the ': ' separator
                String[] parts = line.split(": ");
                // The first part is the source and destination
                String srcDest = parts[0];
                // The second part is the plan, remove the square brackets
                String plan = parts[1].substring(1, parts[1].length() - 1); // Removing the surrounding brackets

                // Split the plan into individual actions
                String[] actions = plan.split(", ");

                LinkedList<Step> moves = new LinkedList<>();
                // Print the extracted information
                for (String action : actions) {
                    switch (action) {
                        case "UP":
                            moves.addLast(new Move(Move.Direction.UP));
                            break;
                        case "DOWN":
                            moves.addLast(new Move(Move.Direction.DOWN));
                            break;
                        case "LEFT":
                            moves.addLast(new Move(Move.Direction.LEFT));
                            break;
                        case "RIGHT":
                            moves.addLast(new Move(Move.Direction.RIGHT));
                            break;
                    }
                }
                plans.put(srcDest, moves.toArray(new Step[0]));
            }
        } catch (IOException e) {
            System.err.println("Problem reading file " + file);
        }
    }

    public static void generatePlans() {
        List<int[]> locations = MarsRoverDomain.getLocations();
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int[] src : locations) {
            for (int[] dest : locations) {
                executorService.submit(new PlanGenerationTask(src, dest));
            }
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            // Wait for all tasks to finish
        }
    }

    private static Step[] generateSinglePlan(int[] src, int[] dest) {
        QLearningMovePlan scheduler = new QLearningMovePlan();
        return scheduler.findPath(src[0], src[1], dest[0], dest[1]);
    }

    private int chooseAction(int x, int y) {
        if (random.nextDouble() < EPSILON) {
            return random.nextInt(ACTIONS_COUNT);
        }
        return maxQAction(x, y);
    }

    private int maxQAction(int x, int y) {
        int bestAction = 0;
        double maxQ = qTable[x][y][0];
        for (int action = 1; action < ACTIONS_COUNT; action++) {
            if (qTable[x][y][action] > maxQ) {
                maxQ = qTable[x][y][action];
                bestAction = action;
            }
        }
        return bestAction;
    }

    private int[] performAction(int x, int y, int action) {
        switch (action) {
            case 0:
                return new int[]{x - 1, y}; // Left
            case 1:
                return new int[]{x + 1, y}; // Right
            case 2:
                return new int[]{x, y + 1}; // Up
            case 3:
                return new int[]{x, y - 1}; // Down
            default:
                return new int[]{x, y};
        }
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE;
    }

    private void train(int startX, int startY, int goalX, int goalY) {
        for (int episode = 0; episode < EPISODES; episode++) {
            int x = startX;
            int y = startY;

            while (x != goalX || y != goalY) {
                int action = chooseAction(x, y);
                int[] nextState = performAction(x, y, action);
                int nextX = nextState[0];
                int nextY = nextState[1];

                if (!isInBounds(nextX, nextY)) {
                    nextX = x;
                    nextY = y;
                }

                double reward = (nextX == goalX && nextY == goalY) ? 100 : -1;
                double maxQNext = qTable[nextX][nextY][maxQAction(nextX, nextY)];
                qTable[x][y][action] = qTable[x][y][action] + LEARNING_RATE * (reward + DISCOUNT_FACTOR * maxQNext - qTable[x][y][action]);

                x = nextX;
                y = nextY;
            }
        }
    }

    public Step[] findPath(int startX, int startY, int goalX, int goalY) {
        ArrayList<Step> steps = new ArrayList<>();

        train(startX, startY, goalX, goalY);
        int x = startX;
        int y = startY;
        while (x != goalX || y != goalY) {
            int action = maxQAction(x, y);
            switch (action) {
                case 0:
                    steps.add(new Move(Move.Direction.LEFT));
                    break;
                case 1:
                    steps.add(new Move(Move.Direction.RIGHT));
                    break;
                case 2:
                    steps.add(new Move(Move.Direction.UP));
                    break;
                case 3:
                    steps.add(new Move(Move.Direction.DOWN));
                    break;
                default:
                    System.err.println("Unknown direction: " + action);
            }

            int[] nextState = performAction(x, y, action);
            x = nextState[0];
            y = nextState[1];
        }
        return steps.toArray(new Step[0]);
    }


    private static class PlanGenerationTask implements Runnable {
        private final int[] src;
        private final int[] dest;

        public PlanGenerationTask(int[] src, int[] dest) {
            this.src = src;
            this.dest = dest;
        }

        @Override
        public void run() {
            Step[] plan = generateSinglePlan(src, dest);
            plans.put(Arrays.toString(src) + Arrays.toString(dest), plan);
        }
    }

}
