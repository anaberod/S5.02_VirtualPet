"use client"

import { useEffect, useMemo } from "react"
import { useRouter } from "next/navigation"
import { Dog, Users } from "lucide-react"
import { AuthGuard } from "@/components/auth-guard"
import { AppHeader } from "@/components/app-header"
import { PetCard } from "@/components/pet-card"
import { UserCard } from "@/components/user-card"
import { CreatePetDialog } from "@/components/create-pet-dialog"
import { usePetsStore } from "@/lib/stores/pets-store"
import { useUsersStore } from "@/lib/stores/users-store"
import { useAuthStore } from "@/lib/stores/auth-store"

function AppDashboard() {
  const router = useRouter()
  const { items: pets, loading: petsLoading, fetchAll: fetchPets } = usePetsStore()
  const { items: users, loading: usersLoading, fetchAll: fetchUsers } = useUsersStore()
  const { token, _hasHydrated, isAdmin } = useAuthStore()

  useEffect(() => {
    console.log(
      "[v0] AppDashboard: useEffect triggered, _hasHydrated:",
      _hasHydrated,
      "token:",
      token ? "present" : "missing",
      "isAdmin:",
      isAdmin,
    )

    if (_hasHydrated && token) {
      if (isAdmin) {
        console.log("[v0] AppDashboard: Admin detected, fetching users...")
        fetchUsers()
      } else {
        console.log("[v0] AppDashboard: Regular user detected, fetching pets...")
        fetchPets()
      }
    } else if (_hasHydrated && !token) {
      console.warn("[v0] AppDashboard: Auth hydrated but no token found")
    } else {
      console.log("[v0] AppDashboard: Waiting for auth hydration...")
    }
  }, [_hasHydrated, token, isAdmin, fetchPets, fetchUsers])

  const sortedPets = useMemo(() => {
    return [...pets].sort((a, b) => {
      // Dead pets go to the end
      if (a.dead && !b.dead) return 1
      if (!a.dead && b.dead) return -1
      return 0
    })
  }, [pets])

  if (isAdmin) {
    return (
      <div className="min-h-screen">
        <AppHeader />

        <main className="container mx-auto px-4 py-8">
          <div className="mb-6 flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold text-balance">Users</h2>
              <p className="text-muted-foreground">Manage all users in the system</p>
            </div>
          </div>

          {usersLoading && users.length === 0 ? (
            <div className="flex items-center justify-center py-12">
              <p className="text-muted-foreground">Loading users...</p>
            </div>
          ) : users.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <Users className="h-16 w-16 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">No users found</h3>
              <p className="text-muted-foreground">There are no users in the system yet.</p>
            </div>
          ) : (
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {users.map((user) => (
                <UserCard key={user.id} user={user} />
              ))}
            </div>
          )}
        </main>
      </div>
    )
  }

  return (
    <div className="min-h-screen">
      <AppHeader />

      <main className="container mx-auto px-4 py-8">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-balance">My Pets</h2>
            <p className="text-muted-foreground">Manage and care for your virtual dogs</p>
          </div>
          <CreatePetDialog />
        </div>

        {petsLoading && pets.length === 0 ? (
          <div className="flex items-center justify-center py-12">
            <p className="text-muted-foreground">Loading your pets...</p>
          </div>
        ) : pets.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <Dog className="h-16 w-16 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">No pets yet</h3>
            <p className="text-muted-foreground mb-4">Create your first dog to get started!</p>
            <CreatePetDialog />
          </div>
        ) : (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {sortedPets.map((pet) => (
              <PetCard key={pet.id} pet={pet} onViewDetails={(id) => router.push(`/pets/${id}`)} />
            ))}
          </div>
        )}
      </main>
    </div>
  )
}

export default function AppPage() {
  return (
    <AuthGuard>
      <AppDashboard />
    </AuthGuard>
  )
}
