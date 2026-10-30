package com.petrochina.xnyqt.pims.corrosion.service;

import com.petrochina.xnyqt.pims.corrosion.dto.SegmentAnchorDTO;
import com.petrochina.xnyqt.pims.corrosion.vo.ModelParameterVO;

import java.util.List;

/**
 * @Name: ICorrosionParameterInterFace
 * @Description: 幂律模型参数业务接口（锚点评估、参数反算、参数库维护）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public interface ICorrosionParameterInterFace {

    /**
     * @Name: evaluateAnchor
     * @Description: 评估单个管段：读取最近有效内检，校验字段并计算 T、d₀
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: Long segmentId 管段ID
     * @Return: SegmentAnchorDTO 评估结果
     */
    SegmentAnchorDTO evaluateAnchor(Long segmentId);

    /**
     * @Name: evaluateAllAnchors
     * @Description: 评估全部管段
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: List SegmentAnchorDTO 评估结果
     */
    List<SegmentAnchorDTO> evaluateAllAnchors();

    /**
     * @Name: buildParameter
     * @Description: 按指定 n 反算 k 并组装参数（不可计算时抛出业务异常）
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: SegmentAnchorDTO anchor 评估结果, double n 增长指数
     * @Return: ModelParameterVO 模型参数
     */
    ModelParameterVO buildParameter(SegmentAnchorDTO anchor, double n);

    /**
     * @Name: getParameter
     * @Description: 查询管段在指定 n 下的模型参数
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: Long segmentId 管段ID, double n 增长指数
     * @Return: ModelParameterVO 模型参数
     */
    ModelParameterVO getParameter(Long segmentId, double n);

    /**
     * @Name: refreshParameterLibrary
     * @Description: 重算全部可计算管段的基准 n 参数并写入参数库
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: int 写入参数条数
     */
    int refreshParameterLibrary();
}
