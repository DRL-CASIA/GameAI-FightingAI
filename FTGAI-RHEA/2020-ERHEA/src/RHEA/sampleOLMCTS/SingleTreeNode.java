package RHEA.sampleOLMCTS;

import RHEA.utils.ElapsedCpuTimer;

import enumerate.Action;

import java.util.ArrayList;
import java.util.Random;

import RHEA.utils.GeneralInformation;
import RHEA.utils.Operations;
import struct.FrameData;

public class SingleTreeNode
{
    private final double HUGE_NEGATIVE = -10000000.0;
    private final double HUGE_POSITIVE =  10000000.0;
    public double epsilon = 1e-7; //  
    public double egreedyEpsilon = 0.05;
    public SingleTreeNode parent;
    public SingleTreeNode[] children;
    public double totValue;
    public int nVisits;
    public Random m_rnd;
    public int m_depth;
    protected double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};
    public int childIdx;

    public int num_actions;
    Action[] actions;
    public int ROLLOUT_DEPTH = 10;
    public double K = Math.sqrt(2);

    public static GeneralInformation rootGI;
    static int count;
    int MAX_NUM_CALLS;

    public SingleTreeNode(Random rnd, int num_actions, Action[] actions) {
        this(null, -1, rnd, num_actions, actions);
    }

    public SingleTreeNode(SingleTreeNode parent, int childIdx, Random rnd, int num_actions,  Action[] actions) {
        this.parent = parent;
        this.m_rnd = rnd;
        this.num_actions = num_actions;
        this.actions = actions;
        children = new SingleTreeNode[num_actions];
        totValue = 0.0;
        this.childIdx = childIdx;
        if(parent != null)
            m_depth = parent.m_depth+1;
        else
            m_depth = 0;
    }


    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {

    	FrameData o_fd = rootGI.getFrameData();
        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;
        int remainingLimit = 5;
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
            //while(numIters < Agent.MCTS_ITERATIONS){
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            SingleTreeNode selected = treePolicy(rootGI);
            double delta = selected.rollOut(rootGI,0);
            backUp(selected, delta);

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();

            rootGI.setFrameData(o_fd);
        }
    }

    public void mctsSearchCalls(int numCalls, int depth) {
    	FrameData o_fd = rootGI.getFrameData();
        ROLLOUT_DEPTH = depth;
        MAX_NUM_CALLS = numCalls;

        int lastCount = -1;
        count = 0;
        int numIters = 0;
        int remainingLimit = 5;
        while((count - lastCount) > 0 && (count + ROLLOUT_DEPTH) < MAX_NUM_CALLS){
            //while(numIters < Agent.MCTS_ITERATIONS){
            lastCount = count;
            //ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            SingleTreeNode selected = treePolicy(rootGI);
            double delta = selected.rollOut(rootGI, MAX_NUM_CALLS);
            backUp(selected, delta);

            numIters++;
//            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
//            avgTimeTaken  = acumTimeTaken/numIters;
//            remaining = elapsedTimer.remainingTimeMillis();
            
            rootGI.setFrameData(o_fd);
        }
    }

    public SingleTreeNode treePolicy(GeneralInformation gi) {

        SingleTreeNode cur = this;

        while (!gi.IsGameOver() && cur.m_depth < ROLLOUT_DEPTH)
        {
            if (cur.notFullyExpanded()) {
                return cur.expand(gi);

            } else {
                SingleTreeNode next = cur.uct(gi);
                cur = next;
            }
        }

        return cur;
    }


    public SingleTreeNode expand(GeneralInformation gi) {
         
        int bestAction = 0;
        double bestValue = -1;

        for (int i = 0; i < children.length; i++) {
            double x = m_rnd.nextDouble();
            if (x > bestValue && children[i] == null) {
                bestAction = i;
                bestValue = x;
            }
        }
      
        
        //Roll the state
        gi.advanceOneStep(actions[bestAction], null);
        count++;

        SingleTreeNode tn = new SingleTreeNode(this,bestAction,this.m_rnd,num_actions, actions);
        children[bestAction] = tn;
        return tn;
    }

    public SingleTreeNode uct(GeneralInformation gi) {

        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        for (SingleTreeNode child : this.children)
        {
            double hvVal = child.totValue;
            double childValue =  hvVal / (child.nVisits + this.epsilon);

            childValue =  Operations.normalise(childValue, bounds[0], bounds[1]);   
            //System.out.println("norm child value: " + childValue);

            double uctValue = childValue +
                    K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon));

            uctValue =  Operations.noise(uctValue, this.epsilon, this.m_rnd.nextDouble()); //break ties randomly
            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }
        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length + " " +
                    + bounds[0] + " " + bounds[1]);
        }

        //Roll the state:
        gi.advanceOneStep(actions[selected.childIdx], null);
        count++;

        return selected;
    }


    public double rollOut(GeneralInformation gi, int numCalls)
    {
        int thisDepth = this.m_depth;

        while (!finishRollout( gi,thisDepth, numCalls)) {

            int action = m_rnd.nextInt(num_actions);
            gi.advanceOneStep(actions[action], null);
            count++;
            thisDepth++;
        }


        double delta = value(gi);

        if(delta < bounds[0])
            bounds[0] = delta;
        if(delta > bounds[1])
            bounds[1] = delta;

        //double normDelta = Utils.normalise(delta ,lastBounds[0], lastBounds[1]);

        return delta;
    }

    public double value(GeneralInformation gi) {

        boolean gameOver = gi.IsGameOver();
        boolean win = gi.IsWin();
        double rawScore = gi.getHPScore();

        if(gameOver && !win)
            rawScore += HUGE_NEGATIVE;

        if(gameOver && win  )
            rawScore += HUGE_POSITIVE;

        return rawScore;
    }

    public boolean finishRollout(GeneralInformation rollGI, int depth, int numCalls)
    {
        if (count >= numCalls)
            return true;

        if(depth >= ROLLOUT_DEPTH)      //rollout end condition.
            return true;

        if(rollGI.IsGameOver())               //end of game
            return true;

        return false;
    }

    public void backUp(SingleTreeNode node, double result)
    {
        SingleTreeNode n = node;
        while(n != null)
        {
            n.nVisits++;
            n.totValue += result;
            n = n.parent;
        }
    }


    public int mostVisitedAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null)
            {
                if(first == -1)
                    first = children[i].nVisits;
                else if(first != children[i].nVisits)
                {
                    allEqual = false;
                }

                double childValue = children[i].nVisits;
                childValue = Operations.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }else if(allEqual)
        {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }

        return selected;
    }

    public ArrayList<Integer> mostVisitedActions(SingleTreeNode root) {

        ArrayList<Integer> actions = new ArrayList<>();
        int MIN_VISITS = 3;

        SingleTreeNode current = root;

        while (true) {
            int selected = -1;
            double bestValue = -Double.MAX_VALUE;
            boolean allEqual = true;
            double first = -1;

            if (current == null || current.notFullyExpanded() && current.nVisits < MIN_VISITS) {
                break;
            }

            for (int i = 0; i < children.length; i++) {

                if (children[i] != null) {
                    if (first == -1)
                        first = children[i].nVisits;
                    else if (first != children[i].nVisits) {
                        allEqual = false;
                    }

                    double childValue = children[i].nVisits;
                    childValue = Operations.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                    if (childValue > bestValue) {
                        bestValue = childValue;
                        selected = i;
                    }
                }
            }

            if (selected == -1) {
                System.out.println("Unexpected selection!");
                selected = 0;
            } else if (allEqual) {
                //If all are equal, we opt to choose for the one with the best Q.
                selected = bestAction(current);
            }
            current = current.children[selected];

            actions.add(selected);
        }
        return actions;
    }

    public int bestAction()
    {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null) {
                //double tieBreaker = m_rnd.nextDouble() * epsilon;
                double childValue = children[i].totValue / (children[i].nVisits + this.epsilon);
                childValue = Operations.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }

        return selected;
    }

    public int bestAction(SingleTreeNode n)
    {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i=0; i<n.children.length; i++) {

            if(n.children[i] != null) {
                //double tieBreaker = m_rnd.nextDouble() * epsilon;
                double childValue = n.children[i].totValue / (n.children[i].nVisits + this.epsilon);
                childValue = Operations.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }

        return selected;
    }


    public boolean notFullyExpanded() {
        for (SingleTreeNode tn : children) {
            if (tn == null) {
                return true;
            }
        }

        return false;
    }
}
