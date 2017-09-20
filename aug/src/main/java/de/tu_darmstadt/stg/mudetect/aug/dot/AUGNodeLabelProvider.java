package de.tu_darmstadt.stg.mudetect.aug.dot;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.Node;
import org.jgrapht.ext.VertexNameProvider;

import java.util.Optional;

public class AUGNodeLabelProvider implements VertexNameProvider<Node> {
    @Override
    public String getVertexName(Node node) {
        StringBuilder label = new StringBuilder(node.getLabel());
        APIUsageGraph graph = node.getGraph();
        if (graph instanceof APIUsageExample) {
            Optional<Integer> sourceLineNumber = ((APIUsageExample) graph).getSourceLineNumber(node);
            sourceLineNumber.ifPresent(lineNumber -> label.append(" L").append(lineNumber));
        }
        return label.toString();
    }
}
