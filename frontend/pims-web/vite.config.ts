import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    base: env.VITE_PUBLIC_PATH || '/',
    plugins: [vue()],
    resolve: {
      alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
    },
    css: {
      preprocessorOptions: { less: { javascriptEnabled: true } }
    },
    server: {
      port: 5173,
      host: '0.0.0.0',
      proxy: {
        // 开发环境经网关访问后端微服务
        '/api': { target: env.VITE_PROXY_TARGET || 'http://localhost:9100', changeOrigin: true }
      }
    },
    build: {
      outDir: 'dist',
      chunkSizeWarningLimit: 1500,
      rollupOptions: {
        output: {
          manualChunks: {
            vue: ['vue', 'vue-router', 'pinia'],
            antd: ['ant-design-vue', '@ant-design/icons-vue'],
            echarts: ['echarts', 'vue-echarts'],
            ol: ['ol']
          }
        }
      }
    }
  }
})
