"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { AuthGuard } from "@/components/auth-guard"

function AdminPage() {
  const router = useRouter()

  useEffect(() => {
    router.replace("/admin/users")
  }, [router])

  return null
}

export default function AdminPageWrapper() {
  return (
    <AuthGuard requireAdmin>
      <AdminPage />
    </AuthGuard>
  )
}
