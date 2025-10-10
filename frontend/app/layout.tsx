import type React from "react"
import type { Metadata } from "next"
import { Quicksand } from "next/font/google"
import { GeistMono } from "geist/font/mono"
import { Analytics } from "@vercel/analytics/next"
import { Toaster } from "sonner"
import { Suspense } from "react"
import "./globals.css"

const quicksand = Quicksand({
  subsets: ["latin"],
  variable: "--font-quicksand",
  weight: ["300", "400", "500", "600", "700"],
})

export const metadata: Metadata = {
  title: "Virtual Pet App",
  description: "Manage your virtual dogs",
  generator: "v0.app",
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="en">
      <body className={`font-sans ${quicksand.variable} ${GeistMono.variable}`}>
        <div className="relative z-10">
          <Suspense fallback={null}>{children}</Suspense>
        </div>
        <Toaster position="top-right" richColors />
        <Analytics />
      </body>
    </html>
  )
}
