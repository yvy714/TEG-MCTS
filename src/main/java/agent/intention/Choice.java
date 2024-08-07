package agent.intention;

/**
 * In the experiment domain, each state in reward machine has no more than one achievable event, so we omit the event selection.
 */
public class Choice implements Cloneable {
    public int intentionChoice;
    public int planChoice;

    public Choice(int intentionChoice, int planChoice) {
        this.intentionChoice = intentionChoice;
        this.planChoice = planChoice;
    }

    @Override
    public Choice clone() {
        try {
            return (Choice) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
