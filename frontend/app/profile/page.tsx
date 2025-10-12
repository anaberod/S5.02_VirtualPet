"use client"

import { AuthGuard } from "@/components/auth-guard"
import { AppHeader } from "@/components/app-header"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import { useRouter } from "next/navigation"
import { useAuthStore } from "@/lib/stores/auth-store"

function ProfilePage() {
  const { username, email, roles } = useAuthStore()
  const router = useRouter()

  return (
    <div className="min-h-screen">
      <AppHeader />

      <main className="container mx-auto px-4 py-8">
        <div className="max-w-2xl mx-auto">
          <Button variant="ghost" size="sm" onClick={() => router.push("/app")} className="mb-4">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Dashboard
          </Button>

          <div className="mb-6">
            <h2 className="text-2xl font-bold text-balance">Profile</h2>
            <p className="text-muted-foreground">View your account information</p>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Account Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">Username</p>
                <p className="text-lg font-medium">{username}</p>
              </div>

              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">Email</p>
                <p className="text-lg font-medium">{email}</p>
              </div>

              <div className="space-y-2">
                <p className="text-sm text-muted-foreground">Roles</p>
                <div className="flex gap-2">
                  {roles.map((role) => (
                    <Badge key={role} variant="secondary">
                      {role.replace("ROLE_", "")}
                    </Badge>
                  ))}
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  )
}

export default function ProfilePageWrapper() {
  return (
    <AuthGuard>
      <ProfilePage />
    </AuthGuard>
  )
}
