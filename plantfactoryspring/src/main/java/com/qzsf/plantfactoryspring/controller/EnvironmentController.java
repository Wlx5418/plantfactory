package com.qzsf.plantfactoryspring.controller;

import com.qzsf.plantfactoryspring.entity.EnvironmentData;
import com.qzsf.plantfactoryspring.service.EnvironmentService;
import com.qzsf.plantfactoryspring.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 环境数据管理控制器
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/environment")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentService environmentService;

    /**
     * 保存环境数据
     */
    @PostMapping("/data")
    // @PreAuthorize("hasAnyAuthority('env:data:create', 'admin')") // 临时禁用认证用于测试
    public ResponseEntity<ApiResponse<EnvironmentData>> saveEnvironmentData(
            @Valid @RequestBody EnvironmentData environmentData) {
        try {
            EnvironmentData saved = environmentService.saveEnvironmentData(environmentData);
            return ResponseEntity.ok(ApiResponse.success(saved, "环境数据保存成功"));
        } catch (Exception e) {
            log.error("保存环境数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("保存失败: " + e.getMessage()));
        }
    }

    /**
     * 批量保存环境数据
     */
    @PostMapping("/data/batch")
    @PreAuthorize("hasAnyAuthority('env:data:create', 'admin')")
    public ResponseEntity<ApiResponse<List<EnvironmentData>>> saveEnvironmentDataBatch(
            @Valid @RequestBody List<EnvironmentData> environmentDataList) {
        try {
            List<EnvironmentData> saved = environmentService.saveEnvironmentDataBatch(environmentDataList);
            return ResponseEntity.ok(ApiResponse.success(saved, "批量保存环境数据成功"));
        } catch (Exception e) {
            log.error("批量保存环境数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("批量保存失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID查询环境数据
     */
    @GetMapping("/data/{id}")
    @PreAuthorize("hasAnyAuthority('env:data:read', 'admin')")
    public ResponseEntity<ApiResponse<EnvironmentData>> getEnvironmentData(
            @PathVariable Long id) {
        try {
            Optional<EnvironmentData> data = environmentService.findById(id);
            if (data.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(data.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.error("环境数据不存在"));
            }
        } catch (Exception e) {
            log.error("查询环境数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 根据传感器ID查询最新数据
     */
    @GetMapping("/data/latest/sensor/{sensorId}")
    @PreAuthorize("hasAnyAuthority('env:data:read', 'admin')")
    public ResponseEntity<ApiResponse<EnvironmentData>> getLatestBySensorId(
            @PathVariable String sensorId) {
        try {
            Optional<EnvironmentData> data = environmentService.findLatestBySensorId(sensorId);
            if (data.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(data.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.error("该传感器暂无数据"));
            }
        } catch (Exception e) {
            log.error("查询传感器最新数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 根据传感器ID和数据类型查询最新数据
     */
    @GetMapping("/data/latest/sensor/{sensorId}/type/{dataType}")
    @PreAuthorize("hasAnyAuthority('env:data:read', 'admin')")
    public ResponseEntity<ApiResponse<EnvironmentData>> getLatestBySensorIdAndDataType(
            @PathVariable String sensorId, @PathVariable EnvironmentData.DataType dataType) {
        try {
            Optional<EnvironmentData> data = environmentService.findLatestBySensorIdAndDataType(sensorId, dataType);
            if (data.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(data.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.error("该传感器暂无此类数据"));
            }
        } catch (Exception e) {
            log.error("查询传感器最新数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 查询指定时间范围的数据
     */
    @GetMapping("/data/range")
    // @PreAuthorize("hasAnyAuthority('env:data:read', 'admin')") // 临时禁用认证用于测试
    public ResponseEntity<ApiResponse<Page<EnvironmentData>>> getDataByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "collectedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<EnvironmentData> data = environmentService.findByTimeRange(
                    startTime, endTime, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error("查询时间范围数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 查询指定传感器的数据
     */
    @GetMapping("/data/sensor/{sensorId}/range")
    @PreAuthorize("hasAnyAuthority('env:data:read', 'admin')")
    public ResponseEntity<ApiResponse<Page<EnvironmentData>>> getDataBySensorId(
            @PathVariable String sensorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "collectedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<EnvironmentData> data = environmentService.findBySensorId(
                    sensorId, startTime, endTime, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error("查询传感器数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 查询指定数据类型的数据
     */
    @GetMapping("/data/type/{dataType}/range")
    @PreAuthorize("hasAnyAuthority('env:data:read', 'admin')")
    public ResponseEntity<ApiResponse<Page<EnvironmentData>>> getDataByDataType(
            @PathVariable EnvironmentData.DataType dataType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "collectedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<EnvironmentData> data = environmentService.findByDataType(
                    dataType, startTime, endTime, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error("查询数据类型失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 查询异常数据
     */
    @GetMapping("/data/abnormal")
    @PreAuthorize("hasAnyAuthority('env:data:read', 'admin')")
    public ResponseEntity<ApiResponse<Page<EnvironmentData>>> getAbnormalData(
            @RequestParam EnvironmentData.DataStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<EnvironmentData> data = environmentService.findAbnormalData(
                    status, startTime, endTime, page, size);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error("查询异常数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 查询超出阈值的数据
     */
    @GetMapping("/data/out-of-range")
    @PreAuthorize("hasAnyAuthority('env:data:read', 'admin')")
    public ResponseEntity<ApiResponse<Page<EnvironmentData>>> getOutOfRangeData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<EnvironmentData> data = environmentService.findOutOfRangeData(
                    startTime, endTime, page, size);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error("查询超阈值数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 获取数据统计信息
     */
    @GetMapping("/statistics/{dataType}")
    @PreAuthorize("hasAnyAuthority('env:statistics:read', 'admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDataStatistics(
            @PathVariable EnvironmentData.DataType dataType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            Map<String, Object> statistics = environmentService.getDataStatistics(
                    dataType, startTime, endTime);
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            log.error("获取数据统计失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取统计失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有数据类型的统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('env:statistics:read', 'admin')")
    public ResponseEntity<ApiResponse<Map<EnvironmentData.DataType, Map<String, Object>>>> getAllStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            Map<EnvironmentData.DataType, Map<String, Object>> statistics = environmentService.getAllDataTypeStatistics(
                    startTime, endTime);
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            log.error("获取全部统计失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取统计失败: " + e.getMessage()));
        }
    }

    /**
     * 获取图表数据
     */
    @GetMapping("/chart/{dataType}")
    @PreAuthorize("hasAnyAuthority('env:chart:read', 'admin')")
    public ResponseEntity<ApiResponse<List<EnvironmentData>>> getChartData(
            @PathVariable EnvironmentData.DataType dataType,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            List<EnvironmentData> data = environmentService.getChartData(dataType, hours);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error("获取图表数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取图表数据失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有活跃传感器ID
     */
    @GetMapping("/sensors")
    // @PreAuthorize("hasAnyAuthority('env:sensors:read', 'admin')") // 临时禁用认证用于测试
    public ResponseEntity<ApiResponse<List<String>>> getAllActiveSensorIds() {
        try {
            List<String> sensorIds = environmentService.getAllActiveSensorIds();
            return ResponseEntity.ok(ApiResponse.success(sensorIds));
        } catch (Exception e) {
            log.error("获取传感器列表失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取传感器列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取传感器最新数据
     */
    @PostMapping("/sensors/latest")
    @PreAuthorize("hasAnyAuthority('env:sensors:read', 'admin')")
    public ResponseEntity<ApiResponse<Map<String, EnvironmentData>>> getLatestDataBySensorIds(
            @RequestBody List<String> sensorIds) {
        try {
            Map<String, EnvironmentData> latestData = environmentService.getLatestDataBySensorIds(sensorIds);
            return ResponseEntity.ok(ApiResponse.success(latestData));
        } catch (Exception e) {
            log.error("获取传感器最新数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取最新数据失败: " + e.getMessage()));
        }
    }

    /**
     * 更新数据阈值
     */
    @PutMapping("/data/{id}/thresholds")
    @PreAuthorize("hasAnyAuthority('env:threshold:update', 'admin')")
    public ResponseEntity<ApiResponse<EnvironmentData>> updateThresholds(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal minThreshold,
            @RequestParam(required = false) BigDecimal maxThreshold) {
        try {
            EnvironmentData updated = environmentService.updateThresholds(id, minThreshold, maxThreshold);
            return ResponseEntity.ok(ApiResponse.success(updated, "阈值更新成功"));
        } catch (Exception e) {
            log.error("更新阈值失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新阈值失败: " + e.getMessage()));
        }
    }

    /**
     * 删除环境数据
     */
    @DeleteMapping("/data/{id}")
    @PreAuthorize("hasAnyAuthority('env:data:delete', 'admin')")
    public ResponseEntity<ApiResponse<String>> deleteEnvironmentData(@PathVariable Long id) {
        try {
            environmentService.deleteEnvironmentData(id);
            return ResponseEntity.ok(ApiResponse.success("环境数据删除成功"));
        } catch (Exception e) {
            log.error("删除环境数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除失败: " + e.getMessage()));
        }
    }

    /**
     * 搜索环境数据
     */
    @GetMapping("/data/search")
    @PreAuthorize("hasAnyAuthority('env:data:read', 'admin')")
    public ResponseEntity<ApiResponse<Page<EnvironmentData>>> searchEnvironmentData(
            @RequestParam(required = false) String sensorId,
            @RequestParam(required = false) EnvironmentData.DataType dataType,
            @RequestParam(required = false) EnvironmentData.DataStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<EnvironmentData> data = environmentService.searchEnvironmentData(
                    sensorId, dataType, status, startTime, endTime, page, size);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error("搜索环境数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 创建模拟数据
     */
    @PostMapping("/data/mock")
    // @PreAuthorize("hasAnyAuthority('env:data:create', 'admin')") // 临时禁用认证用于测试
    public ResponseEntity<ApiResponse<List<EnvironmentData>>> createMockData() {
        try {
            List<EnvironmentData> mockData = environmentService.createMockData();
            return ResponseEntity.ok(ApiResponse.success(mockData, "模拟数据创建成功"));
        } catch (Exception e) {
            log.error("创建模拟数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建模拟数据失败: " + e.getMessage()));
        }
    }

    /**
     * 清理旧数据
     */
    @DeleteMapping("/data/cleanup")
    @PreAuthorize("hasAnyAuthority('env:data:delete', 'admin')")
    public ResponseEntity<ApiResponse<String>> cleanupOldData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeTime) {
        try {
            int deletedCount = environmentService.deleteOldData(beforeTime);
            return ResponseEntity.ok(ApiResponse.success("成功删除 " + deletedCount + " 条旧数据"));
        } catch (Exception e) {
            log.error("清理旧数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("清理失败: " + e.getMessage()));
        }
    }

    /**
     * 检查传感器是否存在
     */
    @GetMapping("/sensors/{sensorId}/exists")
    @PreAuthorize("hasAnyAuthority('env:sensors:read', 'admin')")
    public ResponseEntity<ApiResponse<Boolean>> checkSensorExists(@PathVariable String sensorId) {
        try {
            boolean exists = environmentService.existsSensor(sensorId);
            return ResponseEntity.ok(ApiResponse.success(exists));
        } catch (Exception e) {
            log.error("检查传感器失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("检查失败: " + e.getMessage()));
        }
    }
}