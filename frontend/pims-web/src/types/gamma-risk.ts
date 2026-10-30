export interface GammaAnnual {
  year: number
  date: string
  tYears: number
  shape: number
  meanDepthMm: number
  cappedMeanDepthMm: number
  remainingWallMm: number
  probability: number
  monteCarloProbability: number
  failedSamples: number
  standardError: number
  confidenceLow: number
  confidenceHigh: number
  zeroFailureUpper95: number | null
}
export interface GammaReport {
  segmentId: number
  segmentName: string
  modelVersion: string
  commissionDate: string
  inspectionDate: string
  wallThicknessMm: number
  inspectionDepthMm: number
  kValue: number
  n: number
  theta: number
  sampleCount: number
  seed: number
  status: string
  warnings: string[]
  annual: GammaAnnual[]
  convergence: { sampleCount: number; year: number; probability: number; standardError: number }[]
  history: { date: string; measuredDepthMm: number; calculatedDepthMm: number }[]
}
