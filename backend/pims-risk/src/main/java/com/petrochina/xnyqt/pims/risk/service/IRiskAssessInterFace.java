package com.petrochina.xnyqt.pims.risk.service;

import com.petrochina.xnyqt.pims.risk.dto.AssessRequestDTO;
import com.petrochina.xnyqt.pims.risk.vo.AssessmentVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskFactorGroupVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskInputsVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskMatrixVO;
import com.petrochina.xnyqt.pims.risk.vo.RiskSegmentVO;

import java.util.List;

/**
 * @Name: IRiskAssessInterFace
 * @Description: 失效风险评价业务接口
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public interface IRiskAssessInterFace {

    /**
     * @Name: listFactors
     * @Description: 风险因素定义（按失效可能性/失效后果分组）
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: List RiskFactorGroupVO 因素分组
     */
    List<RiskFactorGroupVO> listFactors();

    /**
     * @Name: getMatrix
     * @Description: 风险矩阵配置
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: RiskMatrixVO 矩阵
     */
    RiskMatrixVO getMatrix();

    /**
     * @Name: listSegments
     * @Description: 管段风险概况（含内腐蚀预测摘要与最近评价状态）
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: List RiskSegmentVO 管段风险概况
     */
    List<RiskSegmentVO> listSegments();

    /**
     * @Name: getInputs
     * @Description: 由内腐蚀预测与管段数据预填风险因素
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: Long segmentId 管段ID, Double evaluateYears 评价年限
     * @Return: RiskInputsVO 预填输入
     */
    RiskInputsVO getInputs(Long segmentId, Double evaluateYears);

    /**
     * @Name: assess
     * @Description: 校验输入并调用当前计算模型，保存评价记录
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: AssessRequestDTO request 评价请求
     * @Return: AssessmentVO 评价结果
     */
    AssessmentVO assess(AssessRequestDTO request);

    /**
     * @Name: listAssessments
     * @Description: 评价记录（按时间倒序）
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: Long segmentId 管段ID（可空）
     * @Return: List AssessmentVO 评价记录
     */
    List<AssessmentVO> listAssessments(Long segmentId);
}
