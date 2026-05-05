export function formatDateTime(value?: string | Date | null) {
  if (!value) return '-'
  if (value instanceof Date) {
    return toDateTimeText(value)
  }

  const text = String(value).trim()
  if (!text) return '-'
  const normalized = text.replace('T', ' ')
  if (/^\d{4}-\d{2}-\d{2}$/.test(normalized)) {
    return normalized
  }
  return normalized.slice(0, 19)
}

function toDateTimeText(date: Date) {
  const pad = (num: number) => String(num).padStart(2, '0')
  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  const hour = pad(date.getHours())
  const minute = pad(date.getMinutes())
  const second = pad(date.getSeconds())
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}
