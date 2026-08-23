import { useEffect, useState } from 'react'

// Tracks the theme ThemeToggle actually applies (a `dark`/`light` class on
// <html>), rather than assuming a theme library -- this app manages theme
// itself (see ThemeToggle.tsx), it doesn't use next-themes.
export function useResolvedTheme(): 'light' | 'dark' {
  const [theme, setTheme] = useState<'light' | 'dark'>(() =>
    typeof document !== 'undefined' && document.documentElement.classList.contains('dark')
      ? 'dark'
      : 'light',
  )

  useEffect(() => {
    const root = document.documentElement
    const readTheme = () => setTheme(root.classList.contains('dark') ? 'dark' : 'light')

    readTheme()
    const observer = new MutationObserver(readTheme)
    observer.observe(root, { attributes: true, attributeFilter: ['class'] })
    return () => observer.disconnect()
  }, [])

  return theme
}
