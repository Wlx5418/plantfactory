package com.qzsf.plantfactoryspring.service;

import com.qzsf.plantfactoryspring.dto.EnvironmentDataDTO;
import com.qzsf.plantfactoryspring.entity.EnvironmentData;
import com.qzsf.plantfactoryspring.repository.EnvironmentDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Excel导入服务
 * 提供完整的事务管理和详细的错误报告
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final EnvironmentDataRepository environmentDataRepository;

    // 期望的Excel列名
    private static final String[] EXPECTED_COLUMNS = {
        "区域ID", "数据时间", "温度", "湿度", "光照强度", "CO2浓度", "pH值", "EC值", "土壤湿度"
    };

    // 数据验证范围
    private static final double MIN_TEMPERATURE = -50.0;
    private static final double MAX_TEMPERATURE = 60.0;
    private static final double MIN_HUMIDITY = 0.0;
    private static final double MAX_HUMIDITY = 100.0;
    private static final double MIN_LIGHT_INTENSITY = 0.0;
    private static final double MAX_LIGHT_INTENSITY = 100000.0;
    private static final double MIN_CO2_LEVEL = 0.0;
    private static final double MAX_CO2_LEVEL = 5000.0;
    private static final double MIN_PH_VALUE = 0.0;
    private static final double MAX_PH_VALUE = 14.0;
    private static final double MIN_EC_VALUE = 0.0;
    private static final double MAX_EC_VALUE = 50.0;
    private static final double MIN_SOIL_MOISTURE = 0.0;
    private static final double MAX_SOIL_MOISTURE = 100.0;

    /**
     * 导入结果类
     */
    public static class ImportResult {
        private final int totalRows;
        private final int successCount;
        private final int failureCount;
        private final List<ImportError> errors;
        private final String summary;
        private final long processingTimeMs;

        public ImportResult(int totalRows, int successCount, int failureCount,
                          List<ImportError> errors, long processingTimeMs) {
            this.totalRows = totalRows;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.errors = errors;
            this.processingTimeMs = processingTimeMs;
            this.summary = String.format(
                "导入完成 - 总行数: %d, 成功: %d, 失败: %d, 成功率: %.2f%%",
                totalRows, successCount, failureCount,
                totalRows > 0 ? (successCount * 100.0 / totalRows) : 0.0
            );
        }

        // Getters
        public int getTotalRows() { return totalRows; }
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public List<ImportError> getErrors() { return errors; }
        public String getSummary() { return summary; }
        public long getProcessingTimeMs() { return processingTimeMs; }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean isCompletelySuccessful() {
            return failureCount == 0 && totalRows > 0;
        }
    }

    /**
     * 导入错误类
     */
    public static class ImportError {
        private final int rowIndex;
        private final String columnName;
        private final String errorMessage;
        private final String cellValue;
        private final String suggestedFix;

        public ImportError(int rowIndex, String columnName, String errorMessage,
                         String cellValue, String suggestedFix) {
            this.rowIndex = rowIndex;
            this.columnName = columnName;
            this.errorMessage = errorMessage;
            this.cellValue = cellValue;
            this.suggestedFix = suggestedFix;
        }

        // Getters
        public int getRowIndex() { return rowIndex; }
        public String getColumnName() { return columnName; }
        public String getErrorMessage() { return errorMessage; }
        public String getCellValue() { return cellValue; }
        public String getSuggestedFix() { return suggestedFix; }
    }

    /**
     * 导入环境数据Excel文件
     */
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importEnvironmentData(MultipartFile file, Long areaId, Long operatorId) {
        long startTime = System.currentTimeMillis();
        AtomicInteger totalRows = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<ImportError> errors = new ArrayList<>();

        try {
            log.info("开始导入环境数据Excel文件: {}, 区域ID: {}", file.getOriginalFilename(), areaId);

            // 验证文件
            validateFile(file);

            // 解析Excel文件
            List<EnvironmentDataDTO> validData = parseExcelFile(file, areaId, operatorId,
                totalRows, successCount, failureCount, errors);

            if (validData.isEmpty()) {
                log.warn("没有有效数据可导入");
                return new ImportResult(totalRows.get(), successCount.get(), failureCount.get(),
                    errors, System.currentTimeMillis() - startTime);
            }

            // 批量保存数据（事务内）
            List<EnvironmentData> savedEntities = new ArrayList<>();
            for (EnvironmentDataDTO dto : validData) {
                try {
                    List<EnvironmentData> entities = convertToEntities(dto);
                    savedEntities.addAll(entities);
                } catch (Exception e) {
                    log.error("转换DTO到实体失败: {}", dto, e);
                    failureCount.incrementAndGet();
                    successCount.decrementAndGet(); // 减少之前计数
                    errors.add(new ImportError(
                        dto.getRowIndex() != null ? dto.getRowIndex() : 0,
                        "数据转换",
                        "无法转换数据格式: " + e.getMessage(),
                        dto.toString(),
                        "检查数据格式是否正确"
                    ));
                }
            }

            // 批量保存
            if (!savedEntities.isEmpty()) {
                try {
                    environmentDataRepository.saveAll(savedEntities);
                    log.info("成功保存 {} 条环境数据记录", savedEntities.size());
                } catch (Exception e) {
                    log.error("批量保存环境数据失败", e);
                    throw new RuntimeException("保存数据失败: " + e.getMessage(), e);
                }
            }

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Excel导入完成 - {}", new ImportResult(
                totalRows.get(), successCount.get(), failureCount.get(), errors, processingTime).getSummary());

            return new ImportResult(totalRows.get(), successCount.get(), failureCount.get(), errors, processingTime);

        } catch (Exception e) {
            log.error("Excel导入过程中发生异常", e);
            // 事务会自动回滚
            errors.add(new ImportError(0, "系统错误", "导入过程异常: " + e.getMessage(), "",
                "请联系系统管理员或检查文件格式"));
            throw new RuntimeException("Excel导入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new IllegalArgumentException("只支持Excel文件格式 (.xlsx, .xls)");
        }

        if (file.getSize() > 50 * 1024 * 1024) { // 50MB
            throw new IllegalArgumentException("文件大小不能超过50MB");
        }
    }

    /**
     * 解析Excel文件
     */
    private List<EnvironmentDataDTO> parseExcelFile(MultipartFile file, Long areaId, Long operatorId,
                                                   AtomicInteger totalRows, AtomicInteger successCount,
                                                   AtomicInteger failureCount, List<ImportError> errors) {
        List<EnvironmentDataDTO> validData = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Excel文件为空或格式错误");
            }

            // 验证表头
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel文件缺少表头");
            }

            validateHeaders(headerRow);

            // 解析数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                totalRows.incrementAndGet();
                Row row = sheet.getRow(i);

                if (row == null || isRowEmpty(row)) {
                    failureCount.incrementAndGet();
                    errors.add(new ImportError(i + 1, "整行", "行为空", "", "删除空行或填写数据"));
                    continue;
                }

                try {
                    EnvironmentDataDTO dto = parseRow(row, i + 1, areaId, operatorId, errors);
                    if (dto != null) {
                        validData.add(dto);
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    errors.add(new ImportError(i + 1, "整行", "解析失败: " + e.getMessage(), "",
                        "检查该行数据格式"));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("读取Excel文件失败: " + e.getMessage(), e);
        }

        return validData;
    }

    /**
     * 验证表头
     */
    private void validateHeaders(Row headerRow) {
        StringBuilder missingColumns = new StringBuilder();

        for (int i = 0; i < EXPECTED_COLUMNS.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null || cell.getStringCellValue().trim().isEmpty()) {
                missingColumns.append(EXPECTED_COLUMNS[i]).append(", ");
            }
        }

        if (missingColumns.length() > 0) {
            throw new IllegalArgumentException(
                "缺少必要的表头列: " + missingColumns.substring(0, missingColumns.length() - 2));
        }
    }

    /**
     * 解析数据行
     */
    private EnvironmentDataDTO parseRow(Row row, int rowIndex, Long areaId, Long operatorId,
                                       List<ImportError> errors) {
        EnvironmentDataDTO dto = new EnvironmentDataDTO();
        dto.setRowIndex(rowIndex);
        boolean hasError = false;

        try {
            // 区域ID（使用传入的区域ID，忽略Excel中的区域ID）
            dto.setAreaId(areaId);

            // 数据时间
            Cell timeCell = row.getCell(1);
            if (timeCell != null) {
                LocalDateTime dataTime = parseDateTime(timeCell, rowIndex, "数据时间", errors);
                if (dataTime == null) {
                    hasError = true;
                } else {
                    dto.setDataTime(dataTime);
                }
            } else {
                errors.add(new ImportError(rowIndex, "数据时间", "数据时间为空", "", "填写有效的数据时间"));
                hasError = true;
            }

            // 温度
            Cell tempCell = row.getCell(2);
            if (tempCell != null) {
                Double temperature = parseNumericCell(tempCell, rowIndex, "温度",
                    MIN_TEMPERATURE, MAX_TEMPERATURE, errors);
                if (temperature == null) {
                    hasError = true;
                } else {
                    dto.setTemperature(temperature);
                }
            }

            // 湿度
            Cell humidityCell = row.getCell(3);
            if (humidityCell != null) {
                Double humidity = parseNumericCell(humidityCell, rowIndex, "湿度",
                    MIN_HUMIDITY, MAX_HUMIDITY, errors);
                if (humidity == null) {
                    hasError = true;
                } else {
                    dto.setHumidity(humidity);
                }
            }

            // 光照强度
            Cell lightCell = row.getCell(4);
            if (lightCell != null) {
                Double lightIntensity = parseNumericCell(lightCell, rowIndex, "光照强度",
                    MIN_LIGHT_INTENSITY, MAX_LIGHT_INTENSITY, errors);
                if (lightIntensity == null) {
                    hasError = true;
                } else {
                    dto.setLightIntensity(lightIntensity);
                }
            }

            // CO2浓度
            Cell co2Cell = row.getCell(5);
            if (co2Cell != null) {
                Double co2Level = parseNumericCell(co2Cell, rowIndex, "CO2浓度",
                    MIN_CO2_LEVEL, MAX_CO2_LEVEL, errors);
                if (co2Level == null) {
                    hasError = true;
                } else {
                    dto.setCo2Level(co2Level);
                }
            }

            // pH值
            Cell phCell = row.getCell(6);
            if (phCell != null) {
                Double phValue = parseNumericCell(phCell, rowIndex, "pH值",
                    MIN_PH_VALUE, MAX_PH_VALUE, errors);
                if (phValue == null) {
                    hasError = true;
                } else {
                    dto.setPhValue(phValue);
                }
            }

            // EC值
            Cell ecCell = row.getCell(7);
            if (ecCell != null) {
                Double ecValue = parseNumericCell(ecCell, rowIndex, "EC值",
                    MIN_EC_VALUE, MAX_EC_VALUE, errors);
                if (ecValue == null) {
                    hasError = true;
                } else {
                    dto.setEcValue(ecValue);
                }
            }

            // 土壤湿度
            Cell soilCell = row.getCell(8);
            if (soilCell != null) {
                Double soilMoisture = parseNumericCell(soilCell, rowIndex, "土壤湿度",
                    MIN_SOIL_MOISTURE, MAX_SOIL_MOISTURE, errors);
                if (soilMoisture == null) {
                    hasError = true;
                } else {
                    dto.setSoilMoisture(soilMoisture);
                }
            }

            // 设置操作员和数据来源
            dto.setOperatorId(operatorId);
            dto.setDataSource("EXCEL_IMPORT");

            return hasError ? null : dto;

        } catch (Exception e) {
            errors.add(new ImportError(rowIndex, "整行", "解析异常: " + e.getMessage(), "",
                "检查数据格式是否正确"));
            return null;
        }
    }

    /**
     * 解析日期时间
     */
    private LocalDateTime parseDateTime(Cell cell, int rowIndex, String columnName, List<ImportError> errors) {
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String dateStr = cell.getStringCellValue().trim();
                if (dateStr.isEmpty()) {
                    errors.add(new ImportError(rowIndex, columnName, "日期时间为空", "", "填写有效的日期时间"));
                    return null;
                }

                // 尝试多种日期格式
                String[] patterns = {
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy/MM/dd HH:mm:ss",
                    "yyyy-MM-dd HH:mm",
                    "yyyy/MM/dd HH:mm",
                    "yyyy-MM-dd",
                    "yyyy/MM/dd"
                };

                for (String pattern : patterns) {
                    try {
                        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
                    } catch (DateTimeParseException ignored) {
                        // 继续尝试下一个格式
                    }
                }

                errors.add(new ImportError(rowIndex, columnName, "日期格式不支持",
                    dateStr, "使用格式: yyyy-MM-dd HH:mm:ss"));
                return null;
            }
        } catch (Exception e) {
            errors.add(new ImportError(rowIndex, columnName, "日期解析失败",
                getCellStringValue(cell), "检查日期格式是否正确"));
            return null;
        }

        errors.add(new ImportError(rowIndex, columnName, "无效的日期格式",
            getCellStringValue(cell), "使用有效的日期格式"));
        return null;
    }

    /**
     * 解析数值单元格
     */
    private Double parseNumericCell(Cell cell, int rowIndex, String columnName,
                                   double minValue, double maxValue, List<ImportError> errors) {
        try {
            double value;
            if (cell.getCellType() == CellType.NUMERIC) {
                value = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String str = cell.getStringCellValue().trim();
                if (str.isEmpty()) {
                    return null; // 空值是允许的
                }
                value = Double.parseDouble(str);
            } else {
                errors.add(new ImportError(rowIndex, columnName, "无效的数据类型",
                    getCellStringValue(cell), "使用数字格式"));
                return null;
            }

            if (value < minValue || value > maxValue) {
                errors.add(new ImportError(rowIndex, columnName, "数值超出范围",
                    String.valueOf(value), String.format("数值应在 %.1f - %.1f 之间", minValue, maxValue)));
                return null;
            }

            return value;
        } catch (NumberFormatException e) {
            errors.add(new ImportError(rowIndex, columnName, "数字格式错误",
                getCellStringValue(cell), "使用有效的数字格式"));
            return null;
        } catch (Exception e) {
            errors.add(new ImportError(rowIndex, columnName, "解析失败: " + e.getMessage(),
                getCellStringValue(cell), "检查数据格式"));
            return null;
        }
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    return String.valueOf(cell.getNumericCellValue());
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 检查行是否为空
     */
    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellStringValue(cell);
                if (!value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 转换DTO为实体列表
     * 一个Excel行包含多种环境数据，需要转换为多个EnvironmentData实体
     */
    private List<EnvironmentData> convertToEntities(EnvironmentDataDTO dto) {
        List<EnvironmentData> entities = new ArrayList<>();
        LocalDateTime timestamp = dto.getDataTime() != null ? dto.getDataTime() : LocalDateTime.now();

        // 温度数据
        if (dto.getTemperature() != null) {
            entities.add(EnvironmentData.builder()
                .sensorId("EXCEL_IMPORT_TEMP_" + dto.getAreaId())
                .dataType(EnvironmentData.DataType.TEMPERATURE)
                .value(BigDecimal.valueOf(dto.getTemperature()))
                .unit("°C")
                .collectedAt(timestamp)
                .source("EXCEL_IMPORT")
                .status(EnvironmentData.DataStatus.NORMAL)
                .build());
        }

        // 湿度数据
        if (dto.getHumidity() != null) {
            entities.add(EnvironmentData.builder()
                .sensorId("EXCEL_IMPORT_HUMIDITY_" + dto.getAreaId())
                .dataType(EnvironmentData.DataType.HUMIDITY)
                .value(BigDecimal.valueOf(dto.getHumidity()))
                .unit("%RH")
                .collectedAt(timestamp)
                .source("EXCEL_IMPORT")
                .status(EnvironmentData.DataStatus.NORMAL)
                .build());
        }

        // 光照强度数据
        if (dto.getLightIntensity() != null) {
            entities.add(EnvironmentData.builder()
                .sensorId("EXCEL_IMPORT_LIGHT_" + dto.getAreaId())
                .dataType(EnvironmentData.DataType.LIGHT_INTENSITY)
                .value(BigDecimal.valueOf(dto.getLightIntensity()))
                .unit("lx")
                .collectedAt(timestamp)
                .source("EXCEL_IMPORT")
                .status(EnvironmentData.DataStatus.NORMAL)
                .build());
        }

        // CO2浓度数据
        if (dto.getCo2Level() != null) {
            entities.add(EnvironmentData.builder()
                .sensorId("EXCEL_IMPORT_CO2_" + dto.getAreaId())
                .dataType(EnvironmentData.DataType.CO2)
                .value(BigDecimal.valueOf(dto.getCo2Level()))
                .unit("ppm")
                .collectedAt(timestamp)
                .source("EXCEL_IMPORT")
                .status(EnvironmentData.DataStatus.NORMAL)
                .build());
        }

        // 土壤湿度数据
        if (dto.getSoilMoisture() != null) {
            entities.add(EnvironmentData.builder()
                .sensorId("EXCEL_IMPORT_SOIL_" + dto.getAreaId())
                .dataType(EnvironmentData.DataType.SOIL_MOISTURE)
                .value(BigDecimal.valueOf(dto.getSoilMoisture()))
                .unit("%")
                .collectedAt(timestamp)
                .source("EXCEL_IMPORT")
                .status(EnvironmentData.DataStatus.NORMAL)
                .build());
        }

        // pH值数据
        if (dto.getPhValue() != null) {
            entities.add(EnvironmentData.builder()
                .sensorId("EXCEL_IMPORT_PH_" + dto.getAreaId())
                .dataType(EnvironmentData.DataType.PH)
                .value(BigDecimal.valueOf(dto.getPhValue()))
                .unit("pH")
                .collectedAt(timestamp)
                .source("EXCEL_IMPORT")
                .status(EnvironmentData.DataStatus.NORMAL)
                .build());
        }

        return entities;
    }
}