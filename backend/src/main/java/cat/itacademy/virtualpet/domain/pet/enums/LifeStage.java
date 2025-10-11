package cat.itacademy.virtualpet.domain.pet.enums;

/**
 * Represents the pet's current life stage.
 * Progression: BABY → ADULT → SENIOR
 * PASSED: terminal state when the pet has died.
 */
public enum LifeStage {
    BABY,
    ADULT,
    SENIOR,
    PASSED
}
