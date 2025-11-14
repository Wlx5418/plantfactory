<template>
  <div id="app">
    <RouterView />
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  font-size: 16px;
  line-height: 1.5;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f5f7fa;
  color: #333;
}

#app {
  min-height: 100vh;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
  transition: background 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 通用工具类 */
.text-center {
  text-align: center;
}

.text-left {
  text-align: left;
}

.text-right {
  text-align: right;
}

.flex {
  display: flex;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flex-column {
  display: flex;
  flex-direction: column;
}

.hidden {
  display: none;
}

.visible {
  visibility: visible;
}

.invisible {
  visibility: hidden;
}

/* 动画类 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s ease;
}

.slide-enter-from {
  transform: translateX(-100%);
}

.slide-leave-to {
  transform: translateX(100%);
}

/* 响应式断点辅助类 */
@media (max-width: 768px) {
  .mobile-hidden {
    display: none;
  }
}

@media (min-width: 769px) {
  .desktop-hidden {
    display: none;
  }
}

/* 按钮通用样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  outline: none;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  pointer-events: none;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
}

.btn-secondary:hover:not(:disabled) {
  background: #f5f5f5;
  border-color: #667eea;
  color: #667eea;
}

.btn-success {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.btn-danger {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: white;
}

.btn-warning {
  background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
  color: white;
}

/* 表单通用样式 */
.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-input {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: white;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input.error {
  border-color: #ff4d4f;
}

.form-error {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 5px;
}

/* 卡片通用样式 */
.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
}

.card:hover {
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
}

.card-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-body {
  padding: 20px;
}

.card-footer {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

/* 消息提示样式 */
.alert {
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.alert-success {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #52c41a;
}

.alert-warning {
  background: #fffbe6;
  border: 1px solid #ffe58f;
  color: #fa8c16;
}

.alert-error {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  color: #ff4d4f;
}

.alert-info {
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  color: #1890ff;
}

/* 加载动画 */
.loading {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 工具提示 */
.tooltip {
  position: relative;
  cursor: help;
}

.tooltip::after {
  content: attr(data-tooltip);
  position: absolute;
  bottom: 125%;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s;
}

.tooltip:hover::after {
  opacity: 1;
}

/* 分隔线 */
.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 20px 0;
}

/* 徽章 */
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 8px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 10px;
  color: white;
  min-width: 18px;
  height: 18px;
}

.badge-primary {
  background: #667eea;
}

.badge-success {
  background: #52c41a;
}

.badge-warning {
  background: #fa8c16;
}

.badge-danger {
  background: #ff4d4f;
}

.badge-info {
  background: #1890ff;
}

/* 进度条 */
.progress {
  width: 100%;
  height: 8px;
  background: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  transition: width 0.3s ease;
}

/* 表格基础样式 */
.table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.table th,
.table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.table th {
  background: #fafafa;
  font-weight: 600;
  color: #333;
}

.table tbody tr:hover {
  background: #f9f9f9;
}

.table tbody tr:last-child td {
  border-bottom: none;
}

/* 标签 */
.tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 500;
}

.tag-blue {
  background: #e6f7ff;
  color: #1890ff;
}

.tag-green {
  background: #f6ffed;
  color: #52c41a;
}

.tag-orange {
  background: #fff7e6;
  color: #fa8c16;
}

.tag-red {
  background: #fff2f0;
  color: #ff4d4f;
}

/* 模态框遮罩 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  max-width: 90%;
  max-height: 90%;
  overflow: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

/* 面包屑导航 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.breadcrumb-item:not(:last-child)::after {
  content: '/';
  color: #999;
}

.breadcrumb-link {
  color: #667eea;
  text-decoration: none;
  transition: color 0.3s;
}

.breadcrumb-link:hover {
  color: #5a67d8;
}

.breadcrumb-item.active {
  color: #333;
  font-weight: 500;
}

/* 步骤条 */
.steps {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 40px 0;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
}

.step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 15px;
  left: 50%;
  width: 100%;
  height: 2px;
  background: #f0f0f0;
  z-index: 1;
}

.step.completed:not(:last-child)::after {
  background: #52c41a;
}

.step-indicator {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #f0f0f0;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  position: relative;
  z-index: 2;
}

.step.completed .step-indicator {
  background: #52c41a;
  color: white;
}

.step.active .step-indicator {
  background: #667eea;
  color: white;
}

.step-title {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
  text-align: center;
}

.step.active .step-title {
  color: #333;
  font-weight: 500;
}

.step.completed .step-title {
  color: #52c41a;
}
</style>