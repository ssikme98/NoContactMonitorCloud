const SYSTEM_JOB_NAME_LABELS = {
  '系统默认（无参）': '标准采集计划',
  '系统默认（有参）': '按条件采集计划',
  '系统默认（多参）': '批量采集计划'
}

export function formatJobName(jobName) {
  return SYSTEM_JOB_NAME_LABELS[jobName] || jobName || '-'
}
