<template>
  <div class="enterprise-location-map">
    <div class="map-head">
      <div>
        <strong>位置预览</strong>
        <span v-if="longitude && latitude">{{ longitude }}, {{ latitude }}</span>
      </div>
      <el-button size="mini" @click="$emit('manual')">手工维护坐标</el-button>
    </div>
    <div class="map-content">
      <div v-if="!hasPoint" class="map-placeholder">
        暂无坐标，请先地址解析或手工维护坐标
      </div>
      <div v-else-if="loadError" class="map-placeholder">
        地图服务暂不可用，请核对坐标后手工保存
      </div>
      <div v-else ref="mapContainer" class="map-container"></div>
    </div>
  </div>
</template>

<script>
import AMapLoader from '@amap/amap-jsapi-loader'
import { loadAmapRuntimeConfig } from '@/config/amap'

export default {
  name: 'EnterpriseLocationMap',
  props: {
    name: {
      type: String,
      default: ''
    },
    longitude: {
      type: String,
      default: ''
    },
    latitude: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      loadError: false,
      map: null,
      amapRef: null,
      marker: null
    }
  },
  computed: {
    hasPoint() {
      return !!this.longitude && !!this.latitude
    },
    point() {
      return [Number(this.longitude), Number(this.latitude)]
    }
  },
  watch: {
    point() {
      this.renderMarker()
    }
  },
  mounted() {
    if (this.hasPoint) {
      this.initMap()
    }
  },
  beforeDestroy() {
    if (this.marker) {
      this.marker.setMap(null)
      this.marker = null
    }
    if (this.map) {
      this.map.destroy()
      this.map = null
    }
  },
  methods: {
    initMap() {
      if (this.map || !this.hasPoint) {
        this.renderMarker()
        return
      }
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
          this.amapRef = AMap
          this.map = new AMap.Map(this.$refs.mapContainer, {
            center: this.point,
            zoom: 15,
            resizeEnable: true,
            dragEnable: true,
            scrollWheel: false,
            mapStyle: 'amap://styles/normal'
          })
          this.map.addControl(new AMap.Scale({ position: 'LB' }))
          this.map.addControl(new AMap.ToolBar({ position: 'RT', liteStyle: true }))
          this.renderMarker()
        })
      }).catch(() => {
        this.loadError = true
      })
    },
    renderMarker() {
      if (!this.hasPoint) {
        if (this.marker) {
          this.marker.setMap(null)
          this.marker = null
        }
        return
      }
      if (!this.map) {
        this.initMap()
        return
      }
      if (this.marker) {
        this.marker.setMap(null)
      }
      const AMap = this.amapRef || window.AMap
      if (!AMap) {
        return
      }
      this.marker = new AMap.Marker({
        position: this.point,
        anchor: 'bottom-center',
        title: this.name || '企业位置',
        label: {
          content: '<div class="enterprise-map-label">' + (this.name || '企业位置') + '</div>',
          offset: new AMap.Pixel(-44, -8)
        }
      })
      this.marker.setMap(this.map)
      this.map.setCenter(this.point)
    }
  }
}
</script>

<style scoped>
.enterprise-location-map {
  border: 1px solid #dbe6f5;
  background: #fff;
}

.map-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-bottom: 1px solid #ebeef5;
  background: #f8fbff;
}

.map-head strong {
  display: block;
  color: #17233d;
  font-size: 14px;
}

.map-head span {
  display: block;
  margin-top: 4px;
  color: #66758a;
  font-size: 12px;
}

.map-content {
  height: 260px;
  background: #edf3fb;
}

.map-container {
  width: 100%;
  height: 100%;
}

.map-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #66758a;
  font-size: 13px;
}

:deep(.enterprise-map-label) {
  min-width: 88px;
  padding: 4px 8px;
  color: #17233d;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #dbe6f5;
  box-shadow: 0 8px 18px rgba(16, 38, 68, 0.15);
}
</style>
