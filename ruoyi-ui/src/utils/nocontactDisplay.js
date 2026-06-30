import { parseTime } from '@/utils/ruoyi'

const REPORT_TYPE_LABELS = {
  overall: '总报告',
  region: '分地区报告',
  indicator: '分指标报告'
}

const BINARY_STATUS_LABELS = {
  0: '启用',
  1: '停用'
}

const INTEGRATION_TYPE_LABELS = {
  api: 'API接口'
}

const SYNC_FREQUENCY_LABELS = {
  realtime: '实时',
  hourly: '每小时',
  daily: '每日',
  weekly: '每周'
}

const WARNING_LEVEL_LABELS = {
  1: '一级',
  2: '二级',
  3: '三级'
}

const WARNING_LEVEL_COLOR_LABELS = {
  1: '一级红色',
  2: '二级橙色',
  3: '三级黄色'
}

const WARNING_MESSAGE_STATUS_LABELS = {
  pending: '待处理',
  processing: '处理中',
  closed: '已关闭',
  ignored: '已忽略'
}

const CHANNEL_LABELS = {
  site: '站内消息',
  sms: '短信',
  email: '邮件'
}

function optionText(labels, value, fallback = '-') {
  if (value === undefined || value === null || value === '') {
    return fallback
  }
  return labels[value] || value
}

function buildOptions(labels) {
  return Object.keys(labels).map((value) => ({
    value,
    label: labels[value]
  }))
}

export const BINARY_STATUS_OPTIONS = buildOptions(BINARY_STATUS_LABELS)
export const WARNING_LEVEL_COLOR_OPTIONS = buildOptions(WARNING_LEVEL_COLOR_LABELS)
export const WARNING_MESSAGE_STATUS_OPTIONS = buildOptions(WARNING_MESSAGE_STATUS_LABELS)

export function reportTypeText(value) {
  return optionText(REPORT_TYPE_LABELS, value)
}

export function integrationTypeText(value) {
  return optionText(INTEGRATION_TYPE_LABELS, value)
}

export function binaryStatusText(value) {
  return optionText(BINARY_STATUS_LABELS, value)
}

export function syncFrequencyText(value) {
  return optionText(SYNC_FREQUENCY_LABELS, value)
}

export function warningLevelText(value) {
  return optionText(WARNING_LEVEL_LABELS, value)
}

export function warningLevelColorText(value) {
  return optionText(WARNING_LEVEL_COLOR_LABELS, value)
}

export function warningMessageStatusText(value) {
  return optionText(WARNING_MESSAGE_STATUS_LABELS, value)
}

export function channelText(value) {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  const labels = String(value)
    .split(',')
    .map(item => item.trim())
    .filter(Boolean)
    .map(item => CHANNEL_LABELS[item] || item)
  return labels.length ? labels.join('、') : '-'
}

export function formatFriendlyDateTime(value) {
  return parseTime(value, '{y}-{m}-{d} {h}:{i}:{s}') || '-'
}
