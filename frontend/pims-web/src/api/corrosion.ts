import { http } from './http'
import type {
  PowerLawReport,
  ApparentExponentVO,
  CorrosionOverviewVO,
  CorrosionSegmentVO,
  InspectionVO,
  ModelParameterVO,
  PredictionVO,
  PredictRequest
} from '@/types/corrosion'
import type { ImportResultVO } from '@/types/gis'

/** 内腐蚀服务 pims-corrosion */
export const corrosionApi = {
  getPowerLawReport: () => http.get<PowerLawReport[]>('/corrosion/power-law-report'),
  getOverview: () => http.get<CorrosionOverviewVO>('/corrosion/overview'),
  listSegments: () => http.get<CorrosionSegmentVO[]>('/corrosion/segments'),
  listInspections: (segmentId: number) => http.get<InspectionVO[]>(`/corrosion/segments/${segmentId}/inspections`),
  getParameter: (segmentId: number, n = 1.0) =>
    http.get<ModelParameterVO>(`/corrosion/segments/${segmentId}/parameter`, { n }, { silent: true }),
  predict: (req: PredictRequest) => http.post<PredictionVO>('/corrosion/predict', req),
  listApparentExponents: () => http.get<ApparentExponentVO[]>('/corrosion/apparent-exponents'),
  importInspections: (file: File) => http.upload<ImportResultVO>('/corrosion/inspections/import', file)
}
