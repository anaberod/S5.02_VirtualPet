"use client"

import { useEffect, useState } from "react"
import { useRouter, useParams } from "next/navigation"
import { ArrowLeft, Trash2 } from "lucide-react"
import { AuthGuard } from "@/components/auth-guard"
import { AppHeader } from "@/components/app-header"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { PetCard } from "@/components/pet-card"
import apiClient from "@/lib/api-client"
import type { PetResponse, UserResponse } from "@/lib/types"

function UserPetsPage() {
  const params = useParams()
  const router = useRouter()
  const id = params.id as string
  const [user, setUser] = useState<UserResponse | null>(null)
  const [pets, setPets] = useState<PetResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [deleting, setDeleting] = useState<number | null>(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        console.log(`[v0] UserPetsPage: Fetching user ${id} and their pets`)
        const [userRes, petsRes] = await Promise.all([
          apiClient.get<UserResponse>(`/admin/users/${id}`),
          apiClient.get<PetResponse[]>(`/admin/users/${id}/pets`),
        ])

        console.log("[v0] UserPetsPage: User response:", userRes.data)
        console.log("[v0] UserPetsPage: Pets response:", petsRes.data)
        console.log("[v0] UserPetsPage: Pets response type:", typeof petsRes.data)
        console.log("[v0] UserPetsPage: Pets is array:", Array.isArray(petsRes.data))

        setUser(userRes.data)

        let petsData: PetResponse[] = []

        if (Array.isArray(petsRes.data)) {
          petsData = petsRes.data
          console.log("[v0] UserPetsPage: Using direct array, length:", petsData.length)
        } else if (petsRes.data && typeof petsRes.data === "object") {
          if (Array.isArray(petsRes.data.pets)) {
            petsData = petsRes.data.pets
            console.log("[v0] UserPetsPage: Using petsRes.data.pets, length:", petsData.length)
          } else if (Array.isArray(petsRes.data.content)) {
            petsData = petsRes.data.content
            console.log("[v0] UserPetsPage: Using petsRes.data.content, length:", petsData.length)
          } else if (Array.isArray(petsRes.data.data)) {
            petsData = petsRes.data.data
            console.log("[v0] UserPetsPage: Using petsRes.data.data, length:", petsData.length)
          } else {
            console.error("[v0] UserPetsPage: Unknown pets response format, keys:", Object.keys(petsRes.data))
          }
        }

        setPets(petsData)
        console.log("[v0] UserPetsPage: Set pets state with", petsData.length, "pets")
      } catch (error) {
        console.error("[v0] UserPetsPage: Failed to fetch data", error)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [id])

  const handleDeletePet = async (petId: number, petName: string) => {
    if (!window.confirm(`Are you sure you want to delete "${petName}"? This action cannot be undone.`)) {
      return
    }

    setDeleting(petId)
    try {
      await apiClient.delete(`/pets/${petId}`)
      setPets(pets.filter((pet) => pet.id !== petId))
    } catch (error) {
      console.error("Failed to delete pet", error)
      alert("Failed to delete pet. Please try again.")
    } finally {
      setDeleting(null)
    }
  }

  return (
    <div className="min-h-screen bg-background">
      <AppHeader />

      <main className="container mx-auto px-4 py-8">
        <Button variant="ghost" size="sm" onClick={() => router.push("/admin/users")} className="mb-4">
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Users
        </Button>

        {loading ? (
          <p className="text-muted-foreground">Loading...</p>
        ) : (
          <>
            {user && (
              <Card className="mb-6">
                <CardHeader>
                  <CardTitle>{user.username}'s Information</CardTitle>
                </CardHeader>
                <CardContent className="grid gap-2">
                  <div>
                    <span className="text-sm text-muted-foreground">Username: </span>
                    <span className="font-medium">{user.username}</span>
                  </div>
                  <div>
                    <span className="text-sm text-muted-foreground">Email: </span>
                    <span className="font-medium">{user.email}</span>
                  </div>
                  <div>
                    <span className="text-sm text-muted-foreground">Roles: </span>
                    <span className="font-medium">{user.roles.join(", ")}</span>
                  </div>
                </CardContent>
              </Card>
            )}

            <div className="mb-4">
              <h2 className="text-2xl font-bold">Pets ({pets.length})</h2>
            </div>

            {pets.length === 0 ? (
              <p className="text-muted-foreground">This user has no pets</p>
            ) : (
              <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {pets.map((pet) => (
                  <div key={pet.id} className="relative">
                    <PetCard pet={pet} readOnly />
                    <Button
                      size="sm"
                      variant="destructive"
                      onClick={() => handleDeletePet(pet.id, pet.name)}
                      disabled={deleting === pet.id}
                      className="absolute top-2 right-2"
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </main>
    </div>
  )
}

export default function UserPetsPageWrapper() {
  return (
    <AuthGuard requireAdmin>
      <UserPetsPage />
    </AuthGuard>
  )
}
