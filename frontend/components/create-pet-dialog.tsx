"use client"

import { useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { toast } from "sonner"
import { Plus } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { usePetsStore } from "@/lib/stores/pets-store"
import type { Breed } from "@/lib/types"

const createPetSchema = z.object({
  name: z.string().min(2, "Name must be at least 2 characters"),
  breed: z.enum(["DALMATIAN", "GOLDEN_RETRIEVER", "LABRADOR"]),
})

type CreatePetFormData = z.infer<typeof createPetSchema>

export function CreatePetDialog() {
  const [open, setOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { create } = usePetsStore()

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
    reset,
  } = useForm<CreatePetFormData>({
    resolver: zodResolver(createPetSchema),
  })

  const onSubmit = async (data: CreatePetFormData) => {
    setIsLoading(true)
    try {
      await create(data)
      toast.success(`${data.name} has been created!`)
      setOpen(false)
      reset()
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Failed to create pet")
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          New Pet
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Create a New Pet</DialogTitle>
          <DialogDescription>Add a new dog to your collection</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Name</Label>
            <Input id="name" placeholder="Enter pet name" {...register("name")} disabled={isLoading} />
            {errors.name && <p className="text-sm text-destructive">{errors.name.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="breed">Breed</Label>
            <Select onValueChange={(value) => setValue("breed", value as Breed)} disabled={isLoading}>
              <SelectTrigger id="breed">
                <SelectValue placeholder="Select a breed" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="DALMATIAN">Dalmatian</SelectItem>
                <SelectItem value="GOLDEN_RETRIEVER">Golden Retriever</SelectItem>
                <SelectItem value="LABRADOR">Labrador</SelectItem>
              </SelectContent>
            </Select>
            {errors.breed && <p className="text-sm text-destructive">{errors.breed.message}</p>}
          </div>

          <div className="flex gap-2 pt-2">
            <Button type="button" variant="outline" onClick={() => setOpen(false)} className="flex-1">
              Cancel
            </Button>
            <Button type="submit" disabled={isLoading} className="flex-1">
              {isLoading ? "Creating..." : "Create Pet"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}
