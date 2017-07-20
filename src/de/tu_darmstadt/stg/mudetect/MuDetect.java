package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.*;
import egroum.EGroumNode;
import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.mining.Pattern;

import java.util.*;
import java.util.stream.Collectors;

public class MuDetect {

    private final Model model;
    private final OverlapsFinder overlapsFinder;
    private final ViolationPredicate violationPredicate;
    private final ViolationRankingStrategy rankingStrategy;
    private final AlternativePatternInstancePredicate alternativePatternInstancePredicate;

    public MuDetect(Model model,
                    OverlapsFinder overlapsFinder,
                    ViolationPredicate violationPredicate,
                    ViolationRankingStrategy rankingStrategy) {
        this.model = model;
        this.overlapsFinder = overlapsFinder;
        this.violationPredicate = violationPredicate;
        this.rankingStrategy = rankingStrategy;
        // SMELL this is untested behaviour, because it's very hard to test in this context. Can we separate it?
        this.alternativePatternInstancePredicate = new AlternativePatternInstancePredicate();
    }

    public List<Violation> findViolations(Collection<AUG> targets) {
        final Overlaps overlaps = findOverlaps(targets, model.getPatterns());
        overlaps.removeViolationIf(violation -> isAlternativePatternInstance(violation, overlaps));
        List<Violation> violations = rankingStrategy.rankViolations(overlaps, model);
        filterAlternativeViolations(violations);
        return violations;
    }

    // REFACTOR such filtering should be configurable and the detector shouldn't know about the specific strategy
    private void filterAlternativeViolations(List<Violation> violations) {
        Set<EGroumNode> converedNodes = new HashSet<>();
        Iterator<Violation> iterator = violations.iterator();
        while (iterator.hasNext()) {
            Overlap violation = iterator.next().getOverlap();
            Set<EGroumNode> mappedTargetNodes = violation.getMappedTargetNodes().stream()
                    .filter(node -> node.getLabel().endsWith("<init>") || node.getLabel().endsWith("()"))
                    .collect(Collectors.toSet());
            if (mappedTargetNodes.stream().anyMatch(converedNodes::contains)) {
                iterator.remove();
            } else {
                converedNodes.addAll(mappedTargetNodes);
            }
        }
    }

    private Overlaps findOverlaps(Collection<AUG> targets, Set<Pattern> patterns) {
        Overlaps overlaps = new Overlaps();
        for (AUG target : targets) {
            for (Pattern pattern : patterns) {
                for (Overlap overlap : overlapsFinder.findOverlaps(target, pattern)) {
                    if (violationPredicate.apply(overlap).orElse(false)) {
                        overlaps.addViolation(overlap);
                    } else {
                        overlaps.addInstance(overlap);
                    }
                }
            }
        }
        return overlaps;
    }

    private boolean isAlternativePatternInstance(Overlap violation, Overlaps overlaps) {
        Set<Overlap> instancesInViolationTarget = overlaps.getInstancesInSameTarget(violation);
        return alternativePatternInstancePredicate.test(violation, instancesInViolationTarget);
    }
}

