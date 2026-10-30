package com.petrochina.xnyqt.pims.gis.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.gis.config.MapProperties;
import com.petrochina.xnyqt.pims.gis.dto.SegmentQueryDTO;
import com.petrochina.xnyqt.pims.gis.service.IPipeSegmentInterFace;
import com.petrochina.xnyqt.pims.gis.vo.GeoImportResultVO;
import com.petrochina.xnyqt.pims.gis.vo.SegmentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Name: PipeSegmentController
 * @Description: 管道位置接口
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Api(tags = "管道位置")
@RestController
@RequestMapping("/gis")
public class PipeSegmentController {

    private final IPipeSegmentInterFace segmentService;

    private final MapProperties mapProperties;

    public PipeSegmentController(IPipeSegmentInterFace segmentService, MapProperties mapProperties) {
        this.segmentService = segmentService;
        this.mapProperties = mapProperties;
    }

    @ApiOperation("管段列表")
    @GetMapping("/segments")
    public ApiResult<List<SegmentVO>> listSegments(@Validated SegmentQueryDTO query) {
        return ApiResult.success(segmentService.listSegments(query));
    }

    @ApiOperation("管段 GeoJSON（FeatureCollection）")
    @GetMapping("/segments/geojson")
    public ApiResult<ObjectNode> getGeoJson(@Validated SegmentQueryDTO query) {
        return ApiResult.success(segmentService.getFeatureCollection(query));
    }

    @ApiOperation("管段详情")
    @GetMapping("/segments/{segmentId}")
    public ApiResult<SegmentVO> getSegment(@PathVariable("segmentId") Long segmentId) {
        return ApiResult.success(segmentService.getSegment(segmentId));
    }

    @ApiOperation("导入 GIS 数据（GeoJSON，WGS84）")
    @PostMapping("/segments/geojson/import")
    public ApiResult<GeoImportResultVO> importGeoJson(@RequestParam("file") MultipartFile file) {
        return ApiResult.success(segmentService.importGeoJson(file));
    }

    @ApiOperation("地图底图配置")
    @GetMapping("/map/config")
    public ApiResult<MapProperties> getMapConfig() {
        return ApiResult.success(mapProperties);
    }
}
