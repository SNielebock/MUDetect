package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Before;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.OverlapsFinderTestUtils.findsOverlaps;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.instance;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.junit.Assert.assertThat;

public class ConditionMappingTest {
    private AlternativeMappingsOverlapsFinder overlapsFinder;

    @Before
    public void setUp() throws Exception {
        overlapsFinder = new AlternativeMappingsOverlapsFinder(
                new AlternativeMappingsOverlapsFinder.Config() {{
                    nodeMatcher = new EquallyLabelledNodeMatcher();
                    matchEntireConditions = true;
                }});
    }

    @Test
    public void mapsEqualConditions() throws Exception {
        TestAUGBuilder target = buildAUG().withActionNodes("A", "<").withDataNodes("int", "long")
                .withCondEdge("<", "sel", "A").withDataEdge("int", PARAMETER, "<").withDataEdge("long", PARAMETER, "<");
        Overlap instance = instance(target);

        assertThat(overlapsFinder, findsOverlaps(target, target, instance));
    }

    @Test
    public void skipsEntireConditionOnDifferentArguments() throws Exception {
        TestAUGBuilder target = buildAUG().withActionNodes("A", "<").withDataNodes("int", "float")
                .withCondEdge("<", "sel", "A").withDataEdge("int", PARAMETER, "<").withDataEdge("float", PARAMETER, "<");
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "<").withDataNodes("int", "long")
                .withCondEdge("<", "sel", "A").withDataEdge("int", PARAMETER, "<").withDataEdge("long", PARAMETER, "<");
        Overlap overlap = buildOverlap(target, pattern).withNode("A").build();

        assertThat(overlapsFinder, findsOverlaps(target, pattern, overlap));
    }
}
