"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Eye, Trash2, ArrowLeft } from "lucide-react"
import { AuthGuard } from "@/components/auth-guard"
import { AppHeader } from "@/components/app-header"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import apiClient from "@/lib/api-client"
import type { UserResponse } from "@/lib/types"

function AdminUsersPage() {
  const router = useRouter()
  const [users, setUsers] = useState<UserResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [deleting, setDeleting] = useState<number | null>(null)

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response = await apiClient.get<UserResponse[]>("/admin/users")
        setUsers(response.data)
      } catch (error) {
        console.error("Failed to fetch users", error)
      } finally {
        setLoading(false)
      }
    }
    fetchUsers()
  }, [])

  const handleDeleteUser = async (userId: number, username: string) => {
    if (
      !window.confirm(
        `Are you sure you want to delete user "${username}" and all their pets? This action cannot be undone.`,
      )
    ) {
      return
    }

    setDeleting(userId)
    try {
      await apiClient.delete(`/admin/users/${userId}`)
      setUsers(users.filter((user) => user.id !== userId))
    } catch (error) {
      console.error("Failed to delete user", error)
      alert("Failed to delete user. Please try again.")
    } finally {
      setDeleting(null)
    }
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
          <h2 className="text-2xl font-bold text-balance">All Users</h2>
          <p className="text-muted-foreground">Manage users in the system</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>All Users</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <p className="text-muted-foreground">Loading...</p>
            ) : users.length === 0 ? (
              <p className="text-muted-foreground">No users found</p>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>Username</TableHead>
                      <TableHead>Email</TableHead>
                      <TableHead>Roles</TableHead>
                      <TableHead>Created</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {users.map((user) => (
                      <TableRow key={user.id}>
                        <TableCell className="font-mono">{user.id}</TableCell>
                        <TableCell className="font-medium">{user.username}</TableCell>
                        <TableCell>{user.email}</TableCell>
                        <TableCell>
                          <div className="flex gap-1">
                            {user.roles.map((role) => (
                              <Badge key={role} variant="outline" className="text-xs">
                                {role.replace("ROLE_", "")}
                              </Badge>
                            ))}
                          </div>
                        </TableCell>
                        <TableCell>{new Date(user.createdAt).toLocaleDateString()}</TableCell>
                        <TableCell>
                          <div className="flex gap-2">
                            <Button
                              size="sm"
                              variant="ghost"
                              onClick={() => router.push(`/admin/users/${user.id}/pets`)}
                            >
                              <Eye className="mr-1 h-4 w-4" />
                              View Pets
                            </Button>
                            <Button
                              size="sm"
                              variant="ghost"
                              onClick={() => handleDeleteUser(user.id, user.username)}
                              disabled={deleting === user.id}
                              className="text-destructive hover:text-destructive"
                            >
                              <Trash2 className="mr-1 h-4 w-4" />
                              {deleting === user.id ? "Deleting..." : "Delete"}
                            </Button>
                          </div>
                        </TableCell>
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

export default function AdminUsersPageWrapper() {
  return (
    <AuthGuard requireAdmin>
      <AdminUsersPage />
    </AuthGuard>
  )
}
