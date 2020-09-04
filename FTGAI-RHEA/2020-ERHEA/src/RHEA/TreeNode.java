package RHEA;

import java.util.ArrayList;
import java.util.Random;

import static RHEA.utils.Constants.epsilon;
import RHEA.utils.Operations;

/**
 * Created by Raluca on 19-Aug-16.
 */
public class TreeNode {

    private TreeNode parent;
    private TreeNode[] children;

    public double value;
    public int nVisits;
    private int m_depth;
    private int idx;
    int childIdx;
    boolean chosen;

    private static int count = 0;
    static String[] actColors = new String[]{"red", "blue", "black", "green", "orange", "purple"};

    private Random gen;

    TreeNode(TreeNode parent, int length, double value, int childIdx, Random generator) {
        this.parent = parent;
        this.value = value;
        chosen = false;
        nVisits = 1;
        if(parent != null)
            m_depth = parent.m_depth+1;
        else {
            m_depth = 0;
            count = 0;
        }
        this.childIdx = childIdx;
        children = new TreeNode[length];
        idx = count++;
        gen = generator;
    }

    private void addChild(int action, double value) {
        if (children[action] == null)
            children[action] = new TreeNode(this, children.length, value, action, gen);
        else {
            children[action].nVisits++;
            children[action].value += value; // - children[action].value) / children[action].nVisits;
        }
    }

    private TreeNode getChild(int action) {
        return children[action];
    }

    public void rollout (int[] individual, double value) {
        TreeNode current = this;
        for (int i = 0; i < individual.length; i++) {
            current.addChild(individual[i], value);
            current = current.getChild(individual[i]);
        }
    }

    public void markChosen (int[] individual) {
        TreeNode cur = this;
        for (int i = 0; i < individual.length; i++) {
            cur = cur.children[individual[i]];
            cur.chosen = true;
        }
    }

    int getBestChild() {
        // get max value of children of node
        double bestValue = -Double.MAX_VALUE;
        int bestChild = -1;

        for (int i = 0; i < children.length; i++) {
            TreeNode n = children[i];
            if (n != null) {
                double childValue = n.value / (n.nVisits + epsilon);
                childValue = Operations.noise(childValue, epsilon, gen.nextDouble());
                if (childValue > bestValue) {
                    bestValue = childValue;
                    bestChild = i;
                }
            }
        }

        return bestChild;
    }

    int getMostVisitedChild() {
        // get max value of children of node
        int mostVisits = 0;
        int bestChild = -1;

        for (int i = 0; i < children.length; i++) {
            TreeNode n = children[i];
            if (n != null) {
                if (n.nVisits > mostVisits) {
                    mostVisits = n.nVisits;
                    bestChild = i;
                }
            }
        }

        return bestChild;
    }

    TreeNode shiftTree(int lastAct, double discount) {

        // discount tree values
        ArrayList<TreeNode> treeNodes = traverse(this);
        for (TreeNode tn : treeNodes) {
            if (discount < 1) {
                tn.value *= discount;
            }
            tn.m_depth--;
        }

        this.children[lastAct].parent = null;
        return this.children[lastAct];
    }


    private boolean hasChildren() {
        for (TreeNode aChildren : children) {
            if (aChildren != null) return true;
        }
        return false;
    }
    private TreeNode getFirstChild() {
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) return children[i];
        }
        return null;
    }
    private TreeNode getNextSibling() {
        if (parent != null) {
            int index = -1;
            for (int i = 0; i < parent.children.length; i++) {
                if (parent.children[i] == null) continue;
                if (parent.children[i].equals(this)) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                for (int i = index + 1; i < parent.children.length; i++) {
                    if (parent.children[i] != null) return parent.children[i];
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        int idxp = parent == null? -1 : parent.idx;
//        return "[i=" + idx + ", q=" + value + ", n=" + nVisits + ", p=" + idxp + ", d=" + m_depth + "]";
//        return "[" + idx + "," + String.format("%.2f", value) + "," + nVisits + "," + idxp + "]";
//        return "[" + idx + "," + String.format("%.2f", value) + "," + nVisits + "]";
        return "[" + String.format("%.2f", value / (nVisits + epsilon)) + "]";
//        return ""+nVisits;
    }

    String treeToString() {
        ArrayList<String> treeLevels = new ArrayList<>();
        ArrayList<TreeNode> treeNodes = traverse (this);

        String tree = "----" + treeNodes.size() + "-----\n";

        for (TreeNode tn : treeNodes) {
            String hex = Integer.toHexString((int)Math.round(tn.nVisits*1.7));
            tree += "A.node_attr['fillcolor']='#000000" + hex + "'" + "\n";
            if (tn.chosen) {
                tree += "A.node_attr['color']='red'\n";
            } else {
                tree += "A.node_attr['color']='black'\n";
            }
            tree += "A.add_node(\"" + tn.idx + "\", label=\"" + tn.toString() + "\")" + "\n";
        }

        for (TreeNode tn : treeNodes) {
            for (TreeNode child : tn.children) {
                if (child != null) {
                    tree += "A.add_edge(\"" + tn.idx + "\", \"" + child.idx + "\", color=\"" + actColors[child.childIdx] + "\")" + "\n";
                }
            }
        }

//        for (TreeNode node : treeNodes) {
//            if (node.m_depth <= 0) { //root node
//                treeLevels.add(node.toString());
//            } else {
//                if (node.m_depth < treeLevels.size()) {
//                    String news = treeLevels.get(node.m_depth);
//                    news += " " + node.toString();
//                    treeLevels.set(node.m_depth, news);
//                } else {
//                    treeLevels.add(node.toString());
//                }
//            }
//        }
//
//
//        for (String s : treeLevels)
//            tree += treeLevels.indexOf(s) + ": " + s + "\n";
//
        tree += "---------\n";

        return tree;
    }

    private ArrayList<TreeNode> traverse(TreeNode rootNode) {

        ArrayList<TreeNode> nodes = new ArrayList<>();

        TreeNode node = rootNode;

        while (node != null) {

            // do stuff with node
            nodes.add(node);

            // move to next node
            if (node.hasChildren()) {
                node = node.getFirstChild();
            }
            else {    // leaf
                // find the parent level
                while (node != null && !node.equals(rootNode) && node.getNextSibling() == null) {
                    // use child-parent link to get to the parent level
                    node = node.parent;
                }

                if (node != null) {
                    node = node.getNextSibling();
                }
            }
        }

        return nodes;
    }

    @Override
    public boolean equals(Object obj) {
        TreeNode node = (TreeNode)obj;
        return node.idx == this.idx;
    }
}
