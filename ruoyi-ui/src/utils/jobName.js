const SYSTEM_JOB_NAME_LABELS = {
  '系统默认（无参）': '系统默认（标准采集）',
  '系统默认（有参）': '系统默认（条件采集）',
  '系统默认（多参）': '系统默认（批量采集）'
}

export function formatJobName(jobName) {
  return SYSTEM_JOB_NAME_LABELS[jobName] || jobName || '-'
}
