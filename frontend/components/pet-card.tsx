"use client"

import { useState } from "react"
import type { ReactElement } from "react"
import { toast } from "sonner"
import { Utensils, Sparkles, Gamepad2 } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { StatBar } from "@/components/stat-bar"
import { usePetsStore } from "@/lib/stores/pets-store"
import type { PetResponse } from "@/lib/types"

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
}

function getPetIllustration(breed: string, stage: string): ReactElement {
  const breedKey = breed.toLowerCase()
  const stageKey = stage.toLowerCase()

  // Color schemes for each breed
  const colors: Record<string, { primary: string; secondary: string; spots?: string }> = {
    dalmatian: { primary: "#ffffff", secondary: "#f0f0f0", spots: "#2d2d2d" },
    golden_retriever: { primary: "#f5d98f", secondary: "#f0c870" },
    labrador: { primary: "#8b6f47", secondary: "#6b5437" },
  }

  const color = colors[breedKey] || colors.labrador
  const size = stageKey === "baby" ? "80" : stageKey === "adult" ? "100" : "90"

  return (
    <svg viewBox="0 0 120 120" className="w-full h-full">
      {/* Dog face */}
      <circle cx="60" cy="60" r={size === "80" ? "35" : size === "100" ? "40" : "38"} fill={color.primary} />

      {/* Ears */}
      <ellipse cx="35" cy="45" rx="15" ry="25" fill={color.secondary} />
      <ellipse cx="85" cy="45" rx="15" ry="25" fill={color.secondary} />

      {/* Eyes */}
      <circle cx="50" cy="55" r="5" fill="#2d2d2d" />
      <circle cx="70" cy="55" r="5" fill="#2d2d2d" />
      <circle cx="51" cy="54" r="2" fill="#ffffff" />
      <circle cx="71" cy="54" r="2" fill="#ffffff" />

      {/* Nose */}
      <ellipse cx="60" cy="68" rx="6" ry="5" fill="#2d2d2d" />

      {/* Mouth */}
      <path d="M 60 68 Q 55 75 50 73" stroke="#2d2d2d" strokeWidth="2" fill="none" strokeLinecap="round" />
      <path d="M 60 68 Q 65 75 70 73" stroke="#2d2d2d" strokeWidth="2" fill="none" strokeLinecap="round" />

      {/* Dalmatian spots */}
      {breedKey === "dalmatian" && (
        <>
          <circle cx="45" cy="50" r="4" fill={color.spots} opacity="0.8" />
          <circle cx="75" cy="52" r="5" fill={color.spots} opacity="0.8" />
          <circle cx="55" cy="70" r="3" fill={color.spots} opacity="0.8" />
          <circle cx="68" cy="48" r="4" fill={color.spots} opacity="0.8" />
        </>
      )}

      {/* Gray muzzle for senior dogs */}
      {stageKey === "senior" && <ellipse cx="60" cy="65" rx="12" ry="8" fill="#d0d0d0" opacity="0.5" />}
    </svg>
  )
}

export function PetCard({ pet, onViewDetails, readOnly = false }: PetCardProps) {
  const { act } = usePetsStore()
  const [actionLoading, setActionLoading] = useState<string | null>(null)

  const handleAction = async (action: "feed" | "wash" | "play") => {
    setActionLoading(action)
    try {
      await act(pet.id, action)
      toast.success(`${pet.name} has been ${action === "feed" ? "fed" : action === "wash" ? "washed" : "played with"}!`)
    } catch (error: any) {
      toast.error(error.response?.data?.message || `Failed to ${action}`)
    } finally {
      setActionLoading(null)
    }
  }

  const getStatColor = (value: number): "success" | "warning" | "danger" => {
    if (value >= 70) return "success"
    if (value >= 40) return "warning"
    return "danger"
  }

  const imageAlt = `${pet.name} - ${STAGE_LABELS[pet.lifeStage]} ${BREED_LABELS[pet.breed]}`

  return (
    <Card className="overflow-hidden transition-all hover:shadow-xl hover:scale-105 border-2">
      <CardHeader className="space-y-3 pb-4">
        <div className="flex justify-center mb-2">
          <div className="relative w-48 h-48 flex items-center justify-center bg-gradient-to-br from-pink-50 to-purple-50 rounded-3xl shadow-lg p-6">
            {getPetIllustration(pet.breed, pet.lifeStage)}
          </div>
        </div>

        <div className="flex items-start justify-between">
          <CardTitle className="text-2xl font-bold">{pet.name}</CardTitle>
          <Badge variant="secondary" className="text-sm font-semibold">
            {STAGE_LABELS[pet.lifeStage]}
          </Badge>
        </div>
        <p className="text-sm text-muted-foreground font-medium">{BREED_LABELS[pet.breed]}</p>
      </CardHeader>

      <CardContent className="space-y-4">
        <div className="space-y-3">
          <StatBar label="Hunger" value={100 - pet.hunger} color={getStatColor(100 - pet.hunger)} />
          <StatBar label="Hygiene" value={pet.hygiene} color={getStatColor(pet.hygiene)} />
          <StatBar label="Fun" value={pet.fun} color={getStatColor(pet.fun)} />
        </div>

        {!readOnly && (
          <div className="grid grid-cols-3 gap-2 pt-2">
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleAction("feed")}
              disabled={actionLoading !== null}
              className="flex flex-col gap-1 h-auto py-3 hover:scale-105 transition-transform"
            >
              <Utensils className="h-5 w-5" />
              <span className="text-xs font-semibold">Feed</span>
            </Button>
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleAction("wash")}
              disabled={actionLoading !== null}
              className="flex flex-col gap-1 h-auto py-3 hover:scale-105 transition-transform"
            >
              <Sparkles className="h-5 w-5" />
              <span className="text-xs font-semibold">Wash</span>
            </Button>
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleAction("play")}
              disabled={actionLoading !== null}
              className="flex flex-col gap-1 h-auto py-3 hover:scale-105 transition-transform"
            >
              <Gamepad2 className="h-5 w-5" />
              <span className="text-xs font-semibold">Play</span>
            </Button>
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
