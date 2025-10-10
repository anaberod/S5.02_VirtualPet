import axios from "axios"

console.log("[v0] API Client: Initializing with baseURL:", process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080")

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
})

// Request interceptor to add JWT token
apiClient.interceptors.request.use(
  (config) => {
    console.log("[v0] API Client: ===== REQUEST INTERCEPTOR START =====")
    console.log("[v0] API Client: Request URL:", config.url)
    console.log("[v0] API Client: Request method:", config.method)

    try {
      // Read from Zustand persist storage
      const authStorage = localStorage.getItem("auth-storage")
      console.log(
        "[v0] API Client: auth-storage raw:",
        authStorage ? `present (${authStorage.substring(0, 100)}...)` : "missing",
      )

      if (authStorage) {
        const parsed = JSON.parse(authStorage)
        console.log("[v0] API Client: Parsed auth-storage:", parsed)

        const token = parsed?.state?.token
        console.log("[v0] API Client: Extracted token:", token ? `present (length: ${token.length})` : "missing")
        console.log("[v0] API Client: Token preview:", token ? `${token.substring(0, 50)}...` : "N/A")

        if (token) {
          config.headers.Authorization = `Bearer ${token}`
          console.log(
            "[v0] API Client: Authorization header set to:",
            config.headers.Authorization.substring(0, 70) + "...",
          )
        } else {
          console.warn("[v0] API Client: No token found in auth-storage, request will be unauthenticated")
        }
      } else {
        console.warn("[v0] API Client: auth-storage not found in localStorage, request will be unauthenticated")
      }
    } catch (error) {
      console.error("[v0] API Client: Error reading token:", error)
    }

    console.log("[v0] API Client: Final request headers:", config.headers)
    console.log("[v0] API Client: ===== REQUEST INTERCEPTOR END =====")
    return config
  },
  (error) => {
    console.error("[v0] API Client: Request interceptor error:", error)
    return Promise.reject(error)
  },
)

// Response interceptor to handle 401/403 errors
apiClient.interceptors.response.use(
  (response) => {
    console.log("[v0] API Client: Response success:", response.config.url, response.status)
    return response
  },
  (error) => {
    console.error("[v0] API Client: Response error:", error.config?.url, error.response?.status, error.message)

    if (error.response?.status === 401) {
      console.log("[v0] API Client: 401 Unauthorized detected - token is invalid or missing, clearing auth storage")
      localStorage.removeItem("auth-storage")
    } else if (error.response?.status === 403) {
      console.log(
        "[v0] API Client: 403 Forbidden detected - user is authenticated but lacks permissions (token NOT cleared)",
      )
    }

    return Promise.reject(error)
  },
)

export { apiClient }
export default apiClient
