"use client"

import { useState } from "react"
import Image from "next/image"
import { toast } from "sonner"
import { Utensils, Sparkles, Gamepad2, AlertTriangle } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { StatBar } from "@/components/stat-bar"
import { usePetsStore } from "@/lib/stores/pets-store"
import type { PetResponse } from "@/lib/types"
import { getPetImageUrl, getPetImageAlt } from "@/lib/pet-images"

interface PetCardProps {
  pet: PetResponse
  onViewDetails?: (id: number) => void
  readOnly?: boolean
}

const BREED_LABELS: Record<string, string> = {
  DALMATIAN: "Dalmatian",
  GOLDEN_RETRIEVER: "Golden Retriever",
  LABRADOR: "Labrador",
}

const STAGE_LABELS: Record<string, string> = {
  BABY: "Baby",
  ADULT: "Adult",
  SENIOR: "Senior",
  PASSED: "Passed",
}

export function PetCard({ pet, onViewDetails, readOnly = false }: PetCardProps) {
  const { act, fetchAll } = usePetsStore()
  const [actionLoading, setActionLoading] = useState<string | null>(null)
  const [imageError, setImageError] = useState(false)
  const [warnings, setWarnings] = useState<string[]>([])

  const hasWarnings = warnings.length > 0
  const isStarving = warnings.includes("hunger_high")
  const isLowHygiene = warnings.includes("hygiene_low")
  const isLowFun = warnings.includes("fun_low")

  const handleAction = async (action: "feed" | "wash" | "play") => {
    if (pet.dead || pet.lifeStage === "PASSED") {
      toast.error(`${pet.name} has passed away and cannot perform actions`)
      return
    }

    setActionLoading(action)
    try {
      const response = await act(pet.id, action)

      setWarnings(response.warnings || [])

      if (response.dead || response.lifeStage === "PASSED") {
        toast.error(response.message || `${pet.name} has passed away...`, {
          description: "Rest in peace, dear friend.",
          duration: 5000,
        })
      } else {
        if (response.warnings && response.warnings.length > 0) {
          const warningMessages = response.warnings.map((w) => {
            if (w === "hunger_high") return "Hunger is getting high!"
            if (w === "hygiene_low") return "Hygiene is getting low!"
            if (w === "fun_low") return "Fun is getting low!"
            return w
          })
          toast.warning(`${pet.name} needs attention!`, {
            description: warningMessages.join(" "),
            duration: 4000,
          })
        } else {
          toast.success(
            `${pet.name} has been ${action === "feed" ? "fed" : action === "wash" ? "washed" : "played with"}!`,
          )
        }
      }
    } catch (error: any) {
      console.error("[v0] Action error:", error.response?.status, error.response?.data)

      await fetchAll()

      const errorMessage = error.response?.data?.message || ""

      if (errorMessage.includes("not hungry") || errorMessage.includes("PetNotHungryException")) {
        toast.info(`${pet.name} is not hungry right now!`)
      } else if (errorMessage.includes("already clean") || errorMessage.includes("PetAlreadyCleanException")) {
        toast.info(`${pet.name} is already clean!`)
      } else if (errorMessage.includes("too happy") || errorMessage.includes("PetTooHappyException")) {
        toast.info(`${pet.name} is already very happy!`)
      } else if (errorMessage.includes("deceased") || errorMessage.includes("PetDeceasedException")) {
        toast.error(`${pet.name} has passed away`, {
          description: "This pet can no longer perform actions.",
        })
      } else if (error.response?.status === 500) {
        toast.error(`Something went wrong with ${pet.name}`, {
          description: "Please refresh the page to see the latest status.",
          duration: 6000,
        })
      } else {
        toast.error(errorMessage || `Failed to ${action}`)
      }
    } finally {
      setActionLoading(null)
    }
  }

  const getHungerColor = (value: number): "success" | "warning" | "danger" => {
    if (value <= 30) return "success"
    if (value <= 70) return "warning"
    return "danger"
  }

  const getStatColor = (value: number): "success" | "warning" | "danger" => {
    if (value >= 70) return "success"
    if (value >= 40) return "warning"
    return "danger"
  }

  const imageUrl = getPetImageUrl(pet.breed, pet.lifeStage)
  const imageUrlWithCache =
    pet.lifeStage === "PASSED"
      ? `${imageUrl}?stage=passed&t=${pet.deathAt ? new Date(pet.deathAt).getTime() : Date.now()}`
      : imageUrl
  const isPassed = pet.dead || pet.lifeStage === "PASSED"

  return (
    <Card className="overflow-hidden transition-all hover:shadow-xl hover:scale-105 border-2">
      <CardHeader className="space-y-3 pb-4">
        <div className="flex justify-center mb-2">
          <div className="relative w-48 h-48 flex items-center justify-center bg-gradient-to-br from-pink-50 to-purple-50 rounded-3xl shadow-lg overflow-hidden">
            <Image
              key={`${pet.id}-${pet.lifeStage}-${pet.dead}`}
              src={imageError ? "/placeholder.svg?height=200&width=200" : imageUrlWithCache}
              alt={getPetImageAlt(pet.breed, pet.lifeStage, pet.name)}
              fill
              className="object-contain p-4"
              unoptimized
              onError={() => {
                console.log(
                  "[v0] Image failed to load:",
                  imageUrlWithCache,
                  "breed:",
                  pet.breed,
                  "stage:",
                  pet.lifeStage,
                  "dead:",
                  pet.dead,
                )
                setImageError(true)
              }}
              onLoad={() => {
                console.log("[v0] Image loaded successfully:", imageUrlWithCache, "stage:", pet.lifeStage)
              }}
            />
          </div>
        </div>

        <div className="flex items-start justify-between">
          <CardTitle className="text-2xl font-bold">{pet.name}</CardTitle>
          <Badge variant={isPassed ? "destructive" : "secondary"} className="text-sm font-semibold">
            {STAGE_LABELS[pet.lifeStage]}
          </Badge>
        </div>
        <p className="text-sm text-muted-foreground font-medium">{BREED_LABELS[pet.breed]}</p>
      </CardHeader>

      <CardContent className="space-y-4">
        {!isPassed && (
          <>
            {hasWarnings && (
              <Alert variant="destructive" className="mb-3">
                <AlertTriangle className="h-4 w-4" />
                <AlertDescription className="text-xs font-semibold">
                  {isStarving && "WARNING: Hunger is getting high! "}
                  {isLowHygiene && "WARNING: Hygiene is getting low! "}
                  {isLowFun && "WARNING: Fun is getting low!"}
                </AlertDescription>
              </Alert>
            )}

            <div className="space-y-3">
              <StatBar label="Hunger" value={pet.hunger} color={getHungerColor(pet.hunger)} />
              <StatBar label="Hygiene" value={pet.hygiene} color={getStatColor(pet.hygiene)} />
              <StatBar label="Fun" value={pet.fun} color={getStatColor(pet.fun)} />
            </div>

            {!readOnly && (
              <div className="grid grid-cols-3 gap-2 pt-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleAction("feed")}
                  disabled={actionLoading !== null || isPassed}
                  className="flex flex-col gap-1 h-auto py-3 hover:scale-105 transition-transform"
                >
                  <Utensils className="h-5 w-5" />
                  <span className="text-xs font-semibold">Feed</span>
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleAction("wash")}
                  disabled={actionLoading !== null || isPassed}
                  className="flex flex-col gap-1 h-auto py-3 hover:scale-105 transition-transform"
                >
                  <Sparkles className="h-5 w-5" />
                  <span className="text-xs font-semibold">Wash</span>
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleAction("play")}
                  disabled={actionLoading !== null || isPassed}
                  className="flex flex-col gap-1 h-auto py-3 hover:scale-105 transition-transform"
                >
                  <Gamepad2 className="h-5 w-5" />
                  <span className="text-xs font-semibold">Play</span>
                </Button>
              </div>
            )}
          </>
        )}

        {isPassed && (
          <div className="text-center py-4 space-y-2">
            <p className="text-sm text-muted-foreground italic">In loving memory of {pet.name}</p>
            {pet.deathAt && (
              <p className="text-xs text-muted-foreground">{new Date(pet.deathAt).toLocaleDateString()}</p>
            )}
          </div>
        )}

        {onViewDetails && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onViewDetails(pet.id)}
            className="w-full hover:scale-105 transition-transform"
          >
            View Details
          </Button>
        )}
      </CardContent>
    </Card>
  )
}
