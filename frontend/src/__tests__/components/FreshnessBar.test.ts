import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import FreshnessBar from '@/components/FreshnessBar.vue'

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElProgress: { name: 'ElProgress', template: '<div class="el-progress" />' },
  ElTag: { name: 'ElTag', template: '<span class="el-tag"><slot /></span>' },
  ElIcon: { name: 'ElIcon', template: '<span><slot /></span>' },
}))

vi.mock('@element-plus/icons-vue', () => ({
  Timer: { name: 'Timer', template: '<i />' },
}))

function mountBar(props: Record<string, any> = {}) {
  return mount(FreshnessBar, {
    props,
    global: {
      stubs: {
        ElProgress: { template: '<div class="el-progress" />' },
        ElTag: { template: '<span class="el-tag"><slot /></span>' },
        ElIcon: { template: '<span><slot /></span>' },
        Timer: { template: '<i />' },
      },
    },
  })
}

describe('FreshnessBar', () => {
  it('should not render when no shelfLifeDays or productionDate', () => {
    const wrapper = mountBar({})
    expect(wrapper.find('.freshness-bar').exists()).toBe(false)
  })

  it('should not render when only shelfLifeDays is provided', () => {
    const wrapper = mountBar({ shelfLifeDays: 10 })
    expect(wrapper.find('.freshness-bar').exists()).toBe(false)
  })

  it('should not render when only productionDate is provided', () => {
    const wrapper = mountBar({ productionDate: '2024-01-01' })
    expect(wrapper.find('.freshness-bar').exists()).toBe(false)
  })

  it('should render when both shelfLifeDays and productionDate are provided', () => {
    const today = new Date()
    const wrapper = mountBar({
      shelfLifeDays: 10,
      productionDate: today.toISOString().split('T')[0],
    })
    expect(wrapper.find('.freshness-bar').exists()).toBe(true)
  })

  it('should show remaining days text for fresh product', () => {
    const today = new Date()
    const twoDaysAgo = new Date(today)
    twoDaysAgo.setDate(today.getDate() - 2)

    const wrapper = mountBar({
      shelfLifeDays: 10,
      productionDate: twoDaysAgo.toISOString().split('T')[0],
    })
    expect(wrapper.find('.freshness-days').text()).toContain('剩余8天')
  })

  it('should show "已过期" for expired product', () => {
    const longAgo = new Date()
    longAgo.setDate(longAgo.getDate() - 30)

    const wrapper = mountBar({
      shelfLifeDays: 5,
      productionDate: longAgo.toISOString().split('T')[0],
    })
    expect(wrapper.find('.freshness-days').text()).toContain('已过期')
  })

  it('should show "仅剩1天" for last-day product', () => {
    const yesterday = new Date()
    yesterday.setDate(yesterday.getDate() - 9)

    const wrapper = mountBar({
      shelfLifeDays: 10,
      productionDate: yesterday.toISOString().split('T')[0],
    })
    expect(wrapper.find('.freshness-days').text()).toContain('仅剩1天')
  })

  it('should display green color for high freshness (>70%)', () => {
    const today = new Date()
    const wrapper = mountBar({
      shelfLifeDays: 10,
      productionDate: today.toISOString().split('T')[0],
    })
    const style = wrapper.find('.freshness-days').attributes('style')
    expect(style).toContain('#10b981')
  })

  it('should display yellow color for medium freshness (30-70%)', () => {
    const today = new Date()
    const fiveDaysAgo = new Date(today)
    fiveDaysAgo.setDate(today.getDate() - 5)

    const wrapper = mountBar({
      shelfLifeDays: 10,
      productionDate: fiveDaysAgo.toISOString().split('T')[0],
    })
    const style = wrapper.find('.freshness-days').attributes('style')
    expect(style).toContain('#f59e0b')
  })

  it('should display red color for low freshness (<30%)', () => {
    const today = new Date()
    const eightDaysAgo = new Date(today)
    eightDaysAgo.setDate(today.getDate() - 8)

    const wrapper = mountBar({
      shelfLifeDays: 10,
      productionDate: eightDaysAgo.toISOString().split('T')[0],
    })
    const style = wrapper.find('.freshness-days').attributes('style')
    expect(style).toContain('#ef4444')
  })

  it('should not show meta by default', () => {
    const today = new Date()
    const wrapper = mountBar({
      shelfLifeDays: 10,
      productionDate: today.toISOString().split('T')[0],
    })
    expect(wrapper.find('.freshness-meta').exists()).toBe(false)
  })

  it('should show meta when showMeta is true and qualityGrade provided', () => {
    const today = new Date()
    const wrapper = mountBar({
      shelfLifeDays: 10,
      productionDate: today.toISOString().split('T')[0],
      showMeta: true,
      qualityGrade: 'A',
    })
    expect(wrapper.find('.freshness-meta').exists()).toBe(true)
    expect(wrapper.text()).toContain('A')
  })

  it('should show storage condition in meta', () => {
    const today = new Date()
    const wrapper = mountBar({
      shelfLifeDays: 10,
      productionDate: today.toISOString().split('T')[0],
      showMeta: true,
      storageCondition: '冷藏0-4℃',
    })
    expect(wrapper.text()).toContain('冷藏0-4℃')
  })
})
