package com.qzsf.plantfactoryspring.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 环境数据WebSocket处理器
 * 处理实时环境数据的推送和命令接收
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
public class EnvironmentWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    // 存储活跃的WebSocket会话
    private static final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    public EnvironmentWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = getUsernameFromSession(session);
        log.info("WebSocket连接建立: sessionId={}, username={}", session.getId(), username);

        // 存储会话
        activeSessions.put(session.getId(), session);

        // 发送欢迎消息
        Map<String, Object> welcomeMessage = Map.of(
            "type", "welcome",
            "message", "连接成功",
            "timestamp", LocalDateTime.now().toString(),
            "sessionId", session.getId()
        );

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMessage)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String username = getUsernameFromSession(session);
        log.debug("收到WebSocket消息: sessionId={}, username={}, message={}",
            session.getId(), username, payload);

        try {
            // 解析消息
            @SuppressWarnings("unchecked")
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);

            String messageType = (String) messageData.get("type");

            switch (messageType) {
                case "subscribe":
                    handleSubscribe(session, messageData, username);
                    break;
                case "command":
                    handleCommand(session, messageData, username);
                    break;
                case "heartbeat":
                    handleHeartbeat(session, username);
                    break;
                default:
                    log.warn("未知消息类型: {}", messageType);
                    sendErrorMessage(session, "未知消息类型: " + messageType);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: sessionId={}, message={}", session.getId(), payload, e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = getUsernameFromSession(session);
        log.info("WebSocket连接关闭: sessionId={}, username={}, status={}",
            session.getId(), username, status);

        // 移除会话
        activeSessions.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String username = getUsernameFromSession(session);
        log.error("WebSocket传输错误: sessionId={}, username={}", session.getId(), username, exception);

        // 移除会话
        activeSessions.remove(session.getId());
    }

    /**
     * 处理订阅请求
     */
    private void handleSubscribe(WebSocketSession session, Map<String, Object> messageData, String username) {
        String areaId = (String) messageData.get("areaId");
        log.info("用户 {} 订阅区域 {} 的环境数据", username, areaId);

        Map<String, Object> response = Map.of(
            "type", "subscribe_response",
            "areaId", areaId,
            "status", "success",
            "message", "订阅成功",
            "timestamp", LocalDateTime.now().toString()
        );

        sendMessage(session, response);
    }

    /**
     * 处理设备控制命令
     */
    private void handleCommand(WebSocketSession session, Map<String, Object> messageData, String username) {
        String deviceId = (String) messageData.get("deviceId");
        String command = (String) messageData.get("command");

        log.info("用户 {} 发送设备控制命令: deviceId={}, command={}", username, deviceId, command);

        // TODO: 实现实际的设备控制逻辑
        // 这里先返回模拟的成功响应

        Map<String, Object> response = Map.of(
            "type", "command_response",
            "deviceId", deviceId,
            "command", command,
            "status", "success",
            "message", "命令执行成功",
            "timestamp", LocalDateTime.now().toString()
        );

        sendMessage(session, response);
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(WebSocketSession session, String username) {
        Map<String, Object> response = Map.of(
            "type", "heartbeat_response",
            "timestamp", LocalDateTime.now().toString()
        );

        sendMessage(session, response);
    }

    /**
     * 发送消息到指定会话
     */
    public void sendMessage(WebSocketSession session, Object message) {
        try {
            if (session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            }
        } catch (Exception e) {
            log.error("发送WebSocket消息失败: sessionId={}", session.getId(), e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        Map<String, Object> response = Map.of(
            "type", "error",
            "message", errorMessage,
            "timestamp", LocalDateTime.now().toString()
        );

        sendMessage(session, response);
    }

    /**
     * 广播环境数据更新
     */
    public void broadcastEnvironmentUpdate(Long areaId, Map<String, Object> environmentData) {
        Map<String, Object> message = Map.of(
            "type", "environment_update",
            "areaId", areaId,
            "data", environmentData,
            "timestamp", LocalDateTime.now().toString()
        );

        broadcast(message);
    }

    /**
     * 广播告警消息
     */
    public void broadcastAlert(String alertType, String message, Long areaId) {
        Map<String, Object> alertMessage = Map.of(
            "type", "alert",
            "alertType", alertType,
            "message", message,
            "areaId", areaId,
            "timestamp", LocalDateTime.now().toString()
        );

        broadcast(alertMessage);
    }

    /**
     * 广播消息到所有活跃会话
     */
    private void broadcast(Object message) {
        activeSessions.values().forEach(session -> sendMessage(session, message));
    }

    /**
     * 从会话中获取用户名
     */
    private String getUsernameFromSession(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        return (String) attributes.get("username");
    }

    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}