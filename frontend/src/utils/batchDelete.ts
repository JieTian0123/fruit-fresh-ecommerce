import { ElMessage, ElMessageBox } from 'element-plus'

type DeleteableItem = {
  id: number
}

type BatchDeleteOptions<T extends DeleteableItem> = {
  items: T[]
  label: string
  deleteOne: (id: number) => Promise<unknown>
  afterDelete?: () => Promise<void> | void
}

export async function batchDeleteSelected<T extends DeleteableItem>({
  items,
  label,
  deleteOne,
  afterDelete
}: BatchDeleteOptions<T>) {
  if (items.length === 0) {
    ElMessage.warning('请先选择要删除的数据')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${items.length} 条${label}吗？此操作不可恢复。`,
      '批量删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }

  let successCount = 0
  for (const item of items) {
    try {
      await deleteOne(item.id)
      successCount += 1
    } catch {
      // 继续删除后续项，最后统一提示结果。
    }
  }

  if (successCount === items.length) {
    ElMessage.success('删除成功')
  } else if (successCount > 0) {
    ElMessage.warning(`已删除 ${successCount} 条，${items.length - successCount} 条删除失败`)
  } else {
    ElMessage.error('删除失败')
  }

  await afterDelete?.()
}
