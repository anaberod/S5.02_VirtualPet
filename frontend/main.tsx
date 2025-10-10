import React from "react"
import ReactDOM from "react-dom/client"
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom"
import HomePage from "./app/page"
import LoginPage from "./app/login/page"
import RegisterPage from "./app/register/page"
import AppPage from "./app/app/page"
import PetDetailPage from "./app/pets/[id]/page"
import ProfilePage from "./app/profile/page"
import AdminPetsPage from "./app/admin/pets/page"
import AdminUsersPage from "./app/admin/users/page"
import UserPetsPage from "./app/admin/users/[id]/pets/page"
import "./app/globals.css"

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/app" element={<AppPage />} />
        <Route path="/pets/:id" element={<PetDetailPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/admin/pets" element={<AdminPetsPage />} />
        <Route path="/admin/users" element={<AdminUsersPage />} />
        <Route path="/admin/users/:id/pets" element={<UserPetsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  </React.StrictMode>,
)
