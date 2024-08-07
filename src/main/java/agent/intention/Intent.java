package agent.intention;

import agent.plan.Plan;
import belief.Event;
import rm.RewardMachine;
import rm.Transition;

import java.util.ArrayList;

public class Intent implements Cloneable {
    public Event pursuingEvent;
    public Plan plan;
    public RewardMachine rewardMachine;

    public Intent(RewardMachine rewardMachine) {
        this.rewardMachine = rewardMachine;
        pursuingEvent = null;
        plan = null;
    }

    /**
     * Event selection, returns all available event, this method is deprecated because in this domain,
     * there could be at most one available event to achieve in each reward machine state.
     */
    @Deprecated
    public Event[] selectAvailableEvents() {
        ArrayList<Event> availableEvents = new ArrayList<>();

        for (Transition transition : rewardMachine.transitions.get(rewardMachine.currentState)) {
            if (transition.achievable)
                availableEvents.add(transition.event);
        }
        return availableEvents.toArray(new Event[availableEvents.size()]);
    }


    /**
     * @return the event can be achieved if event is null
     */
    public Event selectEvent() {
        for (Transition transition : rewardMachine.transitions.get(rewardMachine.currentState)) {
            if (transition.achievable)
                return transition.event;
        }
        return null;
    }

    @Override
    public Intent clone() {
        try {
            Intent clone = (Intent) super.clone();
            if (plan != null)
                clone.plan = plan.clone();
            clone.rewardMachine = (RewardMachine) rewardMachine.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
