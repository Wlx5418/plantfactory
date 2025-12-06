package com.qzsf.plantfactoryspring.controller;

import com.qzsf.plantfactoryspring.utils.ApiResponse;
import com.qzsf.plantfactoryspring.service.PartitionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 数据库分区管理控制器
 * 提供分区管理的REST API接口
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/database/partitions")
@RequiredArgsConstructor
@Tag(name = "数据库分区管理", description = "数据库分区管理相关接口")
@PreAuthorize("hasRole('ADMIN')")
public class PartitionManagementController {

    private final PartitionManagementService partitionManagementService;

    /**
     * 获取分区监控信息
     */
    @GetMapping("/monitor")
    @Operation(summary = "获取分区监控信息", description = "获取所有表的分区状态和统计信息")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPartitionMonitoringInfo() {
        try {
            List<Map<String, Object>> monitoringInfo = partitionManagementService.getPartitionMonitoringInfo();
            return ResponseEntity.ok(ApiResponse.success(monitoringInfo, "获取分区监控信息成功"));
        } catch (Exception e) {
            log.error("获取分区监控信息失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取分区监控信息失败: " + e.getMessage()));
        }
    }

    /**
     * 检查并创建分区
     */
    @PostMapping("/check-and-create")
    @Operation(summary = "检查并创建分区", description = "检查并创建缺失的分区")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> checkAndCreatePartitions() {
        try {
            partitionManagementService.checkAndCreatePartitions();
            return ResponseEntity.ok(ApiResponse.success(null, "分区检查和创建任务已启动"));
        } catch (Exception e) {
            log.error("检查和创建分区失败", e);
            return ResponseEntity.ok(ApiResponse.error("检查和创建分区失败: " + e.getMessage()));
        }
    }

    /**
     * 清理旧分区
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理旧分区", description = "清理超过保留期的旧分区")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> cleanupOldPartitions() {
        try {
            partitionManagementService.cleanupOldPartitions();
            return ResponseEntity.ok(ApiResponse.success(null, "旧分区清理任务已启动"));
        } catch (Exception e) {
            log.error("清理旧分区失败", e);
            return ResponseEntity.ok(ApiResponse.error("清理旧分区失败: " + e.getMessage()));
        }
    }

    /**
     * 手动创建指定月份的分区
     */
    @PostMapping("/create")
    @Operation(summary = "手动创建分区", description = "为指定表创建指定月份的分区")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> createPartitionForMonth(
            @Parameter(description = "表名", example = "environment_data")
            @RequestParam String tableName,

            @Parameter(description = "年份", example = "2025")
            @RequestParam int year,

            @Parameter(description = "月份", example = "12")
            @RequestParam int month) {

        try {
            partitionManagementService.createPartitionForMonth(tableName, year, month);
            String partitionName = "p_" + year + String.format("%02d", month);
            return ResponseEntity.ok(ApiResponse.success(null,
                String.format("分区 %s.%s 创建成功", tableName, partitionName)));
        } catch (Exception e) {
            log.error("创建分区失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建分区失败: " + e.getMessage()));
        }
    }

    /**
     * 手动清理指定月份前的分区
     */
    @PostMapping("/cleanup-before")
    @Operation(summary = "手动清理旧分区", description = "清理指定月份之前的旧分区")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> cleanupPartitionsOlderThan(
            @Parameter(description = "表名", example = "environment_data")
            @RequestParam String tableName,

            @Parameter(description = "保留月数", example = "6")
            @RequestParam int months) {

        try {
            partitionManagementService.cleanupPartitionsOlderThan(tableName, months);
            return ResponseEntity.ok(ApiResponse.success(null,
                String.format("表 %s 的 %d 个月前分区清理任务已启动", tableName, months)));
        } catch (Exception e) {
            log.error("清理旧分区失败", e);
            return ResponseEntity.ok(ApiResponse.error("清理旧分区失败: " + e.getMessage()));
        }
    }

    /**
     * 启用事件调度器
     */
    @PostMapping("/enable-scheduler")
    @Operation(summary = "启用事件调度器", description = "启用MySQL事件调度器")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> enableEventScheduler() {
        try {
            partitionManagementService.enableEventScheduler();
            return ResponseEntity.ok(ApiResponse.success(null, "事件调度器已启用"));
        } catch (Exception e) {
            log.error("启用事件调度器失败", e);
            return ResponseEntity.ok(ApiResponse.error("启用事件调度器失败: " + e.getMessage()));
        }
    }

    /**
     * 获取分区创建建议
     */
    @GetMapping("/suggestions")
    @Operation(summary = "获取分区创建建议", description = "基于当前时间获取需要创建的分区建议")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPartitionSuggestions() {
        try {
            LocalDate currentDate = LocalDate.now();
            List<Map<String, Object>> suggestions = List.of(
                Map.of(
                    "tableName", "environment_data",
                    "suggestedPartitions", generatePartitionSuggestions(currentDate),
                    "description", "环境数据表建议创建的分区"
                ),
                Map.of(
                    "tableName", "operation_logs",
                    "suggestedPartitions", generatePartitionSuggestions(currentDate),
                    "description", "操作日志表建议创建的分区"
                )
            );

            return ResponseEntity.ok(ApiResponse.success(suggestions, "获取分区建议成功"));
        } catch (Exception e) {
            log.error("获取分区建议失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取分区建议失败: " + e.getMessage()));
        }
    }

    /**
     * 生成分区建议列表
     */
    private List<Map<String, Object>> generatePartitionSuggestions(LocalDate currentDate) {
        return List.of(
            Map.of(
                "month", currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                "partitionName", "p_" + currentDate.format(DateTimeFormatter.ofPattern("yyyyMM")),
                "priority", "high",
                "reason", "当前月份"
            ),
            Map.of(
                "month", currentDate.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")),
                "partitionName", "p_" + currentDate.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM")),
                "priority", "high",
                "reason", "下个月"
            ),
            Map.of(
                "month", currentDate.plusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM")),
                "partitionName", "p_" + currentDate.plusMonths(2).format(DateTimeFormatter.ofPattern("yyyyMM")),
                "priority", "medium",
                "reason", "两个月后"
            )
        );
    }
}