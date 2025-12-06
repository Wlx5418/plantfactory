package com.qzsf.plantfactoryspring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket控制器
 * 处理环境数据实时推送
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 订阅特定区域的环境数据
     */
    @SubscribeMapping("/topic/environment/{areaId}")
    public Map<String, Object> subscribeToEnvironmentData(@DestinationVariable Long areaId, Principal principal) {
        log.info("用户 {} 订阅区域 {} 的环境数据", principal.getName(), areaId);

        // 返回当前区域最新数据作为初始数据
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("areaId", areaId);
        initialData.put("timestamp", LocalDateTime.now());
        initialData.put("temperature", 25.5);
        initialData.put("humidity", 65.2);
        initialData.put("lightIntensity", 800.0);
        initialData.put("co2Level", 450.0);
        initialData.put("phValue", 6.5);

        return initialData;
    }

    /**
     * 处理客户端消息
     */
    @MessageMapping("/environment/{areaId}/command")
    public void handleEnvironmentCommand(@DestinationVariable Long areaId,
                                       @Payload Map<String, Object> command,
                                       Principal principal) {
        log.info("用户 {} 向区域 {} 发送命令: {}", principal.getName(), areaId, command);

        // 这里可以处理设备控制命令
        // 例如：开关灯光、调节温度等

        // 返回命令执行结果
        Map<String, Object> response = new HashMap<>();
        response.put("areaId", areaId);
        response.put("command", command);
        response.put("status", "success");
        response.put("timestamp", LocalDateTime.now());
        response.put("executor", principal.getName());

        // 发送响应到特定用户
        messagingTemplate.convertAndSendToUser(principal.getName(),
            "/queue/environment/" + areaId + "/response", response);
    }

    /**
     * 推送环境数据更新（用于定时任务或事件触发）
     */
    public void pushEnvironmentUpdate(Long areaId, Map<String, Object> environmentData) {
        log.debug("推送区域 {} 环境数据更新: {}", areaId, environmentData);

        Map<String, Object> updateMessage = new HashMap<>();
        updateMessage.put("areaId", areaId);
        updateMessage.put("data", environmentData);
        updateMessage.put("timestamp", LocalDateTime.now());
        updateMessage.put("type", "environment_update");

        // 广播到订阅该区域的客户端
        messagingTemplate.convertAndSend("/topic/environment/" + areaId, updateMessage);
    }

    /**
     * 推送环境告警
     */
    public void pushEnvironmentAlert(Long areaId, String alertType, String message) {
        log.warn("推送区域 {} 环境告警: {} - {}", areaId, alertType, message);

        Map<String, Object> alertMessage = new HashMap<>();
        alertMessage.put("areaId", areaId);
        alertMessage.put("type", "environment_alert");
        alertMessage.put("alertType", alertType);
        alertMessage.put("message", message);
        alertMessage.put("timestamp", LocalDateTime.now());
        alertMessage.put("severity", "warning");

        // 广播告警消息
        messagingTemplate.convertAndSend("/topic/environment/" + areaId + "/alerts", alertMessage);

        // 同时广播到全局告警主题
        messagingTemplate.convertAndSend("/topic/alerts", alertMessage);
    }
}