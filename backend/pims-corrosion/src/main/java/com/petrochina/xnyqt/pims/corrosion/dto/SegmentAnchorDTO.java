package com.petrochina.xnyqt.pims.corrosion.dto;

import com.petrochina.xnyqt.pims.corrosion.model.CorrosionInspectionModel;
import com.petrochina.xnyqt.pims.corrosion.model.CorrosionSegmentModel;
import com.petrochina.xnyqt.pims.corrosion.type.GradeType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Name: SegmentAnchorDTO
 * @Description: 管段锚点评估结果（内部业务对象）：最近有效内检、T、d₀、可计算性与说明
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
public class SegmentAnchorDTO {

    private CorrosionSegmentModel segment;

    //按内检日期升序的全部检测记录
    private List<CorrosionInspectionModel> inspections = new ArrayList<>();

    //最近一次有效内检（参数锚点）
    private CorrosionInspectionModel anchor;

    //投运至锚点内检年数 T
    private Double anchorYears;

    //锚点最大腐蚀深度 d₀(mm)
    private Double d0Mm;

    private boolean predictable;

    private GradeType grade = GradeType.D;

    //不可计算原因或数据核验提示
    private List<String> notes = new ArrayList<>();
}
