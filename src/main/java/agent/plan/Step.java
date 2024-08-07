package agent.plan;

public abstract class Step implements Cloneable {
    public abstract boolean isAction();

    @Override
    public abstract Object clone();
}
