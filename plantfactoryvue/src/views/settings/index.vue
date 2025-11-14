<template>
  <div class="settings">
    <a-row :gutter="24">
      <a-col :span="24">
        <a-card title="系统设置" :bordered="false">
          <div class="placeholder-content">
            <h3>系统设置功能开发中...</h3>
            <p>这里将提供系统配置、权限管理、数据备份等功能</p>
            <a-space>
              <a-button type="primary">基础设置</a-button>
              <a-button>用户权限</a-button>
              <a-button>数据管理</a-button>
            </a-space>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
// 系统设置页面占位组件
</script>

<style scoped lang="less">
.placeholder-content {
  text-align: center;
  padding: 60px 20px;
  color: #666;

  h3 {
    margin-bottom: 16px;
    color: #1890ff;
  }

  p {
    margin-bottom: 24px;
  }
}
</style>