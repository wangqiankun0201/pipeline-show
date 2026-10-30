package com.petrochina.xnyqt.pims.gis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.common.util.NumberUtil;
import com.petrochina.xnyqt.pims.gis.dao.PipeSegmentDAO;
import com.petrochina.xnyqt.pims.gis.dao.PipeSegmentGeometryDAO;
import com.petrochina.xnyqt.pims.gis.dto.SegmentQueryDTO;
import com.petrochina.xnyqt.pims.gis.model.PipeSegmentGeometryModel;
import com.petrochina.xnyqt.pims.gis.model.PipeSegmentModel;
import com.petrochina.xnyqt.pims.gis.service.IPipeSegmentInterFace;
import com.petrochina.xnyqt.pims.gis.type.CoordStatusType;
import com.petrochina.xnyqt.pims.gis.util.GeoUtil;
import com.petrochina.xnyqt.pims.gis.vo.GeoImportResultVO;
import com.petrochina.xnyqt.pims.gis.vo.SegmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Name: PipeSegmentImpl
 * @Description: 管段位置业务实现
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Slf4j
@Service
public class PipeSegmentImpl implements IPipeSegmentInterFace {

    //GeoJSON 文件大小上限（字节）
    private static final long MAX_GEOJSON_BYTES = 20L * 1024 * 1024;

    private final PipeSegmentDAO segmentDAO;

    private final PipeSegmentGeometryDAO geometryDAO;

    private final ObjectMapper objectMapper;

    public PipeSegmentImpl(PipeSegmentDAO segmentDAO, PipeSegmentGeometryDAO geometryDAO, ObjectMapper objectMapper) {
        this.segmentDAO = segmentDAO;
        this.geometryDAO = geometryDAO;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<SegmentVO> listSegments(SegmentQueryDTO query) {
        LambdaQueryWrapper<PipeSegmentModel> wrapper = new LambdaQueryWrapper<PipeSegmentModel>()
                .eq(StringUtils.hasText(query.getMine()), PipeSegmentModel::getMine, query.getMine())
                .eq(StringUtils.hasText(query.getPipelineName()), PipeSegmentModel::getPipelineName, query.getPipelineName())
                .like(StringUtils.hasText(query.getKeyword()), PipeSegmentModel::getSegmentName, query.getKeyword())
                .orderByAsc(PipeSegmentModel::getMine, PipeSegmentModel::getPipelineName, PipeSegmentModel::getSegmentId);
        List<PipeSegmentModel> segments = segmentDAO.selectList(wrapper);
        Map<Long, PipeSegmentGeometryModel> geometryMap = geometryDAO.selectList(null).stream()
                .collect(Collectors.toMap(PipeSegmentGeometryModel::getSegmentId, Function.identity()));
        return segments.stream()
                .map(segment -> toVO(segment, geometryMap.get(segment.getSegmentId())))
                .collect(Collectors.toList());
    }

    @Override
    public SegmentVO getSegment(Long segmentId) {
        PipeSegmentModel segment = segmentDAO.selectById(segmentId);
        if (segment == null) {
            throw new BusinessException("管段不存在：" + segmentId);
        }
        return toVO(segment, geometryDAO.selectById(segmentId));
    }

    @Override
    public ObjectNode getFeatureCollection(SegmentQueryDTO query) {
        ObjectNode collection = objectMapper.createObjectNode();
        collection.put("type", "FeatureCollection");
        ArrayNode features = collection.putArray("features");
        for (SegmentVO segment : listSegments(query)) {
            //无几何的管段不输出到地图
            if (segment.getGeometry() == null) {
                continue;
            }
            ObjectNode feature = features.addObject();
            feature.put("type", "Feature");
            feature.put("id", segment.getSegmentId());
            feature.set("geometry", segment.getGeometry());
            ObjectNode properties = objectMapper.valueToTree(segment);
            properties.remove("geometry");
            feature.set("properties", properties);
        }
        return collection;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GeoImportResultVO importGeoJson(MultipartFile file) {
        JsonNode root = readGeoJson(file);
        GeoImportResultVO result = new GeoImportResultVO();
        JsonNode features = root.path("features");
        result.setTotal(features.size());
        for (JsonNode feature : features) {
            String message = importFeature(feature, file.getOriginalFilename());
            //message 为空表示导入成功
            if (message == null) {
                result.setUpdated(result.getUpdated() + 1);
            } else {
                result.setSkipped(result.getSkipped() + 1);
                result.getMessages().add(message);
            }
        }
        log.info("GIS 数据导入完成：总数 {}，更新 {}，跳过 {}", result.getTotal(), result.getUpdated(), result.getSkipped());
        return result;
    }

    /**
     * @Name: readGeoJson
     * @Description: 读取并校验 GeoJSON FeatureCollection 文件
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: MultipartFile file 文件
     * @Return: JsonNode 根节点
     */
    private JsonNode readGeoJson(MultipartFile file) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (file.isEmpty() || file.getSize() > MAX_GEOJSON_BYTES) {
            throw new BusinessException("文件为空或超过 20MB");
        }
        if (!filename.endsWith(".geojson") && !filename.endsWith(".json")) {
            throw new BusinessException("仅支持 .geojson / .json 文件");
        }
        JsonNode root;
        try (InputStream input = file.getInputStream()) {
            root = objectMapper.readTree(input);
        } catch (IOException e) {
            throw new BusinessException("GeoJSON 解析失败，请检查文件格式");
        }
        if (!"FeatureCollection".equals(root.path("type").asText())) {
            throw new BusinessException("GeoJSON 须为 FeatureCollection");
        }
        return root;
    }

    /**
     * @Name: importFeature
     * @Description: 导入单个要素，返回跳过原因，成功返回空
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: JsonNode feature 要素, String source 来源文件名
     * @Return: String 跳过原因
     */
    private String importFeature(JsonNode feature, String source) {
        JsonNode properties = feature.path("properties");
        long segmentId = properties.path("segmentId").asLong(-1);
        JsonNode geometry = feature.path("geometry");
        String error = GeoUtil.validateLine(geometry);
        if (segmentId < 0 || segmentDAO.selectById(segmentId) == null) {
            error = "未匹配到管段（properties.segmentId=" + properties.path("segmentId").asText() + "）";
        }
        if (error != null) {
            return error;
        }
        PipeSegmentGeometryModel model = buildGeometry(segmentId, geometry);
        model.setStartName(textOrNull(properties, "startName"));
        model.setEndName(textOrNull(properties, "endName"));
        model.setCoordStatus(CoordStatusType.SURVEYED.name());
        model.setGeoSource(source);
        if (geometryDAO.selectById(segmentId) == null) {
            geometryDAO.insert(model);
        } else {
            geometryDAO.updateById(model);
        }
        return null;
    }

    private PipeSegmentGeometryModel buildGeometry(long segmentId, JsonNode geometry) {
        double[] start = GeoUtil.firstPoint(geometry);
        double[] end = GeoUtil.lastPoint(geometry);
        PipeSegmentGeometryModel model = new PipeSegmentGeometryModel();
        model.setSegmentId(segmentId);
        model.setGeomType(geometry.path("type").asText());
        model.setGeojson(geometry.toString());
        model.setSrid(4326);
        model.setStartLon(start[0]);
        model.setStartLat(start[1]);
        model.setEndLon(end[0]);
        model.setEndLat(end[1]);
        model.setLengthKm(NumberUtil.round(GeoUtil.lengthKm(geometry), 3));
        model.setUpdateTime(LocalDateTime.now());
        return model;
    }

    private SegmentVO toVO(PipeSegmentModel segment, PipeSegmentGeometryModel geometry) {
        SegmentVO vo = new SegmentVO();
        vo.setSegmentId(segment.getSegmentId());
        vo.setMine(segment.getMine());
        vo.setWorkArea(segment.getWorkArea());
        vo.setPipelineName(segment.getPipelineName());
        vo.setSegmentName(segment.getSegmentName());
        vo.setCommissionDate(segment.getCommissionDate());
        vo.setOuterDiameterMm(segment.getOuterDiameterMm());
        vo.setWallThicknessMm(segment.getWallThicknessMm());
        vo.setMaterialGrade(segment.getMaterialGrade());
        vo.setDesignPressureMpa(segment.getDesignPressureMpa());
        vo.setMediumType(segment.getMediumType());
        vo.setDataSource(segment.getDataSource());
        //有空间数据时补充位置参数
        if (geometry != null) {
            JsonNode geometryNode = parseGeometry(geometry);
            vo.setGeometry(geometryNode);
            vo.setStartName(geometry.getStartName());
            vo.setEndName(geometry.getEndName());
            vo.setStartLon(geometry.getStartLon());
            vo.setStartLat(geometry.getStartLat());
            vo.setEndLon(geometry.getEndLon());
            vo.setEndLat(geometry.getEndLat());
            vo.setLengthKm(geometry.getLengthKm() != null || geometryNode == null
                    ? geometry.getLengthKm() : NumberUtil.round(GeoUtil.lengthKm(geometryNode), 3));
            vo.setCoordStatus(geometry.getCoordStatus());
            vo.setCoordStatusName(CoordStatusType.nameOf(geometry.getCoordStatus()));
        }
        return vo;
    }

    private JsonNode parseGeometry(PipeSegmentGeometryModel geometry) {
        try {
            return objectMapper.readTree(geometry.getGeojson());
        } catch (IOException e) {
            log.error("管段 {} 几何数据损坏", geometry.getSegmentId());
            return null;
        }
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
