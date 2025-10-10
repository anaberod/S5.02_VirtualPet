"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { toast } from "sonner"
import { User, Trash2, Eye } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { useUsersStore } from "@/lib/stores/users-store"
import type { UserResponse } from "@/lib/types"

interface UserCardProps {
  user: UserResponse
}

export function UserCard({ user }: UserCardProps) {
  const router = useRouter()
  const { remove } = useUsersStore()
  const [deleteLoading, setDeleteLoading] = useState(false)

  const handleDelete = async () => {
    if (!confirm(`Are you sure you want to delete user "${user.username}"? This will also delete all their pets.`)) {
      return
    }

    setDeleteLoading(true)
    try {
      await remove(user.id)
      toast.success(`User "${user.username}" has been deleted`)
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Failed to delete user")
    } finally {
      setDeleteLoading(false)
    }
  }

  const handleViewPets = () => {
    router.push(`/admin/users/${user.id}/pets`)
  }

  return (
    <Card className="overflow-hidden transition-all hover:shadow-lg">
      <CardHeader className="space-y-2 pb-4">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-2">
            <User className="h-5 w-5 text-primary" />
            <CardTitle className="text-xl">{user.username}</CardTitle>
          </div>
        </div>
        <p className="text-sm text-muted-foreground">{user.email}</p>
      </CardHeader>

      <CardContent className="space-y-4">
        <div className="space-y-2">
          <p className="text-sm font-medium">Roles</p>
          <div className="flex flex-wrap gap-2">
            {user.roles.map((role) => (
              <Badge key={role} variant="secondary">
                {role.replace("ROLE_", "")}
              </Badge>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2 pt-2">
          <Button
            size="sm"
            variant="outline"
            onClick={handleViewPets}
            className="flex items-center gap-2 bg-transparent"
          >
            <Eye className="h-4 w-4" />
            <span className="text-xs">View Pets</span>
          </Button>
          <Button
            size="sm"
            variant="outline"
            onClick={handleDelete}
            disabled={deleteLoading}
            className="flex items-center gap-2 text-destructive hover:text-destructive bg-transparent"
          >
            <Trash2 className="h-4 w-4" />
            <span className="text-xs">Delete</span>
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}
