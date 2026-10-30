package com.petrochina.xnyqt.pims.corrosion.config;

import com.petrochina.xnyqt.pims.corrosion.service.ICorrosionDataInterFace;
import com.petrochina.xnyqt.pims.corrosion.vo.InspectionImportResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * @Name: SeedDataRunner
 * @Description: 检测记录为空且开启种子数据时，导入内置《内腐蚀数据.xlsx》（走与页面导入相同的流程）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Slf4j
@Component
public class SeedDataRunner implements ApplicationRunner {

    //种子数据批次名称
    private static final String SEED_BATCH = "内腐蚀数据.xlsx";

    private final CorrosionModelProperties properties;

    private final ICorrosionDataInterFace dataService;

    private final ResourceLoader resourceLoader;

    public SeedDataRunner(CorrosionModelProperties properties, ICorrosionDataInterFace dataService,
                          ResourceLoader resourceLoader) {
        this.properties = properties;
        this.dataService = dataService;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!properties.isSeedEnabled() || dataService.countInspections() > 0) {
            return;
        }
        Resource resource = resourceLoader.getResource(properties.getSeedLocation());
        try (InputStream input = resource.getInputStream()) {
            InspectionImportResultVO result = dataService.importInspections(input, SEED_BATCH);
            log.info("已导入种子数据：{} 行，{} 个管段，{}", result.getImported(), result.getSegmentCount(),
                    result.getMessages());
        }
    }
}
