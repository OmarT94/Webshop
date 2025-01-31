import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,   // Falls Backend CORS-Probleme macht
        secure: false,        // Falls dein Backend HTTPS erwartet
        rewrite: (path) => path.replace(/^\/api/, '') // Falls das Backend kein "/api" erwartet
      },
    },
  },
});
