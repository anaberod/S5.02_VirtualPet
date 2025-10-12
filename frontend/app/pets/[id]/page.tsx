"use client"

import { useEffect, useState } from "react"
import { useRouter, useParams } from "next/navigation"
import { toast } from "sonner"
import { ArrowLeft, Trash2 } from "lucide-react"
import { AuthGuard } from "@/components/auth-guard"
import { AppHeader } from "@/components/app-header"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { StatBar } from "@/components/stat-bar"
import { usePetsStore } from "@/lib/stores/pets-store"
import type { PetResponse } from "@/lib/types"

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

function PetDetailPage() {
  const params = useParams()
  const router = useRouter()
  const id = params.id as string
  const { items, remove } = usePetsStore()
  const [pet, setPet] = useState<PetResponse | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)

  useEffect(() => {
    const foundPet = items.find((p) => p.id === Number(id))
    if (foundPet) {
      setPet(foundPet)
    }
  }, [id, items])

  const handleDelete = async () => {
    if (!pet) return

    const confirmed = window.confirm(`Are you sure you want to delete ${pet.name}? This action cannot be undone.`)

    if (!confirmed) return

    setIsDeleting(true)
    try {
      await remove(pet.id)
      toast.success(`${pet.name} has been removed`)
      router.push("/app")
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Failed to delete pet")
    } finally {
      setIsDeleting(false)
    }
  }

  if (!pet) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <p className="text-muted-foreground">Pet not found</p>
      </div>
    )
  }

  const isPassed = pet.dead || pet.lifeStage === "PASSED"

  return (
    <div className="min-h-screen bg-background">
      <AppHeader />

      <main className="container mx-auto px-4 py-8">
        <Button variant="ghost" size="sm" onClick={() => router.push("/app")} className="mb-4">
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Dashboard
        </Button>

        <div className="max-w-2xl mx-auto space-y-6">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="text-2xl">{pet.name}</CardTitle>
                <Badge variant={isPassed ? "destructive" : "secondary"} className="text-sm">
                  {STAGE_LABELS[pet.lifeStage]}
                </Badge>
              </div>
              <p className="text-muted-foreground">{BREED_LABELS[pet.breed]}</p>
            </CardHeader>
            <CardContent className="space-y-6">
              {isPassed ? (
                <>
                  <div className="text-center py-6 space-y-3">
                    <p className="text-lg font-semibold text-muted-foreground italic">In loving memory of {pet.name}</p>
                    <p className="text-sm text-muted-foreground">A beloved companion who brought joy and happiness</p>
                  </div>

                  <div className="grid grid-cols-2 gap-4 pt-4 border-t border-border">
                    <div>
                      <p className="text-sm text-muted-foreground">Born</p>
                      <p className="text-lg font-semibold">{new Date(pet.createdAt).toLocaleDateString()}</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Passed Away</p>
                      <p className="text-lg font-semibold">
                        {pet.deathAt ? new Date(pet.deathAt).toLocaleDateString() : "Unknown"}
                      </p>
                    </div>
                  </div>
                </>
              ) : (
                <>
                  <div className="space-y-4">
                    <h3 className="font-semibold">Statistics</h3>
                    <StatBar label="Hunger" value={100 - pet.hunger} />
                    <StatBar label="Hygiene" value={pet.hygiene} />
                    <StatBar label="Fun" value={pet.fun} />
                  </div>

                  <div className="grid grid-cols-2 gap-4 pt-4 border-t border-border">
                    <div>
                      <p className="text-sm text-muted-foreground">Actions Count</p>
                      <p className="text-2xl font-bold">{pet.actionsCount}</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Life Stage</p>
                      <p className="text-2xl font-bold">{STAGE_LABELS[pet.lifeStage]}</p>
                    </div>
                  </div>

                  <div className="pt-4 border-t border-border">
                    <p className="text-sm text-muted-foreground mb-1">Created</p>
                    <p className="text-sm">{new Date(pet.createdAt).toLocaleDateString()}</p>
                  </div>
                </>
              )}
            </CardContent>
          </Card>

          <Button variant="destructive" className="w-full" disabled={isDeleting} onClick={handleDelete}>
            <Trash2 className="mr-2 h-4 w-4" />
            {isDeleting ? "Deleting..." : "Delete Pet"}
          </Button>
        </div>
      </main>
    </div>
  )
}

export default function PetDetailPageWrapper() {
  return (
    <AuthGuard>
      <PetDetailPage />
    </AuthGuard>
  )
}
