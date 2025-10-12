"use client"

import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"

export default function HomePage() {
  return (
    <div className="flex min-h-screen items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1 text-center">
          <CardTitle className="text-3xl font-bold text-balance">Virtual Pet App</CardTitle>
          <CardDescription>Manage and care for your virtual dogs</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <Link href="/login">
            <Button className="w-full" size="lg">
              Sign In
            </Button>
          </Link>
          <Link href="/register">
            <Button className="w-full bg-transparent" variant="outline" size="lg">
              Create Account
            </Button>
          </Link>
        </CardContent>
      </Card>
    </div>
  )
}
