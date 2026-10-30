import { shallowRef, ref } from 'vue'
import OlMap from 'ol/Map'
import View from 'ol/View'
import TileLayer from 'ol/layer/Tile'
import VectorLayer from 'ol/layer/Vector'
import VectorSource from 'ol/source/Vector'
import XYZ from 'ol/source/XYZ'
import GeoJSON from 'ol/format/GeoJSON'
import Feature, { type FeatureLike } from 'ol/Feature'
import Point from 'ol/geom/Point'
import LineString from 'ol/geom/LineString'
import MultiLineString from 'ol/geom/MultiLineString'
import { Circle as CircleStyle, Fill, Stroke, Style, Text } from 'ol/style'
import { ScaleLine, defaults as defaultControls } from 'ol/control'
import { fromLonLat, toLonLat } from 'ol/proj'
import { createEmpty, extend, isEmpty, type Extent } from 'ol/extent'
import type { BasemapConfig, MapConfigVO, SegmentFeatureCollection, SegmentProperties } from '@/types/gis'
import { pipelinePalette } from '@/styles/theme'

/** 标注显示的分辨率阈值（约 zoom ≥ 11） */
const LABEL_RESOLUTION = 80
const DATA_PROJECTION = 'EPSG:4326'
const VIEW_PROJECTION = 'EPSG:3857'

/** 十六进制颜色加透明度 */
function withAlpha(hex: string, alpha: number): string {
  const v = hex.replace('#', '')
  const r = parseInt(v.substring(0, 2), 16)
  const g = parseInt(v.substring(2, 4), 16)
  const b = parseInt(v.substring(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

/** 取线要素的首末点 */
function getEndpoints(feature: Feature): [number[], number[]] | null {
  const geom = feature.getGeometry()
  if (geom instanceof LineString) {
    return [geom.getFirstCoordinate(), geom.getLastCoordinate()]
  }
  if (geom instanceof MultiLineString) {
    const lines = geom.getLineStrings()
    if (!lines.length) return null
    return [lines[0].getFirstCoordinate(), lines[lines.length - 1].getLastCoordinate()]
  }
  return null
}

/**
 * 管道地图：底图切换、管线高亮（光晕 + 实线 + 流动虚线）、悬停/点击选中
 */
export function usePipelineMap() {
  const map = shallowRef<OlMap | null>(null)
  const pointerCoord = ref<[number, number] | null>(null)
  const zoomLevel = ref<number>(0)
  const activeBasemap = ref<string>('')
  const pipelineColors = ref<Record<string, string>>({})

  let hoveredId: number | null = null
  let selectedId: number | null = null
  let dashOffset = 0
  let animTimer: number | undefined
  let onSelectHandler: ((props: SegmentProperties | null) => void) | null = null

  const baseLayers: TileLayer<XYZ>[] = []
  const pipelineSource = new VectorSource()
  const markerSource = new VectorSource()

  function colorOf(pipelineName: string): string {
    return pipelineColors.value[pipelineName] || pipelinePalette[0]
  }

  /** 管线静态样式：外发光 + 深色描边 + 主色实线 + 标注 */
  function pipelineStyle(feature: FeatureLike, resolution: number): Style[] {
    const props = feature.getProperties() as SegmentProperties
    const color = colorOf(props.pipelineName)
    const id = props.segmentId
    const isSelected = id === selectedId
    const isHovered = id === hoveredId
    const emphasis = isSelected || isHovered
    const styles: Style[] = [
      new Style({ stroke: new Stroke({ color: withAlpha(color, emphasis ? 0.35 : 0.2), width: emphasis ? 22 : 16 }) }),
      new Style({ stroke: new Stroke({ color: withAlpha(color, emphasis ? 0.55 : 0.35), width: emphasis ? 12 : 9 }) }),
      new Style({ stroke: new Stroke({ color: '#0b2a6b', width: emphasis ? 7 : 6 }), zIndex: 1 }),
      new Style({ stroke: new Stroke({ color, width: emphasis ? 5 : 4 }), zIndex: 2 })
    ]
    if (isSelected) {
      // 选中光晕使用中性深蓝，避免与管线配色混淆
      styles.unshift(new Style({ stroke: new Stroke({ color: 'rgba(11, 42, 107, 0.28)', width: 30 }) }))
    }
    if (resolution < LABEL_RESOLUTION || emphasis) {
      styles.push(
        new Style({
          text: new Text({
            text: props.segmentName,
            placement: 'line',
            font: 'bold 13px Microsoft YaHei',
            fill: new Fill({ color: '#0b2a6b' }),
            stroke: new Stroke({ color: '#fff', width: 4 }),
            offsetY: -14,
            overflow: true
          }),
          zIndex: 4
        })
      )
    }
    return styles
  }

  /** 流动虚线（动画层） */
  function flowStyle(feature: FeatureLike): Style {
    const props = feature.getProperties() as SegmentProperties
    const emphasis = props.segmentId === selectedId || props.segmentId === hoveredId
    return new Style({
      stroke: new Stroke({
        color: 'rgba(255, 255, 255, 0.95)',
        width: emphasis ? 2.5 : 2,
        lineDash: [10, 14],
        lineDashOffset: dashOffset,
        lineCap: 'round'
      }),
      zIndex: 3
    })
  }

  /** 起终点标记 */
  function markerStyle(feature: FeatureLike, resolution: number): Style {
    const kind = feature.get('kind') as 'start' | 'end'
    const color = colorOf(feature.get('pipelineName'))
    const showLabel = resolution < LABEL_RESOLUTION / 2
    return new Style({
      image: new CircleStyle({
        radius: kind === 'start' ? 6 : 5,
        fill: new Fill({ color: kind === 'start' ? color : '#fff' }),
        stroke: new Stroke({ color: kind === 'start' ? '#fff' : color, width: 2.5 })
      }),
      text:
        showLabel && feature.get('name')
          ? new Text({
              text: feature.get('name'),
              font: '12px Microsoft YaHei',
              offsetY: 16,
              fill: new Fill({ color: '#333' }),
              stroke: new Stroke({ color: '#fff', width: 3 })
            })
          : undefined,
      zIndex: 5
    })
  }

  const pipelineLayer = new VectorLayer({ source: pipelineSource, style: pipelineStyle, zIndex: 10 })
  const flowLayer = new VectorLayer({ source: pipelineSource, style: flowStyle, zIndex: 11 })
  const markerLayer = new VectorLayer({ source: markerSource, style: markerStyle, zIndex: 12 })

  function createBaseLayers(basemaps: BasemapConfig[]) {
    basemaps.forEach((bm, index) => {
      const layer = new TileLayer({
        source: new XYZ({ url: bm.url, attributions: bm.attribution || undefined, crossOrigin: 'anonymous' }),
        visible: index === 0,
        zIndex: 0
      })
      layer.set('basemapKey', bm.key)
      baseLayers.push(layer)
      if (bm.annotationUrl) {
        const anno = new TileLayer({ source: new XYZ({ url: bm.annotationUrl }), visible: index === 0, zIndex: 1 })
        anno.set('basemapKey', bm.key)
        baseLayers.push(anno)
      }
    })
    activeBasemap.value = basemaps[0]?.key || ''
  }

  function init(target: HTMLElement, config: MapConfigVO) {
    createBaseLayers(config.basemaps || [])
    const instance = new OlMap({
      target,
      layers: [...baseLayers, pipelineLayer, flowLayer, markerLayer],
      controls: defaultControls({ zoom: false, rotate: false, attribution: true }).extend([
        new ScaleLine({ units: 'metric', bar: false })
      ]),
      view: new View({
        projection: VIEW_PROJECTION,
        center: fromLonLat(config.center || [108.1, 30.6]),
        zoom: config.zoom || 9,
        minZoom: 3,
        maxZoom: 18
      })
    })
    zoomLevel.value = Math.round(instance.getView().getZoom() || 0)
    instance.getView().on('change:resolution', () => {
      zoomLevel.value = Math.round((instance.getView().getZoom() || 0) * 10) / 10
    })

    instance.on('pointermove', (evt) => {
      if (evt.dragging) return
      const lonLat = toLonLat(evt.coordinate)
      pointerCoord.value = [lonLat[0], lonLat[1]]
      const hit = instance.forEachFeatureAtPixel(evt.pixel, (f) => f, {
        layerFilter: (l) => l === pipelineLayer,
        hitTolerance: 6
      })
      const id = hit ? (hit.get('segmentId') as number) : null
      if (id !== hoveredId) {
        hoveredId = id
        pipelineLayer.changed()
      }
      instance.getTargetElement().style.cursor = hit ? 'pointer' : ''
    })

    instance.on('singleclick', (evt) => {
      const hit = instance.forEachFeatureAtPixel(evt.pixel, (f) => f, {
        layerFilter: (l) => l === pipelineLayer,
        hitTolerance: 6
      })
      const id = hit ? (hit.get('segmentId') as number) : null
      selectSegment(id, false)
    })

    // 流动虚线动画：仅重绘动画层
    animTimer = window.setInterval(() => {
      dashOffset = (dashOffset - 1.2) % 240
      flowLayer.changed()
    }, 40)

    map.value = instance
  }

  /** 加载管线数据 */
  function loadPipelines(collection: SegmentFeatureCollection) {
    pipelineSource.clear()
    markerSource.clear()
    const format = new GeoJSON()
    const features = format.readFeatures(collection, {
      dataProjection: DATA_PROJECTION,
      featureProjection: VIEW_PROJECTION
    }) as Feature[]

    // 按管线分配颜色
    const names = Array.from(new Set(features.map((f) => String(f.get('pipelineName')))))
    const palette: Record<string, string> = {}
    names.forEach((n, i) => (palette[n] = pipelinePalette[i % pipelinePalette.length]))
    pipelineColors.value = palette

    features.forEach((f) => {
      f.setId(f.get('segmentId'))
      const ends = getEndpoints(f)
      if (!ends) return
      markerSource.addFeatures([
        new Feature({
          geometry: new Point(ends[0]),
          kind: 'start',
          name: f.get('startName'),
          pipelineName: f.get('pipelineName')
        }),
        new Feature({
          geometry: new Point(ends[1]),
          kind: 'end',
          name: f.get('endName'),
          pipelineName: f.get('pipelineName')
        })
      ])
    })
    pipelineSource.addFeatures(features)
  }

  function getFeatureProps(segmentId: number): SegmentProperties | null {
    const f = pipelineSource.getFeatureById(segmentId)
    if (!f) return null
    const { geometry: _geometry, ...props } = f.getProperties()
    void _geometry
    return props as SegmentProperties
  }

  /** 选中管段；zoomTo 为 true 时缩放到管段范围 */
  function selectSegment(segmentId: number | null, zoomTo = true) {
    selectedId = segmentId
    pipelineLayer.changed()
    flowLayer.changed()
    const props = segmentId === null ? null : getFeatureProps(segmentId)
    if (zoomTo && segmentId !== null) {
      const f = pipelineSource.getFeatureById(segmentId)
      const geom = f?.getGeometry()
      if (geom) fitExtent(geom.getExtent(), 15)
    }
    onSelectHandler?.(props)
  }

  function onSelect(handler: (props: SegmentProperties | null) => void) {
    onSelectHandler = handler
  }

  function fitExtent(extent: Extent, maxZoom = 13) {
    map.value?.getView().fit(extent, { padding: [80, 420, 80, 360], duration: 500, maxZoom })
  }

  /** 缩放到全部管线 */
  function fitAll() {
    const extent = createEmpty()
    pipelineSource.getFeatures().forEach((f) => {
      const g = f.getGeometry()
      if (g) extend(extent, g.getExtent())
    })
    if (!isEmpty(extent)) fitExtent(extent, 12)
  }

  function zoomBy(delta: number) {
    const view = map.value?.getView()
    if (!view) return
    view.animate({ zoom: (view.getZoom() || 0) + delta, duration: 250 })
  }

  function switchBasemap(key: string) {
    activeBasemap.value = key
    baseLayers.forEach((l) => l.setVisible(l.get('basemapKey') === key))
  }

  function dispose() {
    window.clearInterval(animTimer)
    map.value?.setTarget(undefined)
    map.value = null
  }

  return {
    map,
    pointerCoord,
    zoomLevel,
    activeBasemap,
    pipelineColors,
    init,
    loadPipelines,
    selectSegment,
    onSelect,
    fitAll,
    zoomBy,
    switchBasemap,
    dispose
  }
}
