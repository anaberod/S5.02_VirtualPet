import type { Breed, LifeStage } from "./types"

export function getPetImageUrl(breed: Breed, lifeStage: LifeStage): string {
  const breedMap: Record<Breed, string> = {
    GOLDEN_RETRIEVER: "golden",
    DALMATIAN: "dalmatian",
    LABRADOR: "labrador",
  }

  const breedKey = breedMap[breed]
  const stageKey = lifeStage.toLowerCase()

  // Images are named like: golden_baby.png, dalmatian_adult.png, labrador_passed.png, etc.
  return `/pets/${breedKey}_${stageKey}.png`
}

// Alt text helper
export function getPetImageAlt(breed: Breed, lifeStage: LifeStage, name: string): string {
  const breedLabels: Record<Breed, string> = {
    DALMATIAN: "Dalmatian",
    GOLDEN_RETRIEVER: "Golden Retriever",
    LABRADOR: "Labrador",
  }

  const stageLabels: Record<LifeStage, string> = {
    BABY: "baby",
    ADULT: "adult",
    SENIOR: "senior",
    PASSED: "passed away",
  }

  return `${name} - ${stageLabels[lifeStage]} ${breedLabels[breed]}`
}
