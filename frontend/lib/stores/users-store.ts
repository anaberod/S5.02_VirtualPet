import { create } from "zustand"
import { apiClient } from "@/lib/api-client"
import type { UserResponse } from "@/lib/types"

interface UsersState {
  items: UserResponse[]
  loading: boolean
  error: string | null
  fetchAll: () => Promise<void>
  remove: (id: number) => Promise<void>
}

export const useUsersStore = create<UsersState>((set, get) => ({
  items: [],
  loading: false,
  error: null,

  fetchAll: async () => {
    console.log("[v0] Users Store: Fetching all users...")
    set({ loading: true, error: null })
    try {
      const response = await apiClient.get<UserResponse[]>("/admin/users")
      console.log("[v0] Users Store: Raw response:", response)

      let users: UserResponse[] = []

      // Handle different response formats
      if (Array.isArray(response.data)) {
        users = response.data
      } else if (response.data && typeof response.data === "object") {
        // Try common property names for paginated or wrapped responses
        const data = response.data as any
        if (Array.isArray(data.users)) {
          users = data.users
        } else if (Array.isArray(data.content)) {
          users = data.content
        } else if (Array.isArray(data.data)) {
          users = data.data
        }
      }

      console.log("[v0] Users Store: Parsed users:", users)
      set({ items: users, loading: false })
    } catch (error: any) {
      console.error("[v0] Users Store: Error fetching users:", error.response?.status, error.message)
      set({
        error: error.response?.data?.message || error.message || "Failed to fetch users",
        loading: false,
        items: [], // Clear items on error
      })
    }
  },

  remove: async (id: number) => {
    console.log("[v0] Users Store: Deleting user:", id)
    try {
      await apiClient.delete(`/admin/users/${id}`)
      set({ items: get().items.filter((user) => user.id !== id) })
      console.log("[v0] Users Store: User deleted successfully")
    } catch (error: any) {
      console.error("[v0] Users Store: Error deleting user:", error.response?.status, error.message)
      throw error
    }
  },
}))
