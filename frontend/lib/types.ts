export type LifeStage = "BABY" | "ADULT" | "SENIOR"
export type Breed = "DALMATIAN" | "GOLDEN_RETRIEVER" | "LABRADOR"

export interface PetResponse {
  id: number
  name: string
  breed: Breed
  hunger: number
  hygiene: number
  fun: number
  actionsCount: number
  lifeStage: LifeStage
  createdAt: string
  updatedAt: string
}

export interface CreatePetRequest {
  name: string
  breed: Breed
}

export interface UpdatePetRequest {
  name: string
  breed: Breed
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}

export interface AuthLoginReq {
  email: string
  password: string
}

export interface AuthRegisterReq {
  username: string
  email: string
  password: string
}

export interface AuthLoginRes {
  token: string
  roles?: string[]
  username?: string
  email?: string
}

export interface UserResponse {
  id: number
  username: string
  email: string
  roles: string[]
  createdAt: string
}
