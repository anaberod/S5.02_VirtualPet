import { create } from "zustand"
import { persist } from "zustand/middleware"

function decodeJWT(token: string): any {
  try {
    const base64Url = token.split(".")[1]
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/")
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join(""),
    )
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error("[v0] Error decoding JWT:", error)
    return null
  }
}

interface AuthState {
  token: string | null
  roles: string[]
  username: string | null
  email: string | null
  isAdmin: boolean
  _hasHydrated: boolean
  login: (token: string, roles?: string[], username?: string, email?: string) => void
  logout: () => void
  setHasHydrated: (state: boolean) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      roles: [],
      username: null,
      email: null,
      isAdmin: false,
      _hasHydrated: false,
      login: (token, roles, username, email) => {
        console.log("[v0] Auth store login called with:", { token, roles, username, email })

        let finalRoles = roles || []
        let finalUsername = username || null
        let finalEmail = email || null

        if (!roles || !username || !email) {
          const decoded = decodeJWT(token)
          console.log("[v0] Decoded JWT:", decoded)

          if (decoded) {
            finalRoles = decoded.roles || decoded.authorities || []
            finalUsername = decoded.username || decoded.sub || null
            finalEmail = decoded.email || decoded.sub || null
          }
        }

        console.log("[v0] Final values:", { finalRoles, finalUsername, finalEmail })

        set({
          token,
          roles: finalRoles,
          username: finalUsername,
          email: finalEmail,
          isAdmin: Array.isArray(finalRoles) && finalRoles.some((role) => role === "ROLE_ADMIN" || role === "ADMIN"),
        })
        console.log("[v0] Auth store state updated, token:", token ? "present" : "null")
      },
      logout: () => {
        console.log("[v0] Auth store logout called")
        set({
          token: null,
          roles: [],
          username: null,
          email: null,
          isAdmin: false,
        })
      },
      setHasHydrated: (state) => {
        set({ _hasHydrated: state })
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        token: state.token,
        roles: state.roles,
        username: state.username,
        email: state.email,
        isAdmin: state.isAdmin,
      }),
      onRehydrateStorage: () => (state) => {
        console.log("[v0] Zustand hydration complete, token:", state?.token ? "present" : "null")
        state?.setHasHydrated(true)
      },
    },
  ),
)
