import type { LimitFlag } from './corrosion'
import type { OptionItem } from './common'

/** 因素数据来源 */
export type FactorSource = 'CORROSION' | 'GIS' | 'MANUAL'

export interface RiskFactorVO {
  code: string
  name: string
  unit: string | null
  inputType: 'number' | 'select' | 'text'
  source: FactorSource
  sourceName: string
  options: OptionItem[] | null
  required: boolean
  min: number | null
  max: number | null
}

export interface RiskFactorGroupVO {
  groupCode: 'POF' | 'COF' | string
  groupName: string
  factors: RiskFactorVO[]
}

export interface RiskLevelDef {
  code: string
  name: string
  color: string
}

export interface RiskMatrixVO {
  pofLevels: Array<{ level: number; name: string }>
  cofLevels: Array<{ level: number; name: string }>
  riskLevels: RiskLevelDef[]
  cells: string[][]
}

/** 评价状态：模型待接入 / 已计算 等 */
export type AssessStatus = 'MODEL_PENDING' | 'CALCULATED' | 'FAILED' | string

export interface RiskSegmentVO {
  segmentId: number
  mine: string
  pipelineName: string
  segmentName: string
  predDepthMm: number | null
  depthRatio: number | null
  corrosionLimitFlag: LimitFlag | null
  pofLevel: number | null
  cofLevel: number | null
  riskLevel: string | null
  riskLevelName: string | null
  status: AssessStatus
  statusName: string
  lastAssessTime: string | null
}

export type FactorValue = string | number | null

export interface RiskInputsVO {
  segmentId: number
  values: Record<string, FactorValue>
}

export interface AssessRequest {
  segmentId: number
  evaluateYears: number
  inputs: Record<string, FactorValue>
}

export interface AssessmentVO {
  assessmentId: string
  segmentId: number
  segmentName: string
  assessTime: string
  status: AssessStatus
  statusName: string
  pofLevel: number | null
  cofLevel: number | null
  pofValue: number | null
  cofValue: number | null
  riskLevel: string | null
  riskLevelName: string | null
  message: string | null
  inputs: Record<string, FactorValue>
}
