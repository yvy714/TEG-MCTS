package marsdomain.mcts;

import agent.action.Action;
import agent.intention.Choice;
import agent.intention.Intent;
import agent.plan.Plan;
import belief.Event;
import marsdomain.RoverAgent;
import rm.RewardMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;


public class TreeNode {
    static final double C = 0.1;
    static final double D = 16;
    static final double sqrt2 = Math.sqrt(2);
    static final double SECOND_TERM_FACTOR = C * sqrt2;
    static final Random rand = new Random();

    int visits;
    double totalReward;
    ArrayList<Double> rewardSquares;
    RoverAgent state;
    ArrayList<Choice> choices;
    double tempUCT;
    private ArrayList<TreeNode> children;
    private TreeNode parent;


    public TreeNode(RoverAgent state) {
        this.state = state;
        rewardSquares = new ArrayList<>();
        choices = new ArrayList<>();
        children = null;
        parent = null;
        visits = 0;
        totalReward = 0;
    }

    public TreeNode getBestUCTChild() {
        TreeNode bestChild = null;
        double bestChildUCT = Double.NEGATIVE_INFINITY;
        Collections.shuffle(children);
        // to visit unvisited children first
        for (TreeNode child : children) {
            if (child.visits == 0)
                return child;
        }
        for (TreeNode child : children) {
            double childUCT = child.getUCTValue();
            if (Double.isInfinite(childUCT) || Double.isNaN(childUCT)) {
                return child;
            }

            if (childUCT > bestChildUCT) {
                bestChild = child;
                bestChildUCT = childUCT;
            }
        }
        return bestChild;
    }

    public TreeNode select() {
        TreeNode maxUCTNode = this;
        while (maxUCTNode.children != null) {
            maxUCTNode = maxUCTNode.getBestUCTChild();
        }
        return maxUCTNode;
    }

    public void expand() {
        if (this.children != null) {
            return;
        }
        ArrayList<TreeNode> expandedChildren = new ArrayList<>();
        ArrayList<TreeNode> readyQueue = new ArrayList<>();
        readyQueue.add(this);

        while (!readyQueue.isEmpty()) {
            TreeNode node = readyQueue.remove(0);
            RoverAgent agent = node.state;
            agent.updateIntentions();

            // do not add nodes that agent fails
            if (agent.reward < 0)
                continue;

            int intentionChoice = agent.intentionChoice;
            ArrayList<Integer> availableIntentions;
            if (intentionChoice == -1 || agent.intentions.get(intentionChoice).notAchievable()) {
                availableIntentions = agent.getAvailableIntentionChoices();
            } else {
                // only one intention choice available
                availableIntentions = new ArrayList<>();
                availableIntentions.add(intentionChoice);
            }

            for (Integer availableIntention : availableIntentions) {
                // peek at the chosen intention
                Intent pursingIntent = agent.intentions.get(availableIntention).peek();

                int[] planChoices;
                if (pursingIntent.pursuingEvent == null || pursingIntent.plan == null) {
                    planChoices = agent.getAvailablePlanIndexes(pursingIntent.selectEvent());
                } else {
                    // no need to perform plan selection
                    planChoices = new int[]{-1};
                }

                for (int planChoice : planChoices) {
                    TreeNode newNode = node.expansionClone(this);
                    Choice lastChoice = new Choice(availableIntention, planChoice);
                    newNode.choices.add(lastChoice);
                    newNode.state.intentionChoice = lastChoice.intentionChoice;
                    pursingIntent = newNode.state.intentions.get(lastChoice.intentionChoice).peek();
                    if (planChoice != -1) {
                        pursingIntent.pursuingEvent = pursingIntent.selectEvent();
                        pursingIntent.plan = newNode.state.getAvailablePlans(pursingIntent.pursuingEvent)[lastChoice.planChoice];
                    }

                    if (pursingIntent.plan.peekNextStep().isAction()) {
                        Plan plan = pursingIntent.plan;
                        boolean failed = false;
                        while (!plan.isFinished() && plan.peekNextStep().isAction()) {
                            Action action = (Action) plan.getNextStep();
                            newNode.choices.add(new Choice(availableIntention, -1));
                            if (!action.simulationAct(newNode.state)) {
                                failed = true;
                                break;
                            }
                            newNode.state.updateIntentions();
                            if (newNode.state.reward < 0) {
                                failed = true;
                                break;
                            }
                        }
                        if (!failed) {
                            // this is to remove the extra choice we add
                            newNode.choices.remove(newNode.choices.size() - 1);
                            newNode.state.intentionChoice = -1; // allow interleaving
                            expandedChildren.add(newNode);
                        }
                    } else {
                        if (!pursingIntent.plan.isFirstStep())
                            newNode.state.intentionChoice = -1;
                        // push the sub-goal to the top of the stack
                        newNode.state.intentions.get(lastChoice.intentionChoice).push(pursingIntent.plan.getNextStep());
                        // add to ready queue for next deliberation cycle
                        readyQueue.add(newNode);
                    }
                }
            }
        }
        children = expandedChildren.isEmpty() ? null : expandedChildren;
    }


    private TreeNode expansionClone(TreeNode parentNode) {
        TreeNode node = new TreeNode(this.state.clone());
        node.choices.addAll(choices);
        node.parent = parentNode;
        return node;
    }

    public TreeNode getRandomChild() {
        if (children == null) {
            return this;
        }

        return children.get(rand.nextInt(children.size()));
    }


    /**
     * Biased intention selection for simulation.
     * This method will bias the intention selection for moving intentions
     * (i.e., tend to select goals near to the agent),
     * for non-moving intentions, it will not be affected.
     *
     * @return the selected intention index
     */
    private int biasedRandomIntentionSelection(ArrayList<Integer> availableIntentions, RoverAgent agent) {
        int[] currentLocation = agent.beliefBase.at.getVariables().clone();

        ArrayList<Integer> nonAtIntentions = new ArrayList<>();
        ArrayList<Integer> atIntentions = new ArrayList<>();

        // filtering moving and non-moving intentions
        for (Integer availableIntention : availableIntentions) {
            Intent intent = agent.intentions.get(availableIntention).peek();
            Event event = intent.pursuingEvent == null ? intent.selectEvent() : intent.pursuingEvent;
            if (event.conjunction[0].getPredicateName().equalsIgnoreCase("at")) {
                atIntentions.add(availableIntention);
            } else {
                nonAtIntentions.add(availableIntention);
            }
        }

        // the intention selection should not affect non-moving intentions,
        // so the prob of selection between moving and non-moving intentions are equal
        if (rand.nextInt(availableIntentions.size()) < atIntentions.size()) {
            ArrayList<Double> prob = new ArrayList<>(atIntentions.size());

            double totalWeight = 0;
            for (Integer atIntention : atIntentions) {
                // this express is for getting the dest location
                int[] dest = agent.intentions.get(atIntention).peek().pursuingEvent == null ?
                        agent.intentions.get(atIntention).peek().selectEvent().conjunction[0].getVariables() :
                        agent.intentions.get(atIntention).peek().pursuingEvent.conjunction[0].getVariables();
                double distance = Math.sqrt(Math.pow(currentLocation[0] - dest[0], 2) + Math.pow(currentLocation[1] - dest[1], 2));
                // inverse the distance to bias selection
                double weight = 1 / distance;
                prob.add(weight);
                totalWeight += weight;
            }

            double randomValue = rand.nextDouble() * totalWeight;
            double cumulativeWeight = 0.0;
            for (int i = 0; i < atIntentions.size(); i++) {
                cumulativeWeight += prob.get(i);
                if (randomValue <= cumulativeWeight) {
                    return atIntentions.get(i);
                }
            }
            // should not be reached
            return availableIntentions.get(rand.nextInt(availableIntentions.size()));
        } else {
            return nonAtIntentions.get(rand.nextInt(nonAtIntentions.size()));
        }
    }


    public double simulate() {
        RoverAgent agent = this.state.clone();

        LinkedList<Choice> simChoices = new LinkedList<>();

        while (!agent.finishAllGoals() && agent.isAgentActive()) {
            agent.updateIntentions();
            if (agent.reward < 0 || agent.finishAllGoals())
                break;

            // random intention selection
            if (agent.intentionChoice == -1 || agent.intentions.get(agent.intentionChoice).notAchievable()) {
                ArrayList<Integer> availableIntentionChoices = agent.getAvailableIntentionChoices();
//                agent.intentionChoice = availableIntentionChoices.
//                        get(rand.nextInt(availableIntentionChoices.size()));
                // biased moving intention selection
                agent.intentionChoice = biasedRandomIntentionSelection(availableIntentionChoices, agent);
            }
            Choice c = new Choice(agent.intentionChoice, -1);

            Intent pursingIntent = agent.intentions.get(agent.intentionChoice).peek();
            // plan selection if possible
            if (pursingIntent.pursuingEvent == null || pursingIntent.plan == null) {
                pursingIntent.pursuingEvent = pursingIntent.selectEvent();
                int[] planChoices = agent.getAvailablePlanIndexes(pursingIntent.pursuingEvent);
                int planChoice = planChoices[rand.nextInt(planChoices.length)];
                pursingIntent.plan = agent.getAvailablePlans(pursingIntent.pursuingEvent)[planChoice];
                c.planChoice = planChoice; // update plan choice as well
            }

            // stitch to the tail
            simChoices.addLast(c);

            if (pursingIntent.plan.peekNextStep().isAction()) {
                // perform action
                Action action = (Action) pursingIntent.plan.getNextStep();
                if (!action.simulationAct(agent))
                    return RewardMachine.NEGATIVE_INFINITY; // this simulation fails
            } else {
                if (!pursingIntent.plan.isFirstStep())
                    agent.intentionChoice = -1;
                // push the sub-goal to the top of the stack
                agent.intentions.get(agent.intentionChoice).push(pursingIntent.plan.getNextStep());
            }
        }

        // single-player mcts
        if (agent.reward > MCTSScheduler.bestSimResult) {
            //System.out.println("Best simulation reward so far: " + agent.reward);
            MCTSScheduler.bestSimResult = agent.reward;
            // stitch the previously selected choices
            for (int i = choices.size() - 1; i >= 0; i--) {
                simChoices.addFirst(choices.get(i));
            }
            // update the best simulation result
            MCTSScheduler.bestChoices = simChoices;
        }

        return agent.reward;
    }


    public void backPropagate(double result) {
        TreeNode node = this;
        double rSquared = result * result;
        while (node.parent != null) {
            node.totalReward += result;
            node.visits++;
            node.rewardSquares.add(rSquared);
            node = node.parent;
        }
        node.totalReward += result;
        node.visits++;
        node.rewardSquares.add(rSquared);
    }


    private double getUCTValue() {
        if (visits == 0)
            return Double.POSITIVE_INFINITY;

        double firstTerm = totalReward / visits;

//        tempUCT = firstTerm + SECOND_TERM_FACTOR * Math.sqrt(Math.log(parent.visits) / visits);
        tempUCT = firstTerm +
                SECOND_TERM_FACTOR * Math.sqrt(Math.log(parent.visits) / visits) +
                Math.sqrt(
                        (rewardSquares.stream().mapToDouble(Double::doubleValue).sum() - (visits * firstTerm * firstTerm) + D)
                                / visits);
        return tempUCT;
    }

}
