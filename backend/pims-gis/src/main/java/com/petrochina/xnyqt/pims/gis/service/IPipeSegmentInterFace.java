package com.petrochina.xnyqt.pims.gis.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.petrochina.xnyqt.pims.gis.dto.SegmentQueryDTO;
import com.petrochina.xnyqt.pims.gis.vo.GeoImportResultVO;
import com.petrochina.xnyqt.pims.gis.vo.SegmentVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Name: IPipeSegmentInterFace
 * @Description: 管段位置业务接口
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public interface IPipeSegmentInterFace {

    /**
     * @Name: listSegments
     * @Description: 按条件查询管段位置信息
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: SegmentQueryDTO query 查询条件
     * @Return: List SegmentVO 管段列表
     */
    List<SegmentVO> listSegments(SegmentQueryDTO query);

    /**
     * @Name: getSegment
     * @Description: 查询单个管段，不存在抛出业务异常
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: Long segmentId 管段ID
     * @Return: SegmentVO 管段
     */
    SegmentVO getSegment(Long segmentId);

    /**
     * @Name: getFeatureCollection
     * @Description: 输出全部管段 GeoJSON FeatureCollection
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: SegmentQueryDTO query 查询条件
     * @Return: ObjectNode FeatureCollection
     */
    ObjectNode getFeatureCollection(SegmentQueryDTO query);

    /**
     * @Name: importGeoJson
     * @Description: 导入 GeoJSON，按 properties.segmentId 更新管段几何
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: MultipartFile file GeoJSON 文件
     * @Return: GeoImportResultVO 导入结果
     */
    GeoImportResultVO importGeoJson(MultipartFile file);
}
