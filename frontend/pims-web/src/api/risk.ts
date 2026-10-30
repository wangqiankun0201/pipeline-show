import { http } from './http'
import type { GammaReport } from '@/types/gamma-risk'
import type {
  AssessmentVO,
  AssessRequest,
  RiskFactorGroupVO,
  RiskInputsVO,
  RiskMatrixVO,
  RiskSegmentVO
} from '@/types/risk'

/** 失效风险服务 pims-risk */
export const riskApi = {
  gammaPredict: (req: { segmentId: number; theta: number; sampleCount: number; seed: number }) =>
    http.post<GammaReport>('/risk/gamma/predict', req),
  listFactors: () => http.get<RiskFactorGroupVO[]>('/risk/factors'),
  getMatrix: () => http.get<RiskMatrixVO>('/risk/matrix'),
  listSegments: () => http.get<RiskSegmentVO[]>('/risk/segments'),
  getInputs: (segmentId: number) => http.get<RiskInputsVO>(`/risk/segments/${segmentId}/inputs`),
  assess: (req: AssessRequest) => http.post<AssessmentVO>('/risk/assess', req),
  listAssessments: (segmentId: number) => http.get<AssessmentVO[]>('/risk/assessments', { segmentId })
}
