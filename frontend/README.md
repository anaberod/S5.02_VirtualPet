# Virtual Pet App

A Next.js application for managing virtual pets (dogs) with authentication and admin features.

## Setup

1. **Create environment file:**
   \`\`\`bash
   cp .env.example .env.local
   \`\`\`
   
   Or manually create a `.env.local` file with:
   \`\`\`
   NEXT_PUBLIC_API_URL=http://localhost:8080
   \`\`\`

2. **Install dependencies:**
   \`\`\`bash
   npm install
   \`\`\`

3. **Start the development server:**
   \`\`\`bash
   npm run dev
   \`\`\`
   
   **Important:** If you get an error about `NEXT_PUBLIC_API_URL` being undefined, make sure:
   - The `.env.local` file exists in the root directory
   - The environment variable is prefixed with `NEXT_PUBLIC_` (required for client-side access in Next.js)
   - You've restarted the dev server after creating/modifying the `.env.local` file (Ctrl+C then `npm run dev` again)

4. **Build for production:**
   \`\`\`bash
   npm run build
   npm start
   \`\`\`

## Features

- User authentication (login/register)
- Create and manage virtual dogs
- Feed, wash, and play with your pets
- Track pet stats (hunger, hygiene, fun)
- Pet evolution system (Baby → Adult → Senior)
- Admin panel for managing all users and pets

## Tech Stack

- Next.js 15 + React 18 + TypeScript
- Tailwind CSS v4 + shadcn/ui
- Next.js App Router (file-based routing)
- Axios for API calls
- Zustand for state management
- Zod + React Hook Form for validation

## Project Structure

\`\`\`
app/
├── page.tsx              # Home page (redirects to /app or /login)
├── login/page.tsx        # Login page
├── register/page.tsx     # Register page
├── app/page.tsx          # Main dashboard (protected)
├── pets/[id]/page.tsx    # Pet detail page (protected)
├── profile/page.tsx      # User profile (protected)
└── admin/
    ├── pets/page.tsx     # Admin: All pets (admin only)
    └── users/
        ├── page.tsx      # Admin: All users (admin only)
        └── [id]/pets/page.tsx  # Admin: User's pets (admin only)
