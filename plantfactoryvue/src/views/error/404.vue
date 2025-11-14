<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-image">
        <img src="@/assets/404.svg" alt="404" />
      </div>
      <div class="error-info">
        <h1>404</h1>
        <h2>页面不存在</h2>
        <p>抱歉，您访问的页面不存在或已被删除</p>
        <a-space>
          <a-button type="primary" @click="goHome">
            返回首页
          </a-button>
          <a-button @click="goBack">
            返回上一页
          </a-button>
        </a-space>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped lang="less">
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.error-content {
  text-align: center;
  color: white;
  max-width: 600px;
  padding: 20px;

  .error-image {
    margin-bottom: 40px;

    img {
      width: 200px;
      height: 200px;
      object-fit: contain;
    }
  }

  .error-info {
    h1 {
      font-size: 72px;
      margin: 0;
      font-weight: bold;
      text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
    }

    h2 {
      font-size: 32px;
      margin: 16px 0;
      font-weight: 600;
    }

    p {
      font-size: 16px;
      margin: 20px 0 30px;
      opacity: 0.9;
    }
  }
}

@media (max-width: 768px) {
  .error-page {
    padding: 40px 20px;

    .error-content {
      .error-image img {
        width: 150px;
        height: 150px;
      }

      .error-info {
        h1 {
          font-size: 48px;
        }

        h2 {
          font-size: 24px;
        }

        p {
          font-size: 14px;
        }
      }
    }
  }
}
</style>