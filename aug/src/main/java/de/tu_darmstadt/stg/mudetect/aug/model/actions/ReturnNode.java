package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;

public class ReturnNode extends BaseNode implements ActionNode {
    public ReturnNode() {}

    public ReturnNode(int sourceLineNumber) {
        super(sourceLineNumber);
    }

    @Override
    public String getLabel() {
        return "return";
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
