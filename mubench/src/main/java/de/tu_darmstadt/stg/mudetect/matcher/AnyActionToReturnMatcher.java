package de.tu_darmstadt.stg.mudetect.matcher;

import de.tu_darmstadt.stg.mudetect.aug.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.Node;

/**
 * When a pattern contains a return (action) node, we interpret this to say that the returned value, i.e., the data node
 * that is a parameter to the return node, should be used <i>somehow</i> (as opposed to dropped). To reflect this idea
 * in the detection, we match any action node taking the respective parameter to the return node in the pattern.
 */
public class AnyActionToReturnMatcher implements NodeMatcher {
    @Override
    public boolean test(Node targetNode, Node patternNode) {
        return targetNode instanceof ActionNode && patternNode.getLabel().equals("return");
    }
}