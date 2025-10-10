import type { Breed, LifeStage } from "./types"

export function getPetImageUrl(breed: Breed, lifeStage: LifeStage): string {
  const imageQueries: Record<string, Record<string, string>> = {
    DALMATIAN: {
      BABY: "cute kawaii baby dalmatian puppy with black spots, chibi anime style, pastel colors, round face, big sparkling eyes, adorable happy expression, soft shading",
      ADULT:
        "cute kawaii adult dalmatian dog with black spots, chibi anime style, pastel colors, friendly smile, big sparkling eyes, playful pose, soft shading",
      SENIOR:
        "cute kawaii senior dalmatian dog with black spots and gray muzzle, chibi anime style, pastel colors, wise gentle expression, big sparkling eyes, soft shading",
    },
    GOLDEN_RETRIEVER: {
      BABY: "cute kawaii baby golden retriever puppy, fluffy cream yellow fur, chibi anime style, pastel colors, round face, big sparkling eyes, adorable expression, soft shading",
      ADULT:
        "cute kawaii adult golden retriever dog, fluffy golden yellow fur, chibi anime style, pastel colors, happy smile, big sparkling eyes, energetic pose, soft shading",
      SENIOR:
        "cute kawaii senior golden retriever dog with white muzzle, fluffy golden fur, chibi anime style, pastel colors, gentle expression, big sparkling eyes, soft shading",
    },
    LABRADOR: {
      BABY: "cute kawaii baby chocolate brown labrador puppy, chibi anime style, pastel colors, round face, big sparkling eyes, playful adorable expression, soft shading",
      ADULT:
        "cute kawaii adult chocolate brown labrador dog, chibi anime style, pastel colors, happy smile, big sparkling eyes, active pose, soft shading",
      SENIOR:
        "cute kawaii senior chocolate brown labrador dog with gray muzzle, chibi anime style, pastel colors, calm wise expression, big sparkling eyes, soft shading",
    },
  }

  const query = imageQueries[breed]?.[lifeStage] || imageQueries.LABRADOR.BABY
  return `/placeholder.svg?height=300&width=300&query=${encodeURIComponent(query)}`
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
  }

  return `${name} - ${stageLabels[lifeStage]} ${breedLabels[breed]}`
}
