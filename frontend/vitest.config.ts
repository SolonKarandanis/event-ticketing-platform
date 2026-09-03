import { defineConfig } from 'vitest/config'
import viteReact from '@vitejs/plugin-react'

// Deliberately separate from vite.config.ts, not an extension of it -- tanstackStart()
// does dev-server/SSR route-generation work that has no reason to run for a plain
// hook/unit test, and would only slow test startup down or risk conflicting with
// Vitest's own transform pipeline. resolve.tsconfigPaths mirrors vite.config.ts exactly
// (a native Vite 8 option, not a separate plugin), so '#/*' imports resolve identically
// in tests as they do in the real app.
export default defineConfig({
  resolve: { tsconfigPaths: true },
  plugins: [viteReact()],
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
  },
})
