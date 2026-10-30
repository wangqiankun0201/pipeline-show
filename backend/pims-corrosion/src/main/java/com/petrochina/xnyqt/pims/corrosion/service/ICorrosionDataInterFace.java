package com.petrochina.xnyqt.pims.corrosion.service;

import com.petrochina.xnyqt.pims.corrosion.vo.ApparentExponentVO;
import com.petrochina.xnyqt.pims.corrosion.vo.CorrosionOverviewVO;
import com.petrochina.xnyqt.pims.corrosion.vo.CorrosionSegmentVO;
import com.petrochina.xnyqt.pims.corrosion.vo.InspectionImportResultVO;
import com.petrochina.xnyqt.pims.corrosion.vo.InspectionVO;

import java.io.InputStream;
import java.util.List;

/**
 * @Name: ICorrosionDataInterFace
 * @Description: 内腐蚀数据业务接口（导入、管段汇总、检测记录、表观指数）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public interface ICorrosionDataInterFace {

    /**
     * @Name: importInspections
     * @Description: 全量导入内腐蚀数据，按“气矿—管线—管段”归并物理管段并刷新参数库
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: InputStream input 文件流, String batchName 导入批次（文件名）
     * @Return: InspectionImportResultVO 导入结果
     */
    InspectionImportResultVO importInspections(InputStream input, String batchName);

    /**
     * @Name: countInspections
     * @Description: 检测记录总数
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: long 记录数
     */
    long countInspections();

    /**
     * @Name: listSegments
     * @Description: 管段腐蚀汇总（含基准 n 参数与可计算性）
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: List CorrosionSegmentVO 汇总列表
     */
    List<CorrosionSegmentVO> listSegments();

    /**
     * @Name: listInspections
     * @Description: 管段检测记录
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: Long segmentId 管段ID
     * @Return: List InspectionVO 检测记录
     */
    List<InspectionVO> listInspections(Long segmentId);

    /**
     * @Name: getOverview
     * @Description: 内腐蚀概览统计
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: CorrosionOverviewVO 概览
     */
    CorrosionOverviewVO getOverview();

    /**
     * @Name: listApparentExponents
     * @Description: 相同源管段ID双期记录的表观指数
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: List ApparentExponentVO 表观指数
     */
    List<ApparentExponentVO> listApparentExponents();
}
