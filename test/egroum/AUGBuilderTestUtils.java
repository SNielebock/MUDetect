package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;

import static egroum.EGroumTestUtils.buildGroumForMethod;

class AUGBuilderTestUtils {
    static AUG buildAUG(String code) {
        return AUGBuilder.toAUG(buildGroumForMethod(code));
    }
}
