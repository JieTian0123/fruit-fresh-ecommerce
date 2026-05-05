import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'

// Use vi.hoisted so variables are available when vi.mock factory runs (hoisted)
const { mockSetOption, mockResize, mockDispose, mockInit } = vi.hoisted(() => {
  const mockSetOption = vi.fn()
  const mockResize = vi.fn()
  const mockDispose = vi.fn()
  const mockInit = vi.fn(() => ({
    setOption: mockSetOption,
    resize: mockResize,
    dispose: mockDispose,
  }))
  return { mockSetOption, mockResize, mockDispose, mockInit }
})

vi.mock('echarts', () => ({
  default: { init: mockInit },
  init: mockInit,
}))

import BaseChart from '@/components/BaseChart.vue'

describe('BaseChart', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render chart container with default dimensions', () => {
    const wrapper = mount(BaseChart, {
      props: {
        options: { xAxis: {}, yAxis: {}, series: [] },
      },
    })
    const el = wrapper.element as HTMLElement
    expect(el.style.width).toBe('100%')
    expect(el.style.height).toBe('350px')
  })

  it('should accept custom width and height', () => {
    const wrapper = mount(BaseChart, {
      props: {
        options: { xAxis: {}, yAxis: {}, series: [] },
        width: '500px',
        height: '400px',
      },
    })
    const el = wrapper.element as HTMLElement
    expect(el.style.width).toBe('500px')
    expect(el.style.height).toBe('400px')
  })

  it('should initialize echarts on mount', () => {
    mount(BaseChart, {
      props: {
        options: { xAxis: {}, yAxis: {}, series: [] },
      },
    })
    expect(mockInit).toHaveBeenCalled()
    expect(mockSetOption).toHaveBeenCalledWith({ xAxis: {}, yAxis: {}, series: [] })
  })

  it('should update chart when options change', async () => {
    const wrapper = mount(BaseChart, {
      props: {
        options: { xAxis: {}, yAxis: {}, series: [] },
      },
    })

    const newOptions = { xAxis: { data: ['A', 'B'] }, yAxis: {}, series: [{ data: [1, 2] }] }
    await wrapper.setProps({ options: newOptions })
    await nextTick()

    // The watch should call setOption with new options
    expect(mockSetOption).toHaveBeenCalledWith(newOptions, true)
  })

  it('should dispose chart on unmount', () => {
    const wrapper = mount(BaseChart, {
      props: {
        options: { xAxis: {}, yAxis: {}, series: [] },
      },
    })
    wrapper.unmount()
    expect(mockDispose).toHaveBeenCalled()
  })

  it('should add resize listener on mount', () => {
    const addEventSpy = vi.spyOn(window, 'addEventListener')
    mount(BaseChart, {
      props: {
        options: { xAxis: {}, yAxis: {}, series: [] },
      },
    })
    expect(addEventSpy).toHaveBeenCalledWith('resize', expect.any(Function))
    addEventSpy.mockRestore()
  })

  it('should remove resize listener on unmount', () => {
    const removeEventSpy = vi.spyOn(window, 'removeEventListener')
    const wrapper = mount(BaseChart, {
      props: {
        options: { xAxis: {}, yAxis: {}, series: [] },
      },
    })
    wrapper.unmount()
    expect(removeEventSpy).toHaveBeenCalledWith('resize', expect.any(Function))
    removeEventSpy.mockRestore()
  })
})
