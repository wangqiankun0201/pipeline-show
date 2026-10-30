package com.petrochina.xnyqt.pims.risk.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.common.util.NumberUtil;
import com.petrochina.xnyqt.pims.risk.model.RiskFactorModel;
import com.petrochina.xnyqt.pims.risk.vo.FactorOptionVO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Name: RiskInputUtil
 * @Description: 风险因素输入校验与规范化（类型、范围、选项），拒绝未定义的因素
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public final class RiskInputUtil {

    //数值型因素
    public static final String TYPE_NUMBER = "number";

    //选择型因素
    public static final String TYPE_SELECT = "select";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RiskInputUtil() {
    }

    /**
     * @Name: parseOptions
     * @Description: 解析因素选项 JSON
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: String optionsJson 选项 JSON
     * @Return: List FactorOptionVO 选项
     */
    public static List<FactorOptionVO> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(optionsJson, new TypeReference<List<FactorOptionVO>>() {
            });
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * @Name: normalizeInputs
     * @Description: 按因素定义校验并规范化输入：数值转 Double，选项须在定义范围内
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: Map inputs 原始输入, Map factors 因素定义（编码→定义）
     * @Return: Map 规范化后的输入
     */
    public static Map<String, Object> normalizeInputs(Map<String, Object> inputs, Map<String, RiskFactorModel> factors) {
        List<String> errors = new ArrayList<>();
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            RiskFactorModel factor = factors.get(entry.getKey());
            if (factor == null) {
                errors.add("未定义的风险因素 " + entry.getKey());
                continue;
            }
            String text = entry.getValue() == null ? "" : String.valueOf(entry.getValue()).trim();
            //空值视为未填写
            if (text.isEmpty()) {
                continue;
            }
            String error = TYPE_NUMBER.equals(factor.getInputType())
                    ? putNumber(normalized, factor, text) : putOption(normalized, factor, text);
            if (error != null) {
                errors.add(error);
            }
        }
        List<String> missing = factors.values().stream()
                .filter(factor -> Boolean.TRUE.equals(factor.getRequired()) && !normalized.containsKey(factor.getFactorCode()))
                .map(RiskFactorModel::getFactorName).collect(Collectors.toList());
        if (!missing.isEmpty()) {
            errors.add("缺少必填因素：" + String.join("、", missing));
        }
        if (!errors.isEmpty()) {
            throw new BusinessException(String.join("；", errors));
        }
        return normalized;
    }

    private static String putNumber(Map<String, Object> normalized, RiskFactorModel factor, String text) {
        Double value = NumberUtil.parseDouble(text);
        String error = null;
        if (value == null) {
            error = factor.getFactorName() + " 须为数值";
        } else if ((factor.getMinValue() != null && value < factor.getMinValue())
                || (factor.getMaxValue() != null && value > factor.getMaxValue())) {
            error = String.format("%s 须在 %s ~ %s 之间", factor.getFactorName(),
                    factor.getMinValue() == null ? "-∞" : factor.getMinValue(),
                    factor.getMaxValue() == null ? "+∞" : factor.getMaxValue());
        } else {
            normalized.put(factor.getFactorCode(), value);
        }
        return error;
    }

    private static String putOption(Map<String, Object> normalized, RiskFactorModel factor, String text) {
        boolean valid = parseOptions(factor.getOptionsJson()).stream().anyMatch(option -> option.getValue().equals(text));
        if (!valid) {
            return factor.getFactorName() + " 的取值不在可选范围内";
        }
        normalized.put(factor.getFactorCode(), text);
        return null;
    }
}
