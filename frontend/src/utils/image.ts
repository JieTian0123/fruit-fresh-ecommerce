export const defaultImage =
  'data:image/svg+xml,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300" viewBox="0 0 300 300"><rect fill="#f0fdf4" width="300" height="300"/><text x="150" y="140" text-anchor="middle" font-size="64">🍊</text><text x="150" y="180" text-anchor="middle" fill="#9ca3af" font-size="14" font-family="sans-serif">暂无图片</text></svg>'
  )

export const defaultAvatar =
  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

export function normalizeImageUrl(value?: string | null, fallback = '') {
  if (!value) return fallback
  const url = String(value).trim()
  if (!url) return fallback
  if (url.startsWith('data:') || /^https?:\/\//.test(url) || url.startsWith('/api/')) {
    return url
  }
  if (url.startsWith('/uploads/')) {
    return `/api${url}`
  }
  return url
}

export function splitImageList(value?: string | null): string[] {
  if (!value) return []
  const trimmed = String(value).trim()
  if (!trimmed) return []
  try {
    const parsed = JSON.parse(trimmed)
    if (Array.isArray(parsed)) {
      return parsed.map(item => normalizeImageUrl(String(item))).filter(Boolean)
    }
  } catch {
    // Support comma separated image fields.
  }
  return trimmed.split(',').map(item => normalizeImageUrl(item)).filter(Boolean)
}

export function getProductImage(product?: {
  mainImage?: string | null
  images?: string | null
  subImages?: string | null
} | null, fallback = defaultImage) {
  if (!product) return fallback
  return (
    normalizeImageUrl(product.mainImage) ||
    splitImageList(product.images)[0] ||
    splitImageList(product.subImages)[0] ||
    fallback
  )
}
