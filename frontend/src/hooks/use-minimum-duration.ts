import { useEffect, useRef, useState } from 'react'

// Keeps `isActive` reading as true for at least `minDurationMs` once it turns true,
// even if the real value flips back to false sooner -- e.g. a loading skeleton that
// would otherwise flash on and off faster than a human can actually register it for a
// fast response. Never holds it artificially if `isActive` was never actually true
// (cached data that never shows a loading state at all).
export function useMinimumDuration(isActive: boolean, minDurationMs: number): boolean {
  const [shown, setShown] = useState(isActive)
  const startedAtRef = useRef<number | null>(isActive ? Date.now() : null)

  useEffect(() => {
    if (isActive) {
      startedAtRef.current = Date.now()
      setShown(true)
      return
    }

    const startedAt = startedAtRef.current
    if (startedAt === null) {
      setShown(false)
      return
    }

    const remaining = minDurationMs - (Date.now() - startedAt)
    if (remaining <= 0) {
      setShown(false)
      return
    }

    const timeout = setTimeout(() => {
      startedAtRef.current = null
      setShown(false)
    }, remaining)

    return () => clearTimeout(timeout)
  }, [isActive, minDurationMs])

  return shown
}
