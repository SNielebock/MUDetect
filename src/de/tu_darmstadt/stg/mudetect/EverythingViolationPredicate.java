package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.Optional;

public class EverythingViolationPredicate implements ViolationPredicate {
    @Override
    public Optional<Boolean> isViolation(Overlap overlap) {
        return Optional.of(true);
    }
}
