import type { HomeActivity } from '@/types'

const STORAGE_KEY = 'fruit-home-activities-v3'

export const DEFAULT_HOME_ACTIVITIES: HomeActivity[] = [
  {
    code: 'DAILY_NEW',
    title: '新鲜到店',
    subtitle: '刚采摘入库生鲜',
    badge: '新鲜优选',
    actionText: '看新鲜',
    imageUrl: '/activity/fresh-new.svg',
    linkUrl: '/activity/fresh-new',
    theme: 'daily',
    sort: 1,
    status: 1
  },
  {
    code: 'NEW_TASTE',
    title: '临期特惠',
    subtitle: '保质期过半低价购',
    badge: '动态降价',
    actionText: '看特惠',
    imageUrl: '/activity/near-expiry.svg',
    linkUrl: '/activity/near-expiry',
    theme: 'deal',
    sort: 2,
    status: 1
  },
  {
    code: 'FULL_REDUCTION',
    title: '热销榜单',
    subtitle: '平台高销量生鲜',
    badge: '人气热卖',
    actionText: '看热销',
    imageUrl: '/activity/hot-sales.svg',
    linkUrl: '/activity/hot-sales',
    theme: 'hot',
    sort: 3,
    status: 1
  }
]

function normalizeActivity(activity: HomeActivity): HomeActivity {
  const fallback = DEFAULT_HOME_ACTIVITIES.find(item => item.code === activity.code)
  const merged = { ...fallback, ...activity } as HomeActivity
  if (fallback) {
    merged.title = fallback.title
    merged.subtitle = fallback.subtitle
    merged.badge = fallback.badge
    merged.actionText = fallback.actionText
    merged.theme = fallback.theme
  }
  if (activity.code === 'DAILY_NEW') {
    if (merged.linkUrl?.includes('/category') || merged.linkUrl?.includes('/daily-new')) {
      merged.linkUrl = fallback?.linkUrl || merged.linkUrl
    }
    if (!merged.imageUrl || merged.imageUrl.includes('daily-new')) {
      merged.imageUrl = fallback?.imageUrl || '/activity/fresh-new.svg'
    }
  }
  if (activity.code === 'NEW_TASTE') {
    if (merged.linkUrl?.includes('/category') || merged.linkUrl?.includes('/seasonal-fresh')) {
      merged.linkUrl = fallback?.linkUrl || merged.linkUrl
    }
    if (!merged.imageUrl || merged.imageUrl.includes('seasonal-fresh')) {
      merged.imageUrl = fallback?.imageUrl || '/activity/near-expiry.svg'
    }
  }
  if ((activity.code === 'DAILY_NEW' || activity.code === 'NEW_TASTE') && merged.linkUrl?.includes('/category')) {
    merged.linkUrl = fallback?.linkUrl || merged.linkUrl
  }
  if (activity.code === 'FULL_REDUCTION') {
    if (!merged.linkUrl || merged.linkUrl.includes('/coupons') || merged.linkUrl.includes('/fresh-deal')) {
      merged.linkUrl = fallback?.linkUrl || '/activity/hot-sales'
    }
    if (!merged.imageUrl || merged.imageUrl.includes('full-reduction') || merged.imageUrl.includes('fresh-deal')) {
      merged.imageUrl = fallback?.imageUrl || '/activity/hot-sales.svg'
    }
    merged.title = fallback?.title || '热销榜单'
    merged.subtitle = fallback?.subtitle
    merged.badge = fallback?.badge
    merged.actionText = fallback?.actionText
    merged.theme = fallback?.theme
  }
  return merged
}

export function mergeHomeActivities(activities?: HomeActivity[] | null): HomeActivity[] {
  const map = new Map(DEFAULT_HOME_ACTIVITIES.map(item => [item.code, { ...item }]))
  ;(activities || []).forEach(activity => {
    map.set(activity.code, normalizeActivity(activity))
  })
  return Array.from(map.values()).sort((a, b) => (a.sort || 0) - (b.sort || 0))
}

export function loadStoredHomeActivities(): HomeActivity[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return DEFAULT_HOME_ACTIVITIES
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? mergeHomeActivities(parsed) : DEFAULT_HOME_ACTIVITIES
  } catch {
    return DEFAULT_HOME_ACTIVITIES
  }
}

export function saveHomeActivities(activities: HomeActivity[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(mergeHomeActivities(activities)))
}

export function saveHomeActivity(code: string, patch: Partial<HomeActivity>) {
  const next = mergeHomeActivities(loadStoredHomeActivities().map(activity =>
    activity.code === code ? { ...activity, ...patch } : activity
  ))
  saveHomeActivities(next)
  return next
}
