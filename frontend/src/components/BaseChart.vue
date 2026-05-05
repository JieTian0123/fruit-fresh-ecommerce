<template>
  <div ref="chartRef" :style="{ height, width }"></div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import * as echarts from 'echarts'

const props = withDefaults(defineProps<{
  options: echarts.EChartsOption
  width?: string
  height?: string
}>(), {
  width: '100%',
  height: '350px'
})

const chartRef = ref<HTMLDivElement>()
const chartInstance = shallowRef<echarts.ECharts>()

// 注意使用 shallowRef 避免 Vue 3 深度代理破坏 ECharts 内部状态

onMounted(() => {
  if (chartRef.value) {
    chartInstance.value = echarts.init(chartRef.value)
    chartInstance.value.setOption(props.options)
    window.addEventListener('resize', handleResize)
  }
})

function handleResize() {
  chartInstance.value?.resize()
}

watch(() => props.options, (newOpt) => {
  chartInstance.value?.setOption(newOpt, true)
}, { deep: true })

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance.value?.dispose()
})
</script>
