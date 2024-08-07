package marsdomain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MarsRoverDomain {
    /* static default config of the Mars rover domain */
    public static final int DEFAULT_GRID_HEIGHT = 21;
    public static final int DEFAULT_GRID_WIDTH = 21;
    public static final int DEFAULT_NUMBER_OF_HOLES = 10;
    public static final int DEFAULT_MAX_BATTERY_CAPACITY = 60;
    public static final int DEFAULT_NUMBER_OF_EXPERIMENT_LOCATIONS = 5;


    /* Config of current domain */
    public final int GRID_HEIGHT;
    public final int GRID_WIDTH;
    public final int NUMBER_OF_HOLES;
    public final int MAX_BATTERY_CAPACITY;
    public final int NUMBER_OF_EXPERIMENT_LOCATIONS;
    public int[] baseLocation;
    public int[] agentLocation;
    /* Information about current domain */
    ArrayList<int[]> holeLocations = new ArrayList<>();
    ArrayList<int[]> expLocations = new ArrayList<>(16);


    public MarsRoverDomain() {
        GRID_WIDTH = DEFAULT_GRID_WIDTH;
        GRID_HEIGHT = DEFAULT_GRID_HEIGHT;
        NUMBER_OF_HOLES = DEFAULT_NUMBER_OF_HOLES;
        MAX_BATTERY_CAPACITY = DEFAULT_MAX_BATTERY_CAPACITY;
        NUMBER_OF_EXPERIMENT_LOCATIONS = DEFAULT_NUMBER_OF_EXPERIMENT_LOCATIONS;

        baseLocation = new int[]{(GRID_WIDTH - 1) / 2, (GRID_HEIGHT - 1) / 2};
        agentLocation = new int[]{(GRID_WIDTH - 1) / 2, (GRID_HEIGHT - 1) / 2};

        // generate holes and experiment locations
        generateLocation();
    }

    public static ArrayList<int[]> getLocations() {
        ArrayList<int[]> locations = new ArrayList<>();
        for (int i = 0; i < MarsRoverDomain.DEFAULT_GRID_WIDTH; i++) {
            for (int j = 0; j < MarsRoverDomain.DEFAULT_GRID_HEIGHT; j++) {
                locations.add(new int[]{i, j});
            }
        }
        return locations;
    }

    private void generateLocation() {
        ArrayList<int[]> availableLocations = new ArrayList<>(GRID_WIDTH * GRID_HEIGHT);
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                if (i != baseLocation[0] && j != baseLocation[1])
                    availableLocations.add(new int[]{i, j});
            }
        }
        Collections.shuffle(availableLocations, new Random());

        for (int i = 0; i < NUMBER_OF_HOLES; i++)
            holeLocations.add(availableLocations.remove(0));

        for (int i = 0; i < NUMBER_OF_EXPERIMENT_LOCATIONS; i++)
            expLocations.add(availableLocations.remove(0));

    }

    /**
     * Returns a string representation of the Mars Rover domain environment.
     * The grid is represented as a 2D character array where:
     * <ul>
     *   <li>'.' represents an empty cell</li>
     *   <li>'B' represents the base station</li>
     *   <li>'H' represents a hole</li>
     *   <li>'E' represents an experiment location</li>
     *   <li>'A' represents the agent (rover)</li>
     * </ul>
     *
     * @return A string representing the current state of the grid.
     */
    @Override
    public String toString() {
        // Create a grid of characters initialized to '.'
        char[][] grid = new char[GRID_WIDTH][GRID_HEIGHT];
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                grid[i][j] = '.';
            }
        }

        // Place the base station
        if (baseLocation != null && baseLocation.length == 2) {
            grid[baseLocation[0]][baseLocation[1]] = 'B';
        }

        // Place the holes
        for (int[] hole : holeLocations) {
            if (hole.length == 2) {
                grid[hole[0]][hole[1]] = 'H';
            }
        }

        // Place the experiment locations
        for (int[] exp : expLocations) {
            if (exp.length == 2) {
                grid[exp[0]][exp[1]] = 'E';
            }
        }

        // Place the rover
        if (agentLocation != null && agentLocation.length == 2) {
            grid[agentLocation[0]][agentLocation[1]] = 'A';
        }

        // Build the string representation
        StringBuilder sb = new StringBuilder();
        for (int j = GRID_HEIGHT - 1; j >= 0; j--) {
            for (int i = 0; i < GRID_WIDTH; i++) {
                sb.append(grid[i][j]).append(' ').append(' ');
            }
            sb.append('\n');
        }

        return sb.toString();
    }

}
