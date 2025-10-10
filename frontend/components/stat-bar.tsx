import { Progress } from "@/components/ui/progress"

interface StatBarProps {
  label: string
  value: number
  max?: number
  color?: "default" | "success" | "warning" | "danger"
}

export function StatBar({ label, value, max = 100, color = "default" }: StatBarProps) {
  const percentage = (value / max) * 100

  const getColorClass = () => {
    if (color === "success") return "bg-chart-3"
    if (color === "warning") return "bg-chart-5"
    if (color === "danger") return "bg-destructive"
    return "bg-primary"
  }

  return (
    <div className="space-y-1.5">
      <div className="flex items-center justify-between text-sm">
        <span className="text-muted-foreground">{label}</span>
        <span className="font-medium text-foreground">
          {value}/{max}
        </span>
      </div>
      <Progress value={percentage} className="h-2" indicatorClassName={getColorClass()} />
    </div>
  )
}
