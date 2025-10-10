import { create } from "zustand"
import apiClient from "@/lib/api-client"
import type { PetResponse, CreatePetRequest, UpdatePetRequest } from "@/lib/types"

interface PetsState {
  items: PetResponse[]
  loading: boolean
  error: string | null
  fetchAll: () => Promise<void>
  create: (data: CreatePetRequest) => Promise<PetResponse>
  update: (id: number, data: UpdatePetRequest) => Promise<PetResponse>
  remove: (id: number) => Promise<void>
  act: (id: number, action: "feed" | "wash" | "play") => Promise<PetResponse>
}

export const usePetsStore = create<PetsState>((set, get) => ({
  items: [],
  loading: false,
  error: null,

  fetchAll: async () => {
    console.log("[v0] Pets Store: fetchAll() called")
    set({ loading: true, error: null })
    try {
      console.log("[v0] Pets Store: Making GET request to /pets")
      const response = await apiClient.get<PetResponse[]>("/pets")
      console.log("[v0] Pets Store: Received response:", response.data.length, "pets")
      set({ items: response.data, loading: false })
    } catch (error: any) {
      console.error("[v0] Pets Store: Error fetching pets:", error.response?.status, error.response?.data)
      set({
        error: error.response?.data?.message || "Failed to fetch pets",
        loading: false,
      })
    }
  },

  create: async (data) => {
    set({ loading: true, error: null })
    try {
      const response = await apiClient.post<PetResponse>("/pets", data)
      set((state) => ({
        items: [...state.items, response.data],
        loading: false,
      }))
      return response.data
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Failed to create pet",
        loading: false,
      })
      throw error
    }
  },

  update: async (id, data) => {
    set({ loading: true, error: null })
    try {
      const response = await apiClient.put<PetResponse>(`/pets/${id}`, data)
      set((state) => ({
        items: state.items.map((pet) => (pet.id === id ? response.data : pet)),
        loading: false,
      }))
      return response.data
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Failed to update pet",
        loading: false,
      })
      throw error
    }
  },

  remove: async (id) => {
    set({ loading: true, error: null })
    try {
      await apiClient.delete(`/pets/${id}`)
      set((state) => ({
        items: state.items.filter((pet) => pet.id !== id),
        loading: false,
      }))
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Failed to delete pet",
        loading: false,
      })
      throw error
    }
  },

  act: async (id, action) => {
    set({ loading: true, error: null })
    try {
      const response = await apiClient.post<PetResponse>(`/pets/${id}/actions/${action}`)
      set((state) => ({
        items: state.items.map((pet) => (pet.id === id ? response.data : pet)),
        loading: false,
      }))
      return response.data
    } catch (error: any) {
      set({
        error: error.response?.data?.message || `Failed to ${action} pet`,
        loading: false,
      })
      throw error
    }
  },
}))
