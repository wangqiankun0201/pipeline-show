/** 坐标状态：示意坐标 / 实测坐标 */
export type CoordStatus = 'SCHEMATIC' | 'SURVEYED'

export interface LineStringGeometry {
  type: 'LineString' | 'MultiLineString'
  coordinates: number[][] | number[][][]
}

/** 管段（位置）信息 */
export interface SegmentVO {
  segmentId: number
  mine: string
  workArea: string | null
  pipelineName: string
  segmentName: string
  commissionDate: string | null
  outerDiameterMm: number | null
  wallThicknessMm: number | null
  materialGrade: string | null
  designPressureMpa: number | null
  mediumType: string | null
  lengthKm: number | null
  startName: string | null
  endName: string | null
  startLon: number | null
  startLat: number | null
  endLon: number | null
  endLat: number | null
  coordStatus: CoordStatus
  coordStatusName: string
  dataSource: string | null
  geometry?: LineStringGeometry | null
}

export type SegmentProperties = Omit<SegmentVO, 'geometry'>

export interface SegmentFeatureCollection {
  type: 'FeatureCollection'
  features: Array<{
    type: 'Feature'
    id?: number | string
    geometry: LineStringGeometry
    properties: SegmentProperties
  }>
}

export interface BasemapConfig {
  key: string
  name: string
  url: string
  attribution: string | null
  annotationUrl: string | null
}

export interface MapConfigVO {
  center: [number, number]
  zoom: number
  basemaps: BasemapConfig[]
}

export interface ImportResultVO {
  total: number
  updated?: number
  imported?: number
  skipped?: number
  segmentCount?: number
  messages: string[]
}
