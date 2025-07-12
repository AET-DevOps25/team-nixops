'use client'

import { useState, useRef, useEffect } from "react"
import FullCalendarClient from "@/components/full-calendar-client"

export default function ResizableLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const [leftWidth, setLeftWidth] = useState(600)
  const isDragging = useRef(false)
  const lastResize = useRef(0)

  const handleMouseDown = () => {
    isDragging.current = true
  }

  const handleMouseMove = (e: MouseEvent) => {
    if (!isDragging.current) return
    setLeftWidth(e.clientX)

    const now = Date.now()
    if (now - lastResize.current > 100) {
      window.dispatchEvent(new Event("resize"))
      lastResize.current = now
    }
  }

  const handleMouseUp = () => {
    if (isDragging.current) {
      isDragging.current = false
      window.dispatchEvent(new Event("resize"))
    }
  }

  useEffect(() => {
    window.addEventListener("mousemove", handleMouseMove)
    window.addEventListener("mouseup", handleMouseUp)
    return () => {
      window.removeEventListener("mousemove", handleMouseMove)
      window.removeEventListener("mouseup", handleMouseUp)
    }
  }, [])

  return (
    <div className="flex h-screen w-screen overflow-hidden">
      <div
        style={{ width: leftWidth }}
        className="overflow-y-auto"
      >
        {children}
      </div>

      <div
        onMouseDown={handleMouseDown}
        style={{
          background: 'color-mix(in oklab, var(--ring) 50%, transparent)'
        }}
        className="w-1 cursor-col-resize"
      />

      <div className="flex-1 overflow-y-auto">
        <FullCalendarClient />
      </div>
    </div>
  )
}
