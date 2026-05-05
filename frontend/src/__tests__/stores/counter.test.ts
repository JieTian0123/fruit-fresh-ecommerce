import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useCounterStore } from '@/stores/counter'

describe('Counter Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should initialize with count 0', () => {
    const store = useCounterStore()
    expect(store.count).toBe(0)
  })

  it('should compute doubleCount correctly', () => {
    const store = useCounterStore()
    expect(store.doubleCount).toBe(0)
    store.count = 5
    expect(store.doubleCount).toBe(10)
  })

  it('should increment count', () => {
    const store = useCounterStore()
    store.increment()
    expect(store.count).toBe(1)
    store.increment()
    expect(store.count).toBe(2)
  })

  it('should maintain doubleCount after increment', () => {
    const store = useCounterStore()
    store.increment()
    store.increment()
    store.increment()
    expect(store.count).toBe(3)
    expect(store.doubleCount).toBe(6)
  })
})
