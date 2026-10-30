import { http } from './http'
import type { ImportResultVO, MapConfigVO, SegmentFeatureCollection, SegmentVO } from '@/types/gis'

export interface SegmentQuery {
  mine?: string
  pipelineName?: string
  keyword?: string
}

/** 管道位置服务 pims-gis */
export const gisApi = {
  listSegments: (query?: SegmentQuery) => http.get<SegmentVO[]>('/gis/segments', query as Record<string, unknown>),
  getSegmentGeoJson: () => http.get<SegmentFeatureCollection>('/gis/segments/geojson'),
  getSegment: (segmentId: number) => http.get<SegmentVO>(`/gis/segments/${segmentId}`),
  importGeoJson: (file: File) => http.upload<ImportResultVO>('/gis/segments/geojson/import', file),
  getMapConfig: () => http.get<MapConfigVO>('/gis/map/config')
}
