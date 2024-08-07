package marsdomain;

import agent.action.Action;
import agent.intention.Choice;
import agent.intention.Intent;
import agent.intention.IntentionStack;
import agent.plan.Plan;
import belief.Event;
import belief.Predicate;
import marsdomain.mcts.MCTSScheduler;
import marsdomain.plan.BasicRoverPlan;
import marsdomain.plan.ExpPlan;
import marsdomain.plan.MovePlan;
import marsdomain.plan.MoveStrategy;
import rm.RewardMachine;
import rm.Transition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class RoverAgent implements Cloneable {
    public MarsRoverDomain environment;
    public RoverBeliefBase beliefBase;
    public int batt_level;
    public double reward = 0;
    public ArrayList<IntentionStack> intentions = new ArrayList<>(20);
    /**
     * The index of intention that this agent is currently pursuing
     */
    public int intentionChoice = -1;
    // the number of choices applied last round
    public int choiceCount;

    public RoverAgent(MarsRoverDomain environment) {
        this.environment = environment;
        initializeBeliefBase();
        initializeGoals();
    }

    public void run() {
        while (!finishAllGoals() && isAgentActive()) {
            MCTSScheduler scheduler = new MCTSScheduler(this.clone());
            Action action = this.deliberate(scheduler.schedule());
            MCTSScheduler.removeChoices(choiceCount);
            if (!this.performAction(action))
                return;
            //System.out.println("Action: " + action + "; Location: " + Arrays.toString(environment.agentLocation) + "; Battery level: " + batt_level);
            senseEnvironment();
        }
    }

    public Action deliberate(List<Choice> choices) {
        if (!isAgentActive() || finishAllGoals() || choices == null || choices.isEmpty())
            return null;
        int numChoices = 0;
        // intention progression cycle
        for (Choice choice : choices) {
            numChoices++;
            // update rms according to environmental change, and drops completed/intentions goals/sub-goals
            // corresponds to the second part of Belief Update and Goal Update in paper
            updateIntentions();
            if (finishAllGoals() || reward < 0) {
                return null;
            }

            intentionChoice = choice.intentionChoice;

            Intent pursingIntent = intentions.get(intentionChoice).peek();

            if (pursingIntent.pursuingEvent == null || pursingIntent.plan == null) {
                pursingIntent.pursuingEvent = pursingIntent.selectEvent();
                pursingIntent.plan = getAvailablePlans(pursingIntent.pursuingEvent)[choice.planChoice];
            }

            if (pursingIntent.plan.peekNextStep().isAction()) {
                choiceCount = numChoices;
                return (Action) pursingIntent.plan.getNextStep();
            } else {
                // push the sub-goal to the top of the stack
                intentions.get(intentionChoice).push(pursingIntent.plan.getNextStep());
                intentionChoice = -1;
            }
        }
        return null;
    }

    public void updateIntentions() {
        for (int i = 0; i < intentions.size(); i++) {
            reward += updateSingleIntention(i, intentions.get(i));
        }
        reward = reward < 0 ? RewardMachine.NEGATIVE_INFINITY : reward;
    }


    /**
     * This method firstly updates any transitions (Belief Update),
     * and then drop intents that are true on trace or violated.
     *
     * @param idx       the intention index in agent's intention list
     * @param intention the intention to update
     */
    private double updateSingleIntention(int idx, IntentionStack intention) {
        double r = 0;
        ArrayList<Intent> intents = intention.intents;

        // for each intent, check if any transition happens
        for (Intent intent : intents) {
            RewardMachine rm = intent.rewardMachine;
            for (Transition transition : rm.transitions.get(rm.currentState)) {
                if (beliefBase.verify(transition.event)) {
                    r = r + transition.reward;
                    rm.currentState = transition.destState;
                    intent.plan = null;
                    intent.pursuingEvent = null;
                    if (transition.reward != 0) {
                        rm.dropped = true;
                    }
                    break;
                }
            }
        }

        // drop intent that is achieved or invariant is violated
        // and their associate sub-goals (i.e., intents on top of it)
        for (int i = 0; i < intents.size(); i++) {
            if (intents.get(i).rewardMachine.dropped) {
                intents.subList(i, intents.size()).clear();
                // allow for interleaving
                if (idx == intentionChoice) {
                    intentionChoice = -1;
                }
                break;
            }
        }

        return r;
    }


    public ArrayList<Integer> getAvailableIntentionChoices() {
        ArrayList<Integer> intentionChoices = new ArrayList<>(intentions.size());
        for (int i = 0; i < intentions.size(); i++) {
            if (!intentions.get(i).notAchievable())
                intentionChoices.add(i);
        }
        Collections.shuffle(intentionChoices);
        return intentionChoices;
    }


    /**
     * Agent needs to update and process the following predicates from environment:
     * <p>
     * hole
     * batt_full
     * batt_empty
     * batt(_)
     * at(_, _)
     * at_base
     */
    public void senseEnvironment() {
        ArrayList<Predicate> predicates = new ArrayList<>();

        // at(_, _)
        predicates.add(new Predicate("at", environment.agentLocation.clone()));

        // hole
        Predicate atHolePredicate = beliefBase.hole_at.get(new Predicate("hole_at", environment.agentLocation).getPredicateKey());
        boolean atHole = atHolePredicate != null;
        predicates.add(new Predicate("hole", atHole));

        // batt_full
        predicates.add(new Predicate("batt_full", batt_level == environment.MAX_BATTERY_CAPACITY));

        // batt_empty
        predicates.add(new Predicate("batt_empty", batt_level <= 0));

        // batt(_)
        predicates.add(new Predicate("batt", new int[]{batt_level}, true));

        // at_base
        predicates.add(new Predicate("at_base", Arrays.equals(environment.agentLocation, environment.baseLocation)));

        beliefBase.update(predicates);
    }

    public void initializeBeliefBase() {
        // list of predicates to be initialized
        ArrayList<Predicate> predicates = new ArrayList<>(32);

        // initialize battery information
        predicates.add(new Predicate("batt_empty", false));
        predicates.add(new Predicate("batt_full", true));
        predicates.add(new Predicate("batt", new int[]{environment.MAX_BATTERY_CAPACITY}));
        batt_level = environment.MAX_BATTERY_CAPACITY;

        // initialize hole information
        predicates.add(new Predicate("hole", false));

        for (int[] holeLocation : environment.holeLocations) {
            predicates.add(new Predicate("hole_at", holeLocation));
        }

        // initialize experiment location
        for (int[] expLocation : environment.expLocations) {
            predicates.add(new Predicate("exp", expLocation, false));
        }

        // initialize agent position
        predicates.add(new Predicate("at", environment.agentLocation.clone()));


        // initialize base position
        predicates.add(new Predicate("base_at", environment.baseLocation));
        predicates.add(new Predicate("at_base", Arrays.equals(environment.baseLocation, environment.agentLocation)));

        this.beliefBase = new RoverBeliefBase(predicates);
    }

    public Plan[] getAvailablePlans(Event planGoal) {
        // init 2 available plans for moving to x and y
        if (planGoal.conjunction[0].getPredicateName().equalsIgnoreCase("at")) {
            return new Plan[]{new MovePlan(MoveStrategy.HUMAN_AUTHORED, this, planGoal),
                    new MovePlan(MoveStrategy.Q_LEARNING_BASED, this, planGoal),};
        } else if (planGoal.conjunction[0].getPredicateName().equalsIgnoreCase("exp")) {
            return new Plan[]{
                    new ExpPlan(MoveStrategy.HUMAN_AUTHORED, this, planGoal),
                    new ExpPlan(MoveStrategy.Q_LEARNING_BASED, this, planGoal)
            };
        }
        // else, the BasicRoverPlan class will achieve the goal, just pass planGoal to its constructor
        return new Plan[]{new BasicRoverPlan(this, planGoal)};
    }

    public int[] getAvailablePlanIndexes(Event planGoal) {
        // init 2 available plans for moving to x and y
        if (planGoal.conjunction[0].getPredicateName().equalsIgnoreCase("at")
                || planGoal.conjunction[0].getPredicateName().equalsIgnoreCase("exp"))
            return new int[]{0, 1};
        // else, the BasicRoverPlan class will achieve the goal, just pass planGoal to its constructor
        return new int[]{0};
    }

    /**
     * NOTE: the self-transitions are removed for simplicity.
     */
    public void initializeGoals() {
        // G¬batt_empty
        RewardMachine rm1 = new RewardMachine(2);
        Event event1 = new Event(new Predicate("batt_empty"));
        rm1.addTransition(0, new Transition(event1, RewardMachine.NEGATIVE_INFINITY, 1, false));
        intentions.add(new IntentionStack(new Intent(rm1), false));

        // F(exp_xiyi ∧ F at_base)
        RewardMachine rm3;
        for (int[] expLocation : environment.expLocations) {
            rm3 = new RewardMachine(3);
            // state 0
            Event event5 = new Event(new Predicate("exp", expLocation, true));
            rm3.addTransition(0, new Transition(event5, 1, 1, true));
            // state 1
            //Event event6 = new Event(new Predicate("at", environment.baseLocation.clone()));
            //rm3.addTransition(1, new Transition(event6, 1, 2, true));

            intentions.add(new IntentionStack(new Intent(rm3)));
        }

        // F batt_full
        RewardMachine rm2 = new RewardMachine(3);
        // state 0
        Event event2 = new Event(new Predicate("at", environment.baseLocation.clone()));
        rm2.addTransition(0, new Transition(event2, 0, 1, true));
        // state 1
        Event event3 = new Event(new Predicate("at_base", false));
        rm2.addTransition(1, new Transition(event3, 0, 0, false));
        Event event4 = new Event(new Predicate("batt_full"));
        rm2.addTransition(1, new Transition(event4, Double.MIN_VALUE, 2, true));
        intentions.add(new IntentionStack(new Intent(rm2)));
    }

    /**
     * Agent is active if its battery level is above zero and not at hole.
     */
    public boolean isAgentActive() {
        Predicate atHole = new Predicate("hole");
        return this.batt_level > 0 && !beliefBase.verify(new Event(atHole));
    }

    /**
     * @return true if agent has finished all its executable top-level goals
     */
    public boolean finishAllGoals() {
        for (IntentionStack intention : intentions) {
            if (!intention.isFinished())
                return false;
        }
        return true;
    }

    public int finishedGoals() {
        int count = 0;
        for (IntentionStack intention : intentions) {
            if (intention.isFinished())
                count++;
        }
        return count;
    }

    public void actFailed() {
        intentions.get(intentionChoice).failed = true;
    }

    public boolean performAction(Action action) {
        if (action != null)
            return action.act(this);
        return false;
    }

    @Override
    public RoverAgent clone() {
        try {
            RoverAgent clone = (RoverAgent) super.clone();
            clone.beliefBase = beliefBase.clone();
            clone.intentions = new ArrayList<>(intentions.size());
            for (int i = 0; i < intentions.size(); i++) {
                clone.intentions.add(intentions.get(i).clone());
                if (!clone.intentions.get(i).notAchievable()
                        && clone.intentions.get(i).peek().plan != null && clone.intentions.get(i).peek().plan instanceof MovePlan) {
                    ((MovePlan) clone.intentions.get(i).peek().plan).beliefBase = clone.beliefBase;
                }
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
