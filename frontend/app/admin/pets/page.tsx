"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { ArrowLeft } from "lucide-react"
import { AuthGuard } from "@/components/auth-guard"
import { AppHeader } from "@/components/app-header"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import apiClient from "@/lib/api-client"
import type { PetResponse, UserResponse } from "@/lib/types"

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

function AdminPetsPage() {
  const router = useRouter()
  const [pets, setPets] = useState<PetResponse[]>([])
  const [users, setUsers] = useState<UserResponse[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [petsResponse, usersResponse] = await Promise.all([
          apiClient.get("/admin/pets"),
          apiClient.get<UserResponse[]>("/admin/users"),
        ])

        let petsData: PetResponse[] = []
        if (Array.isArray(petsResponse.data)) {
          petsData = petsResponse.data
        } else if (petsResponse.data?.content) {
          petsData = petsResponse.data.content
        } else if (petsResponse.data?.pets) {
          petsData = petsResponse.data.pets
        }

        setPets(petsData)
        setUsers(usersResponse.data)
      } catch (error) {
        console.error("Failed to fetch data", error)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [])

  const getOwnerUsername = (ownerId?: number): string => {
    if (!ownerId) return "Unknown"
    const user = users.find((u) => u.id === ownerId)
    return user?.username || `User #${ownerId}`
  }

  return (
    <div className="min-h-screen">
      <AppHeader />

      <main className="container mx-auto px-4 py-8">
        <Button variant="ghost" size="sm" onClick={() => router.push("/app")} className="mb-4">
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Dashboard
        </Button>

        <div className="mb-6">
          <h2 className="text-2xl font-bold text-balance">All Pets</h2>
          <p className="text-muted-foreground">View all pets in the system</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>All Pets in System</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <p className="text-muted-foreground">Loading...</p>
            ) : pets.length === 0 ? (
              <p className="text-muted-foreground">No pets found</p>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>Name</TableHead>
                      <TableHead>Breed</TableHead>
                      <TableHead>Stage</TableHead>
                      <TableHead>Owner</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {pets.map((pet) => (
                      <TableRow key={pet.id}>
                        <TableCell className="font-mono">{pet.id}</TableCell>
                        <TableCell className="font-medium">{pet.name}</TableCell>
                        <TableCell>{BREED_LABELS[pet.breed]}</TableCell>
                        <TableCell>
                          <Badge variant="secondary">{STAGE_LABELS[pet.lifeStage]}</Badge>
                        </TableCell>
                        <TableCell>{getOwnerUsername(pet.ownerId)}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </main>
    </div>
  )
}

export default function AdminPetsPageWrapper() {
  return (
    <AuthGuard requireAdmin>
      <AdminPetsPage />
    </AuthGuard>
  )
}
