package com.qzsf.plantfactoryspring.controller;

import com.qzsf.plantfactoryspring.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备控制控制器
 * 实现设备控制接口，解决"只能看不能控"的问题
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "设备控制", description = "设备控制相关接口")
public class DeviceControlController {

    // 模拟设备状态存储
    private static final Map<String, Object> deviceStatus = new ConcurrentHashMap<>();

    /**
     * 控制设备
     */
    @PostMapping("/{deviceId}/control")
    @Operation(summary = "控制设备", description = "发送控制命令到指定设备")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> controlDevice(
            @Parameter(description = "设备ID", example = "DEV001")
            @PathVariable String deviceId,

            @Parameter(description = "控制命令")
            @RequestBody Map<String, Object> command) {

        try {
            String commandType = (String) command.get("command");
            log.info("收到设备控制命令: deviceId={}, command={}, parameters={}",
                deviceId, commandType, command);

            // 异步执行设备控制
            CompletableFuture.supplyAsync(() -> executeDeviceControl(deviceId, command))
                .thenAccept(result -> {
                    log.info("设备控制完成: deviceId={}, result={}", deviceId, result);
                })
                .exceptionally(throwable -> {
                    log.error("设备控制失败: deviceId={}, command={}", deviceId, command, throwable);
                    return null;
                });

            // 立即返回响应（异步处理）
            Map<String, Object> response = Map.of(
                "deviceId", deviceId,
                "command", commandType,
                "status", "accepted",
                "message", "设备控制命令已接受，正在执行",
                "timestamp", LocalDateTime.now().toString()
            );

            return ResponseEntity.ok(ApiResponse.success(response, "设备控制命令已发送"));

        } catch (Exception e) {
            log.error("设备控制命令处理失败: deviceId={}, command={}", deviceId, command, e);
            return ResponseEntity.ok(ApiResponse.error("设备控制失败: " + e.getMessage()));
        }
    }

    /**
     * 获取设备状态
     */
    @GetMapping("/{deviceId}/status")
    @Operation(summary = "获取设备状态", description = "获取指定设备的当前状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'OBSERVER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDeviceStatus(
            @Parameter(description = "设备ID", example = "DEV001")
            @PathVariable String deviceId) {

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> status = (Map<String, Object>) deviceStatus.getOrDefault(deviceId, createDefaultDeviceStatus(deviceId));

            Map<String, Object> response = Map.of(
                "deviceId", deviceId,
                "status", status,
                "timestamp", LocalDateTime.now().toString()
            );

            return ResponseEntity.ok(ApiResponse.success(response, "获取设备状态成功"));

        } catch (Exception e) {
            log.error("获取设备状态失败: deviceId={}", deviceId, e);
            return ResponseEntity.ok(ApiResponse.error("获取设备状态失败: " + e.getMessage()));
        }
    }

    /**
     * 批量控制设备
     */
    @PostMapping("/batch-control")
    @Operation(summary = "批量控制设备", description = "同时控制多个设备")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchControlDevices(
            @Parameter(description = "批量控制命令")
            @RequestBody Map<String, Object> batchCommand) {

        try {
            @SuppressWarnings("unchecked")
            java.util.List<String> deviceIds = (java.util.List<String>) batchCommand.get("deviceIds");
            String command = (String) batchCommand.get("command");

            if (deviceIds == null || deviceIds.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("设备ID列表不能为空"));
            }

            log.info("收到批量设备控制命令: deviceIds={}, command={}", deviceIds, command);

            // 异步批量执行
            CompletableFuture.runAsync(() -> {
                for (String deviceId : deviceIds) {
                    try {
                        Map<String, Object> singleCommand = Map.of(
                            "command", command,
                            "batchId", batchCommand.get("batchId")
                        );
                        executeDeviceControl(deviceId, singleCommand);
                        Thread.sleep(100); // 避免同时发送太多命令
                    } catch (Exception e) {
                        log.error("批量控制设备失败: deviceId={}", deviceId, e);
                    }
                }
            });

            Map<String, Object> response = Map.of(
                "deviceIds", deviceIds,
                "command", command,
                "batchId", batchCommand.get("batchId"),
                "status", "accepted",
                "message", String.format("批量控制命令已发送到 %d 个设备", deviceIds.size()),
                "timestamp", LocalDateTime.now().toString()
            );

            return ResponseEntity.ok(ApiResponse.success(response, "批量控制命令已发送"));

        } catch (Exception e) {
            log.error("批量设备控制失败: command={}", batchCommand, e);
            return ResponseEntity.ok(ApiResponse.error("批量设备控制失败: " + e.getMessage()));
        }
    }

    /**
     * 执行设备控制（模拟实现）
     */
    private Map<String, Object> executeDeviceControl(String deviceId, Map<String, Object> command) {
        try {
            String commandType = (String) command.get("command");
            @SuppressWarnings("unchecked")
            Map<String, Object> currentStatus = (Map<String, Object>) deviceStatus.computeIfAbsent(deviceId, this::createDefaultDeviceStatus);

            Map<String, Object> result = Map.of(
                "deviceId", deviceId,
                "command", commandType,
                "status", "success",
                "message", "命令执行成功",
                "executionTime", LocalDateTime.now().toString()
            );

            // 模拟不同类型的设备控制
            switch (commandType) {
                case "TURN_ON_LIGHT":
                    currentStatus.put("lightStatus", "ON");
                    currentStatus.put("lightIntensity", command.getOrDefault("intensity", 80));
                    break;

                case "TURN_OFF_LIGHT":
                    currentStatus.put("lightStatus", "OFF");
                    currentStatus.put("lightIntensity", 0);
                    break;

                case "ADJUST_LIGHT_INTENSITY":
                    currentStatus.put("lightIntensity", command.get("intensity"));
                    break;

                case "TURN_ON_FAN":
                    currentStatus.put("fanStatus", "ON");
                    currentStatus.put("fanSpeed", command.getOrDefault("speed", "medium"));
                    break;

                case "TURN_OFF_FAN":
                    currentStatus.put("fanStatus", "OFF");
                    currentStatus.put("fanSpeed", "off");
                    break;

                case "TURN_ON_PUMP":
                    currentStatus.put("pumpStatus", "ON");
                    break;

                case "TURN_OFF_PUMP":
                    currentStatus.put("pumpStatus", "OFF");
                    break;

                case "SET_TEMPERATURE":
                    currentStatus.put("targetTemperature", command.get("temperature"));
                    break;

                case "SET_HUMIDITY":
                    currentStatus.put("targetHumidity", command.get("humidity"));
                    break;

                default:
                    log.warn("未知设备控制命令: {}", commandType);
                    result = Map.of(
                        "deviceId", deviceId,
                        "command", commandType,
                        "status", "error",
                        "message", "未知命令类型",
                        "executionTime", LocalDateTime.now().toString()
                    );
            }

            // 更新设备状态
            currentStatus.put("lastUpdate", LocalDateTime.now().toString());
            deviceStatus.put(deviceId, currentStatus);

            return result;

        } catch (Exception e) {
            log.error("设备控制执行失败: deviceId={}, command={}", deviceId, command, e);
            return Map.of(
                "deviceId", deviceId,
                "status", "error",
                "message", "设备控制执行失败: " + e.getMessage(),
                "executionTime", LocalDateTime.now().toString()
            );
        }
    }

    /**
     * 创建默认设备状态
     */
    private Map<String, Object> createDefaultDeviceStatus(String deviceId) {
        return Map.of(
            "deviceId", deviceId,
            "online", true,
            "lightStatus", "OFF",
            "lightIntensity", 0,
            "fanStatus", "OFF",
            "fanSpeed", "off",
            "pumpStatus", "OFF",
            "targetTemperature", 25.0,
            "targetHumidity", 65.0,
            "lastUpdate", LocalDateTime.now().toString()
        );
    }

    /**
     * 获取所有设备状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取所有设备状态", description = "获取系统中所有设备的状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'OBSERVER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllDeviceStatus() {
        try {
            Map<String, Object> response = Map.of(
                "devices", deviceStatus,
                "count", deviceStatus.size(),
                "timestamp", LocalDateTime.now().toString()
            );

            return ResponseEntity.ok(ApiResponse.success(response, "获取所有设备状态成功"));

        } catch (Exception e) {
            log.error("获取所有设备状态失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取设备状态失败: " + e.getMessage()));
        }
    }

    /**
     * 重置设备状态（用于测试）
     */
    @PostMapping("/{deviceId}/reset")
    @Operation(summary = "重置设备状态", description = "重置指定设备的状态（仅用于测试）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> resetDeviceStatus(
            @Parameter(description = "设备ID", example = "DEV001")
            @PathVariable String deviceId) {

        try {
            deviceStatus.remove(deviceId);
            log.info("设备状态已重置: deviceId={}", deviceId);

            return ResponseEntity.ok(ApiResponse.success(null, "设备状态重置成功"));

        } catch (Exception e) {
            log.error("重置设备状态失败: deviceId={}", deviceId, e);
            return ResponseEntity.ok(ApiResponse.error("重置设备状态失败: " + e.getMessage()));
        }
    }
}