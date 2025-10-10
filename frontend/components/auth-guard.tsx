"use client"

import type React from "react"
import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"

interface AuthGuardProps {
  children: React.ReactNode
  requireAdmin?: boolean
}

export function AuthGuard({ children, requireAdmin = false }: AuthGuardProps) {
  const router = useRouter()
  const [isChecking, setIsChecking] = useState(true)
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  useEffect(() => {
    console.log("[v0] AuthGuard: Starting authentication check...")

    try {
      const storedData = localStorage.getItem("auth-storage")
      console.log("[v0] AuthGuard: Raw localStorage data:", storedData ? "exists" : "null")

      if (!storedData) {
        console.log("[v0] AuthGuard: ❌ No auth-storage found, redirecting to /login")
        router.replace("/login")
        setIsChecking(false)
        return
      }

      const parsed = JSON.parse(storedData)
      const token = parsed?.state?.token
      const isAdmin = parsed?.state?.isAdmin || false

      console.log("[v0] AuthGuard: Parsed auth data:", {
        hasToken: !!token,
        tokenLength: token?.length || 0,
        isAdmin,
        requireAdmin,
      })

      if (!token) {
        console.log("[v0] AuthGuard: ❌ No token in parsed data, redirecting to /login")
        router.replace("/login")
        setIsChecking(false)
        return
      }

      if (requireAdmin && !isAdmin) {
        console.log("[v0] AuthGuard: ❌ Admin required but user is not admin, redirecting to /app")
        router.replace("/app")
        setIsChecking(false)
        return
      }

      console.log("[v0] AuthGuard: ✅ Authentication successful, rendering children")
      setIsAuthenticated(true)
      setIsChecking(false)
    } catch (error) {
      console.error("[v0] AuthGuard: ❌ Error during auth check:", error)
      router.replace("/login")
      setIsChecking(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []) // Empty dependency array - only run once on mount

  if (isChecking) {
    console.log("[v0] AuthGuard: Rendering loading state...")
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-muted-foreground">Loading...</div>
      </div>
    )
  }

  if (!isAuthenticated) {
    console.log("[v0] AuthGuard: Not authenticated, returning null")
    return null
  }

  console.log("[v0] AuthGuard: Rendering protected content")
  return <>{children}</>
}
