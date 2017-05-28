package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MissingElementViolationPredicateTest {
    @Test
    public void fullInstance() throws Exception {
        Overlap overlap = buildOverlap(buildAUG().withActionNode(":action:")).withNode(":action:").build();

        ViolationPredicate strategy = new MissingElementViolationPredicate();
        assertFalse(strategy.isViolation(overlap));
    }

    @Test
    public void missingNode() throws Exception {
        Overlap overlap = buildOverlap(buildAUG().withActionNode(":action:")).build();

        MissingElementViolationPredicate strategy = new MissingElementViolationPredicate();
        assertTrue(strategy.isViolation(overlap));
    }

    @Test
    public void missingEdge() throws Exception {
        final TestAUGBuilder builder = buildAUG().withActionNodes(":a1:", ":a2:").withDataEdge(":a1:", ORDER, ":a2:");
        Overlap overlap = buildOverlap(builder).withNode(":a1:").withNode(":a2:").build();

        MissingElementViolationPredicate strategy = new MissingElementViolationPredicate();
        assertTrue(strategy.isViolation(overlap));
    }
}