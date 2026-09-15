import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { LazyMotion, MotionConfig } from 'motion/react'
import '@fontsource-variable/archivo'
import './index.css'
import App from './App.jsx'

const loadMotionFeatures = () => import('./lib/motionFeatures').then((module) => module.default)

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <MotionConfig reducedMotion="user" transition={{ ease: [0.2, 0, 0, 1] }}>
        <LazyMotion features={loadMotionFeatures} strict>
          <App />
        </LazyMotion>
      </MotionConfig>
    </BrowserRouter>
  </StrictMode>,
)
