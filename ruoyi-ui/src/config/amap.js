import AMapLoader from '@amap/amap-jsapi-loader'
import { getSupportPublicSettings } from '@/api/nocontact/support'

let runtimeConfig = null
let amapLoaderPromise = null
const DEFAULT_AMAP_CONFIG = {
  key: '',
  securityJsCode: ''
}

export function loadAmapRuntimeConfig() {
  if (runtimeConfig) {
    return Promise.resolve(runtimeConfig)
  }
  return getSupportPublicSettings().then(response => {
    const data = response.data || {}
    runtimeConfig = {
      key: data.amapFrontendKey || DEFAULT_AMAP_CONFIG.key,
      securityJsCode: data.amapSecurityJsCode || DEFAULT_AMAP_CONFIG.securityJsCode
    }
    return runtimeConfig
  }).catch(() => {
    runtimeConfig = { ...DEFAULT_AMAP_CONFIG }
    return runtimeConfig
  })
}

export function loadAmapJsApi(plugins = []) {
  return loadAmapRuntimeConfig().then(config => {
    if (!config.key || !config.securityJsCode) {
      throw new Error('高德地图配置缺失')
    }
    window._AMapSecurityConfig = { securityJsCode: config.securityJsCode }
    if (!amapLoaderPromise) {
      amapLoaderPromise = AMapLoader.load({
        key: config.key,
        version: '2.0',
        plugins
      })
    }
    return amapLoaderPromise.then(AMap => {
      const uniquePlugins = plugins.filter(Boolean)
      if (!uniquePlugins.length || !AMap.plugin) {
        return AMap
      }
      return new Promise((resolve) => {
        AMap.plugin(uniquePlugins, () => resolve(AMap))
      })
    })
  })
}

export function geocodeWithAmapJs(regionName, address) {
  return loadAmapJsApi(['AMap.Geocoder']).then(AMap => {
    return new Promise((resolve, reject) => {
      const geocoder = new AMap.Geocoder({
        city: regionName || '湖南省'
      })
      geocoder.getLocation(address, (status, result) => {
        if (status !== 'complete' || !result || !result.geocodes || !result.geocodes.length) {
          reject(new Error('地址解析失败'))
          return
        }
        const first = result.geocodes[0]
        const location = first.location || {}
        if (location.lng === undefined || location.lat === undefined) {
          reject(new Error('地址解析失败'))
          return
        }
        resolve({
          longitude: String(location.lng),
          latitude: String(location.lat),
          formattedAddress: first.formattedAddress || first.formatted_address || ''
        })
      })
    })
  })
}
