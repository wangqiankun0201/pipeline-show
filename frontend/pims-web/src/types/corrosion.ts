export interface PowerLawReport {
  segmentId: number
  segmentName: string
  commissionDate: string
  wallThicknessMm: number
  kValue: number
  n: number
  cutoffDate: string
  comparison: { date: string; tYears: number; measuredDepthMm: number; calculatedDepthMm: number; errorMm: number }[]
  curve: CurvePoint[]
  annual: { year: number; date: string; depthMm: number | null; stopped: boolean }[]
}

/** 数据可信等级 A 缺陷匹配 / B 两期管段 / C 单期锚定 / D 缺失 */
export type DataGrade = 'A' | 'B' | 'C' | 'D'

/** 越界标识 */
export type LimitFlag = 'NORMAL' | 'ATTENTION' | 'MODEL_OUT_OF_RANGE'

export interface CorrosionOverviewVO {
  segmentCount: number
  inspectionCount: number
  predictableCount: number
  medianK: number | null
  maxDepthPct: number | null
  latestInspectionDate: string | null
  modelName: string
  modelVersion: string
  baseN: number
}

export interface CorrosionSegmentVO {
  segmentId: number
  mine: string
  workArea: string | null
  pipelineName: string
  segmentName: string
  commissionDate: string | null
  wallThicknessMm: number | null
  inspectionCount: number
  latestInspectionDate: string | null
  latestMaxDepthPct: number | null
  d0Mm: number | null
  tYears: number | null
  kValue: number | null
  predictable: boolean
  grade: DataGrade
  gradeName: string
  note: string | null
}

export interface InspectionVO {
  inspectionId: number
  sourceSegmentId: number
  segmentName: string
  inspectionDate: string | null
  serviceYears: number | null
  outerDiameterMm: number | null
  wallThicknessMm: number | null
  materialGrade: string | null
  designPressureMpa: number | null
  operatingPressureMpa: number | null
  operatingPressureText: string | null
  pressureFactor: number | null
  operatingTemperature: string | null
  mediumType: string | null
  liquidAccumulation: string | null
  co2MolPct: number | null
  h2sMolPct: number | null
  o2MolPct: number | null
  waterCutPct: number | null
  chlorideMgL: number | null
  ph: number | null
  inhibitorDosing: string | null
  inhibitorConcentration: string | null
  piggingCycleDays: number | null
  defectCount: number | null
  maxDepthPct: number | null
  maxDepthMm: number | null
  avgErf: number | null
  maxErf: number | null
  remark: string | null
  isLatest: boolean
}

export interface ModelParameterVO {
  segmentId: number
  segmentName: string
  modelName: string
  modelVersion: string
  n: number
  kValue: number
  kUnit: string
  d0Mm: number
  tYears: number
  maxDepthPct: number
  wallThicknessMm: number
  anchorDate: string
  commissionDate: string
  timeOrigin: string
  timeOriginName: string
  grade: DataGrade
  gradeName: string
  parameterSource: string
}

export interface PredictRequest {
  segmentId: number
  targetDate?: string | null
  yearsAfterIli?: number | null
  scenarioN: number
  compareScenarios: boolean
  curveEndYears?: number | null
}

export interface CurvePoint {
  date: string
  tYears: number
  depthMm: number
}

export interface ScenarioResultVO {
  n: number
  kValue: number
  predDepthMm: number
  depthRatio: number
  remainingWallMm: number
  limitFlag: LimitFlag
  limitFlagName: string
  outOfRangeDate: string | null
  curve?: CurvePoint[]
}

export interface InspectionPointVO {
  date: string
  tYears: number
  depthMm: number
  depthPct: number
  isAnchor: boolean
}

export interface PredictionVO {
  calcId: string
  calcTime: string
  parameter: ModelParameterVO
  targetDate: string
  deltaYears: number
  main: ScenarioResultVO
  scenarios: ScenarioResultVO[]
  inspectionPoints: InspectionPointVO[]
  warnings: string[]
}

export interface ApparentExponentVO {
  sourceSegmentId: number
  segmentName: string
  date1: string
  date2: string
  t1Years: number
  t2Years: number
  d1Pct: number
  d2Pct: number
  nApp: number
  direction: 'INCREASE' | 'DECREASE' | string
  directionName: string
  judgement: string
}
