package rm;

import belief.Event;

public class Transition {
    public Event event;
    public double reward;
    public int destState;
    public boolean achievable;


    public Transition(Event event, double reward, int destState, boolean achievable) {
        this.event = event;
        this.reward = reward;
        this.destState = destState;
        this.achievable = achievable;
    }

}
