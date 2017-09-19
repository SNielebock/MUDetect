package de.tu_darmstadt.stg.mubench;

import com.google.common.collect.Multiset;
import de.tu_darmstadt.stg.mubench.cli.DetectionStrategy;
import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mudetect.MuDetect;
import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.mining.AUGMiner;
import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mustudies.UsageUtils;
import de.tu_darmstadt.stg.yaml.YamlObject;
import de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.tu_darmstadt.stg.mudetect.AlternativeViolationPredicate.firstAlternativeViolation;

abstract class MuDetectStrategy implements DetectionStrategy {

    abstract Collection<de.tu_darmstadt.stg.mudetect.aug.APIUsageExample> loadTrainingExamples(DetectorArgs args, DetectorOutput.Builder output) throws IOException;

    abstract AUGMiner createMiner();

    private Collection<APIUsageExample> loadDetectionTargets(DetectorArgs args) throws IOException {
        return new AUGBuilder(new DefaultAUGConfiguration()).build(args.getTargetPath().srcPath, args.getDependencyClassPath());
    }

    abstract MuDetect createDetector(Model model);

    @Override
    public DetectorOutput detectViolations(DetectorArgs args) throws Exception {
        DetectorOutput.Builder output = createOutput();

        long startTime = System.currentTimeMillis();
        Collection<APIUsageExample> trainingExamples = loadTrainingExamples(args, output);
        long endTrainingLoadTime = System.currentTimeMillis();
        output.withRunInfo("trainingLoadTime", endTrainingLoadTime - startTime);
        output.withRunInfo("numberOfTrainingExamples", trainingExamples.size());
        output.withRunInfo("numberOfUsagesInTrainingExamples", getTypeUsageCounts(trainingExamples));

        Model model = createMiner().mine(trainingExamples);
        long endTrainingTime = System.currentTimeMillis();
        output.withRunInfo("trainingTime", endTrainingTime - endTrainingLoadTime);
        output.withRunInfo("numberOfPatterns", model.getPatterns().size());
        output.withRunInfo("maxPatternSupport", model.getMaxPatternSupport());

        Collection<APIUsageExample> targets = loadDetectionTargets(args);
        long endDetectionLoadTime = System.currentTimeMillis();
        output.withRunInfo("detectionLoadTime", endDetectionLoadTime - endTrainingTime);
        output.withRunInfo("numberOfTargets", targets.size());

        List<Violation> violations = createDetector(model).findViolations(targets).stream()
                .filter(firstAlternativeViolation()).collect(Collectors.toList());
        long endDetectionTime = System.currentTimeMillis();
        output.withRunInfo("detectionTime", endDetectionTime - endDetectionLoadTime);
        output.withRunInfo("numberOfViolations", violations.size());
        output.withRunInfo("numberOfExploredAlternatives", AlternativeMappingsOverlapsFinder.numberOfExploredAlternatives);

        return output.withFindings(violations, ViolationUtils::toFinding);
    }

    private YamlObject getTypeUsageCounts(Collection<APIUsageExample> targets) {
        YamlObject object = new YamlObject();
        for (Multiset.Entry<String> entry : UsageUtils.countNumberOfUsagesPerType(targets).entrySet()) {
            object.put(entry.getElement(), entry.getCount());
        }
        return object;
    }
}