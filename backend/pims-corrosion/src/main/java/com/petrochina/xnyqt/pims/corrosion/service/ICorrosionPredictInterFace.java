package com.petrochina.xnyqt.pims.corrosion.service;

import com.petrochina.xnyqt.pims.corrosion.dto.PredictRequestDTO;
import com.petrochina.xnyqt.pims.corrosion.vo.PredictSummaryVO;
import com.petrochina.xnyqt.pims.corrosion.vo.PredictionVO;

import java.util.List;

/**
 * @Name: ICorrosionPredictInterFace
 * @Description: 内腐蚀趋势预测业务接口
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public interface ICorrosionPredictInterFace {

    /**
     * @Name: predict
     * @Description: 按最近内检锚定幂律模型预测目标日期腐蚀深度，并输出情景曲线、留痕
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: PredictRequestDTO request 预测请求
     * @Return: PredictionVO 预测结果
     */
    PredictionVO predict(PredictRequestDTO request);

    /**
     * @Name: summarizeAll
     * @Description: 全部可计算管段在基准 n 下内检后指定年数的预测摘要（供失效风险服务）
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: double yearsAfterIli 内检后年数
     * @Return: List PredictSummaryVO 预测摘要
     */
    List<PredictSummaryVO> summarizeAll(double yearsAfterIli);
}
