<template>
  <div class="gis-page">
    <div ref="mapEl" class="map-container" />

    <!-- 左侧：管段目录 -->
    <div class="overlay overlay-left">
      <SegmentListPanel
        :segments="segments"
        :selected-id="selected?.segmentId ?? null"
        :colors="pipelineColors"
        :loading="loading"
        @select="onListSelect"
      />
    </div>

    <!-- 顶部：概况与 GIS 数据导入 -->
    <div class="overlay overlay-top">
      <div class="summary-bar">
        <span class="summary-title"><EnvironmentOutlined /> 管道位置一张图</span>
        <span class="summary-item">管线 <b class="num-font">{{ pipelineCount }}</b> 条</span>
        <span class="summary-item">管段 <b class="num-font">{{ segments.length }}</b> 段</span>
        <span class="summary-item">总长 <b class="num-font">{{ totalLength }}</b> km</span>
        <a-tag v-if="schematicCount > 0" color="orange">示意坐标 {{ schematicCount }} 段</a-tag>
        <a-upload :show-upload-list="false" accept=".geojson,.json" :before-upload="onImport">
          <a-button size="small" type="primary" ghost :loading="importing"><UploadOutlined /> 导入GIS数据</a-button>
        </a-upload>
      </div>
    </div>

    <!-- 右侧：工具栏与管段详情 -->
    <div class="overlay overlay-tools" :class="{ shifted: !!selected }">
      <MapToolbar
        :basemaps="basemaps"
        :active="activeBasemap"
        @switch-basemap="switchBasemap"
        @zoom="zoomBy"
        @fit="fitAll"
      />
    </div>
    <div class="overlay overlay-right">
      <SegmentDetailPanel :segment="selected" :color="selectedColor" @close="selectSegment(null, false)" />
    </div>

    <!-- 底部：图例与坐标 -->
    <div class="overlay overlay-legend" :class="{ shifted: !!selected }">
      <MapLegend :colors="pipelineColors" />
    </div>
    <div class="overlay overlay-status">
      <span>WGS84</span>
      <span class="num-font">
        {{ pointerCoord ? `${pointerCoord[0].toFixed(5)}°E  ${pointerCoord[1].toFixed(5)}°N` : '—' }}
      </span>
      <span>级别 <span class="num-font">{{ zoomLevel }}</span></span>
    </div>

    <div v-if="loadError" class="overlay overlay-error">
      <a-result status="warning" title="地图数据加载失败" :sub-title="loadError">
        <template #extra>
          <a-button type="primary" @click="loadData">重新加载</a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { EnvironmentOutlined, UploadOutlined } from '@ant-design/icons-vue'
import { gisApi } from '@/api/gis'
import type { BasemapConfig, MapConfigVO, SegmentProperties } from '@/types/gis'
import { useSegmentStore } from '@/stores/segment'
import { usePipelineMap } from './composables/usePipelineMap'
import SegmentListPanel from './components/SegmentListPanel.vue'
import SegmentDetailPanel from './components/SegmentDetailPanel.vue'
import MapToolbar from './components/MapToolbar.vue'
import MapLegend from './components/MapLegend.vue'

defineOptions({ name: 'GisMapView' })

/** 后端配置不可用时的默认底图 */
const FALLBACK_CONFIG: MapConfigVO = {
  center: [108.1, 30.6],
  zoom: 9,
  basemaps: [
    {
      key: 'vector',
      name: '矢量底图',
      url: 'https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      attribution: '© OpenStreetMap',
      annotationUrl: null
    }
  ]
}

const route = useRoute()
const segmentStore = useSegmentStore()
const mapEl = ref<HTMLElement>()
const segments = ref<SegmentProperties[]>([])
const basemaps = ref<BasemapConfig[]>([])
const selected = ref<SegmentProperties | null>(null)
const loading = ref(false)
const importing = ref(false)
const loadError = ref('')

const {
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
} = usePipelineMap()

const pipelineCount = computed(() => new Set(segments.value.map((s) => s.pipelineName)).size)
const totalLength = computed(() => segments.value.reduce((sum, s) => sum + (s.lengthKm || 0), 0).toFixed(1))
const schematicCount = computed(() => segments.value.filter((s) => s.coordStatus === 'SCHEMATIC').length)
const selectedColor = computed(() =>
  selected.value ? pipelineColors.value[selected.value.pipelineName] || '#2470ff' : '#2470ff'
)

onSelect((props) => {
  selected.value = props
  segmentStore.select(props?.segmentId ?? null)
})

function onListSelect(segmentId: number) {
  selectSegment(segmentId, true)
}

async function loadPipelineData() {
  const collection = await gisApi.getSegmentGeoJson()
  loadPipelines(collection)
  segments.value = collection.features.map((f) => f.properties)
}

async function loadData() {
  loading.value = true
  loadError.value = ''
  try {
    await loadPipelineData()
    await nextTick()
    const querySegment = Number(route.query.segmentId)
    if (querySegment) {
      selectSegment(querySegment, true)
    } else {
      fitAll()
    }
  } catch {
    loadError.value = '无法获取管段 GIS 数据，请检查 pims-gis 服务是否可用。'
  } finally {
    loading.value = false
  }
}

async function onImport(file: File) {
  importing.value = true
  try {
    const result = await gisApi.importGeoJson(file)
    message.success(`导入完成：共 ${result.total} 条，更新 ${result.updated ?? 0} 条，跳过 ${result.skipped ?? 0} 条`)
    await loadPipelineData()
    fitAll()
  } catch {
    // 错误提示由 http 拦截器统一处理
  } finally {
    importing.value = false
  }
  return false
}

onMounted(async () => {
  let config = FALLBACK_CONFIG
  try {
    config = await gisApi.getMapConfig()
  } catch {
    // 使用默认底图
  }
  basemaps.value = config.basemaps
  if (mapEl.value) init(mapEl.value, config)
  await loadData()
})

watch(
  () => route.query.segmentId,
  (val) => {
    const id = Number(val)
    if (id && segments.value.length) selectSegment(id, true)
  }
)

onBeforeUnmount(dispose)
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.gis-page {
  position: relative;
  height: 100%;
  overflow: hidden;
}

.map-container {
  position: absolute;
  inset: 0;
  background: #dfe8f2;

  :deep(.ol-scale-line) {
    left: auto;
    right: 16px;
    bottom: 12px;
    background: rgba(36, 112, 255, 0.85);
    border-radius: 2px;
  }

  :deep(.ol-attribution) {
    bottom: 40px;
  }
}

.overlay {
  position: absolute;
  z-index: 5;
}

.overlay-left {
  left: 16px;
  top: 16px;
  bottom: 56px;
  display: flex;
}

.overlay-top {
  left: 332px;
  top: 16px;
}

.summary-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 44px;
  padding: 0 16px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 2px;
  box-shadow: 0 2px 10px rgba(0, 32, 96, 0.16);
  font-size: 14px;
  color: @text;

  b {
    font-size: 20px;
    color: @primary;
    font-weight: 400;
    margin: 0 2px;
  }
}

.summary-title {
  font-size: 16px;
  font-weight: 700;
  color: @text-main;
  display: flex;
  gap: 6px;
  align-items: center;
}

.overlay-tools {
  right: 16px;
  top: 16px;
  transition:
    right 0.25s,
    top 0.25s;

  // 详情面板展开时工具栏左移并下移到概况条下方，避免与概况条重叠
  &.shifted {
    right: 432px;
    top: 76px;
  }

  // 小于 1920 画布时概况条与底图切换同一行放不下，工具栏下移
  @media (max-width: 1800px) {
    top: 76px;
  }
}

.overlay-right {
  right: 16px;
  top: 16px;
  bottom: 56px;
  display: flex;
}

.overlay-legend {
  right: 16px;
  bottom: 44px;
  transition: right 0.25s;

  &.shifted {
    right: 432px;
  }
}

.overlay-status {
  left: 332px;
  bottom: 12px;
  display: flex;
  gap: 16px;
  padding: 4px 12px;
  font-size: 12px;
  color: #fff;
  background: rgba(11, 42, 107, 0.75);
  border-radius: 2px;
}

.overlay-error {
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(243, 244, 246, 0.85);
}
</style>
