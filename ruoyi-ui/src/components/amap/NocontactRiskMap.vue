<template>
  <div ref="rootElement" class="risk-map">
    <div v-if="loadError" class="risk-map-fallback">
      <div class="fallback-title">地图服务暂不可用</div>
      <div class="fallback-subtitle">已切换为区域风险列表</div>
      <div class="fallback-list">
        <button
          v-for="item in normalizedRegions"
          :key="item.name"
          class="fallback-item"
          :class="{ active: item.name === selectedName }"
          @click="selectRegion(item)"
        >
          <span>{{ item.name }}</span>
          <strong>{{ item.value }}</strong>
        </button>
      </div>
    </div>
    <div ref="mapContainer" class="risk-map-container"></div>
    <div
      v-if="activeInfo"
      class="risk-map-info-panel"
      :style="infoPanelStyle"
      @click.stop
      @mousedown.stop
      @touchstart.stop
    >
      <strong>{{ activeInfo.name }}风险热度 {{ activeInfo.value }}</strong>
      <span>重点指标：{{ activeInfo.metric || '-' }}</span>
      <span>一级 {{ activeInfo.level1 || 0 }} / 二级 {{ activeInfo.level2 || 0 }} / 三级 {{ activeInfo.level3 || 0 }}</span>
      <span>待处理 {{ activeInfo.pending || 0 }} 项，处置率 {{ activeInfo.handledRate || 0 }}%</span>
    </div>
  </div>
</template>

<script>
import AMapLoader from '@amap/amap-jsapi-loader'
import { loadAmapRuntimeConfig } from '@/config/amap'

const DEFAULT_CENTER = {
  '长沙市': [112.938814, 28.228209],
  '株洲市': [113.151737, 27.835806],
  '湘潭市': [112.944052, 27.82973],
  '衡阳市': [112.57195, 26.89324],
  '邵阳市': [111.467674, 27.239527],
  '岳阳市': [113.132855, 29.37029],
  '常德市': [111.698497, 29.031673],
  '张家界市': [110.479921, 29.127401],
  '益阳市': [112.355042, 28.570066],
  '郴州市': [113.032067, 25.793589],
  '永州市': [111.608019, 26.434516],
  '怀化市': [109.97824, 27.550082],
  '娄底市': [112.008497, 27.728136],
  '湘西州': [109.739735, 28.314296],
  '湘西土家族苗族自治州': [109.739735, 28.314296]
}

export default {
  name: 'NocontactRiskMap',
  props: {
    regions: {
      type: Array,
      default: () => []
    },
    selectedRegion: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      loadError: false,
      selectedName: '',
      activeInfo: null,
      infoPanelStyle: { left: '12px', top: '12px' },
      map: null,
      amapRef: null,
      markers: [],
      heatMarkers: [],
      infoPanelRaf: 0,
      disposed: false
    }
  },
  computed: {
    normalizedRegions() {
      return (this.regions || [])
        .map(item => {
          const center = Array.isArray(item.center) && item.center.length === 2 ? item.center : DEFAULT_CENTER[item.name]
          if (!center) {
            return null
          }
          return {
            ...item,
            center,
            value: Number(item.value || item.count || 0)
          }
        })
        .filter(Boolean)
    }
  },
  watch: {
    regions: {
      deep: true,
      handler() {
        if (!this.map || !this.amapRef) {
          return
        }
        this.renderRiskLayer()
      }
    },
    selectedRegion(value) {
      this.selectedName = value || ''
      this.updateMarkerActiveState()
    }
  },
  mounted() {
    this.selectedName = this.selectedRegion || ''
    document.addEventListener('pointerdown', this.handleDocumentPointerDown, true)
    this.initMap()
  },
  beforeDestroy() {
    this.disposed = true
    document.removeEventListener('pointerdown', this.handleDocumentPointerDown, true)
    if (this.infoPanelRaf) {
      window.cancelAnimationFrame(this.infoPanelRaf)
      this.infoPanelRaf = 0
    }
    this.clearMapObjects()
    if (this.map) {
      this.map.destroy()
      this.map = null
    }
  },
  methods: {
    initMap() {
      loadAmapRuntimeConfig().then(config => {
        if (!config.key || !config.securityJsCode) {
          this.loadError = true
          return
        }
        window._AMapSecurityConfig = { securityJsCode: config.securityJsCode }
        return AMapLoader.load({
          key: config.key,
          version: '2.0',
          plugins: ['AMap.Scale', 'AMap.ToolBar']
        }).then(AMap => {
          if (this.disposed || !this.$refs.mapContainer) {
            return
          }
          this.amapRef = AMap
          this.map = new AMap.Map(this.$refs.mapContainer, {
            center: [112.9, 27.95],
            zoom: 7.25,
            viewMode: '2D',
            resizeEnable: true,
            animateEnable: true,
            dragEnable: true,
            scrollWheel: false,
            doubleClickZoom: false,
            keyboardEnable: false,
            touchZoom: false,
            mapStyle: 'amap://styles/normal',
            features: ['bg', 'road', 'point']
          })
          this.map.addControl(new AMap.Scale({ position: 'LB' }))
          this.map.addControl(new AMap.ToolBar({ position: 'RT', liteStyle: true }))
          this.map.on('click', () => this.selectRegion(null))
          ;['mapmove', 'zoomchange', 'moveend', 'zoomend', 'resize'].forEach(name => {
            this.map.on(name, this.scheduleInfoPanelUpdate)
          })
          this.renderRiskLayer()
        })
      }).catch(error => {
        console.error('AMap load failed', error)
        this.loadError = true
      })
    },
    clearMapObjects() {
      this.markers.forEach(marker => marker.setMap(null))
      this.heatMarkers.forEach(marker => marker.setMap(null))
      this.markers = []
      this.heatMarkers = []
      this.activeInfo = null
    },
    renderRiskLayer() {
      this.clearMapObjects()
      if (!this.normalizedRegions.length) {
        return
      }
      const maxValue = Math.max.apply(null, this.normalizedRegions.map(item => item.value).concat([1]))
      this.heatMarkers = this.normalizedRegions.map(item => this.createHeatMarker(item, maxValue))
      this.markers = this.normalizedRegions.map(item => this.createRiskMarker(item))
      this.map.setFitView(this.markers, false, [18, 18, 18, 18], 7.25)
      this.updateMarkerActiveState()
    },
    createHeatMarker(region, maxValue) {
      const ratio = region.value / maxValue
      const size = Math.round(72 + ratio * 72)
      const opacity = Math.min(0.9, 0.48 + ratio * 0.32)
      const element = document.createElement('div')
      element.className = 'risk-map-heat-spot'
      element.style.setProperty('--spot-size', size + 'px')
      element.style.setProperty('--spot-opacity', String(opacity))
      const marker = new this.amapRef.Marker({
        position: region.center,
        anchor: 'center',
        zIndex: 80,
        clickable: false,
        bubble: false,
        content: element
      })
      marker.setMap(this.map)
      return marker
    },
    createRiskMarker(region) {
      const element = this.markerContent(region)
      const marker = new this.amapRef.Marker({
        position: region.center,
        anchor: 'center',
        zIndex: 120,
        bubble: false,
        content: element
      })
      marker.setMap(this.map)
      this.bindMarkerDomEvents(element, region)
      return marker
    },
    markerContent(region) {
      const element = document.createElement('div')
      element.className = 'risk-map-marker'
      element.dataset.riskName = region.name
      element.innerHTML = '<i></i><div><b>' + region.name + '</b><strong>' + region.value + '</strong></div>'
      return element
    },
    bindMarkerDomEvents(element, region) {
      const stopDomEvent = event => {
        event.stopPropagation()
        event.preventDefault()
      }
      const disableDrag = event => {
        this.map.setStatus({ dragEnable: false })
        stopDomEvent(event)
      }
      const enableDrag = () => {
        if (this.map) {
          this.map.setStatus({ dragEnable: true })
        }
      }
      element.addEventListener('mousedown', event => {
        disableDrag(event)
        document.addEventListener('mouseup', enableDrag, { once: true })
      })
      element.addEventListener('touchstart', event => {
        disableDrag(event)
        document.addEventListener('touchend', enableDrag, { once: true })
        document.addEventListener('touchcancel', enableDrag, { once: true })
      }, { passive: false })
      element.addEventListener('click', event => {
        stopDomEvent(event)
        this.selectRegion(region)
      })
      element.addEventListener('dblclick', stopDomEvent)
    },
    handleDocumentPointerDown(event) {
      if (!this.activeInfo || (this.$refs.rootElement && this.$refs.rootElement.contains(event.target))) {
        return
      }
      this.selectRegion(null)
    },
    selectRegion(region) {
      this.selectedName = region ? region.name : ''
      this.activeInfo = region || null
      this.updateMarkerActiveState()
      if (region) {
        this.scheduleInfoPanelUpdate()
      }
      this.$emit('select-region', region || null)
    },
    scheduleInfoPanelUpdate() {
      if (!this.activeInfo || this.infoPanelRaf) {
        return
      }
      this.infoPanelRaf = window.requestAnimationFrame(() => {
        this.infoPanelRaf = 0
        this.updateInfoPanelPosition()
      })
    },
    updateInfoPanelPosition() {
      if (!this.map || !this.$refs.rootElement || !this.activeInfo) {
        return
      }
      const pixel = this.map.lngLatToContainer(this.activeInfo.center)
      const container = this.$refs.rootElement.getBoundingClientRect()
      const panel = this.$refs.rootElement.querySelector('.risk-map-info-panel')
      const panelWidth = panel ? panel.offsetWidth : 248
      const panelHeight = panel ? panel.offsetHeight : 124
      const gap = 12
      const padding = 8
      let left = pixel.x + gap
      let top = pixel.y - panelHeight - gap
      if (left + panelWidth + padding > container.width) {
        left = pixel.x - panelWidth - gap
      }
      if (top < padding) {
        top = pixel.y + gap
      }
      left = this.clamp(left, padding, Math.max(padding, container.width - panelWidth - padding))
      top = this.clamp(top, padding, Math.max(padding, container.height - panelHeight - padding))
      this.infoPanelStyle = {
        left: Math.round(left) + 'px',
        top: Math.round(top) + 'px'
      }
    },
    clamp(value, min, max) {
      return Math.min(Math.max(value, min), max)
    },
    updateMarkerActiveState() {
      const container = this.$refs.mapContainer
      if (!container) {
        return
      }
      container.querySelectorAll('.risk-map-marker').forEach(element => {
        element.classList.toggle('active', element.dataset.riskName === this.selectedName)
      })
    }
  }
}
</script>

<style scoped>
.risk-map {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 320px;
  overflow: hidden;
  background: #edf3fb;
  border: 1px solid #dbe6f5;
}

.risk-map-container {
  width: 100%;
  height: 100%;
  min-height: 0;
}

:deep(.risk-map-heat-spot) {
  width: var(--spot-size);
  height: var(--spot-size);
  border-radius: 50%;
  background: radial-gradient(circle, rgba(226, 25, 31, 0.88) 0%, rgba(232, 37, 43, 0.58) 24%, rgba(238, 57, 65, 0.34) 48%, rgba(244, 91, 99, 0.16) 66%, rgba(244, 91, 99, 0) 78%);
  filter: blur(3px);
  mix-blend-mode: multiply;
  opacity: var(--spot-opacity);
  pointer-events: none;
  will-change: transform;
}

.risk-map-fallback {
  position: absolute;
  inset: 14px;
  z-index: 5;
  padding: 14px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid #dbe6f5;
}

.fallback-title {
  color: #17233d;
  font-weight: 700;
}

.fallback-subtitle {
  margin-top: 4px;
  color: #66758a;
  font-size: 12px;
}

.fallback-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.fallback-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  height: 42px;
  padding: 0 10px;
  border: 1px solid #e0e8f5;
  background: #f7faff;
  color: #334e75;
  cursor: pointer;
}

.fallback-item.active {
  border-color: #e5484d;
  color: #c92127;
  background: #fff2f2;
}

.fallback-item strong {
  color: #d92d35;
  font-size: 18px;
}

:deep(.amap-copyright),
:deep(.amap-logo) {
  opacity: 0.65;
}

:deep(.risk-map-marker) {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #ffffff;
  font-family: Arial, 'Microsoft YaHei', sans-serif;
  text-shadow: -1px -1px 0 #24303f, 1px -1px 0 #24303f, -1px 1px 0 #24303f, 1px 1px 0 #24303f, 0 2px 4px rgba(0, 0, 0, 0.35);
  transform: translate(-8px, -6px);
  cursor: pointer;
  pointer-events: auto;
  user-select: none;
}

:deep(.risk-map-marker i) {
  width: 10px;
  height: 10px;
  border: 2px solid #ffffff;
  border-radius: 50%;
  background: #ee2f37;
  box-shadow: 0 0 0 2px rgba(238, 47, 55, 0.75);
}

:deep(.risk-map-marker b),
:deep(.risk-map-marker strong) {
  display: block;
  line-height: 1.05;
  font-weight: 800;
}

:deep(.risk-map-marker b) {
  font-size: 16px;
}

:deep(.risk-map-marker strong) {
  margin-top: 3px;
  font-size: 20px;
}

:deep(.risk-map-marker.active) {
  transform: translate(-8px, -6px) scale(1.08);
}

:deep(.risk-map-marker.active i) {
  box-shadow: 0 0 0 3px rgba(238, 47, 55, 0.82), 0 0 18px rgba(238, 47, 55, 0.92);
}

.risk-map-info-panel {
  position: absolute;
  z-index: 8;
  box-sizing: border-box;
  width: 218px;
  display: grid;
  gap: 6px;
  padding: 10px 12px;
  color: #26364d;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #dbe6f5;
  box-shadow: 0 12px 28px rgba(16, 38, 68, 0.18);
  font-size: 12px;
}

.risk-map-info-panel strong {
  color: #d92d35;
  font-size: 14px;
}

.risk-map-info-panel span {
  display: block;
}

@media (max-width: 900px) {
  .fallback-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  :deep(.risk-map-marker b) {
    font-size: 18px;
  }

  :deep(.risk-map-marker strong) {
    font-size: 22px;
  }
}
</style>
