"""一次性代码生成脚本：生成 pims-corrosion 的 VO/DTO 数据类（保持注释与注解风格统一）。"""
import os
import sys

BASE = os.path.join(sys.argv[1], 'com', 'petrochina', 'xnyqt', 'pims', 'corrosion')

HDR = '''/**
 * @Name: {n}
 * @Description: {d}
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */'''


def gen(pkg, n, d, fields):
    imps = {'com.fasterxml.jackson.annotation.JsonAutoDetect', 'io.swagger.annotations.ApiModel',
            'io.swagger.annotations.ApiModelProperty', 'lombok.Data'}
    body = []
    for t, f, c in fields:
        ann = [f'    @ApiModelProperty("{c}")']
        if t == 'LocalDate':
            imps |= {'java.time.LocalDate', 'com.fasterxml.jackson.annotation.JsonFormat'}
            ann.append('    @JsonFormat(pattern = "yyyy-MM-dd")')
        if t.startswith('List<'):
            imps |= {'java.util.List', 'java.util.ArrayList'}
        init = ' = new ArrayList<>()' if t.startswith('List<') else ''
        body.append('\n'.join(ann) + f'\n    private {t} {f}{init};\n')
    imp = '\n'.join(f'import {i};' for i in sorted(imps))
    src = (f'package com.petrochina.xnyqt.pims.corrosion.{pkg};\n\n{imp}\n\n{HDR.format(n=n, d=d)}\n@Data\n'
           f'@ApiModel("{d}")\n'
           '//按字段名序列化，保证 kValue、tYears 等字段名与接口文档一致\n'
           '@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,\n'
           '        isGetterVisibility = JsonAutoDetect.Visibility.NONE)\n'
           f'public class {n} {{\n\n' + '\n'.join(body) + '}\n')
    os.makedirs(os.path.join(BASE, pkg), exist_ok=True)
    with open(os.path.join(BASE, pkg, n + '.java'), 'w', encoding='utf-8') as fp:
        fp.write(src)


gen('vo', 'CorrosionOverviewVO', '内腐蚀概览', [
    ('Integer', 'segmentCount', '管段数'), ('Integer', 'inspectionCount', '检测记录数'),
    ('Integer', 'predictableCount', '可预测管段数'), ('Double', 'medianK', 'k 中位数（基准 n）'),
    ('Double', 'maxDepthPct', '最大腐蚀深度(%)'), ('LocalDate', 'latestInspectionDate', '最近内检日期'),
    ('String', 'modelName', '模型名称'), ('String', 'modelVersion', '模型版本'), ('Double', 'baseN', '基准指数 n')])

gen('vo', 'CorrosionSegmentVO', '管段腐蚀汇总', [
    ('Long', 'segmentId', '物理管段ID'), ('String', 'mine', '气矿'), ('String', 'workArea', '作业区'),
    ('String', 'pipelineName', '管线'), ('String', 'segmentName', '管段'), ('LocalDate', 'commissionDate', '投运日期'),
    ('Double', 'wallThicknessMm', '名义壁厚(mm)'), ('String', 'sourceSegmentIds', '归并源管段ID'),
    ('Integer', 'inspectionCount', '检测记录数'), ('LocalDate', 'latestInspectionDate', '最近有效内检日期'),
    ('Double', 'latestMaxDepthPct', '最近内检最大腐蚀深度(%)'), ('Double', 'd0Mm', 'd0(mm)'), ('Double', 'tYears', 'T(年)'),
    ('Double', 'kValue', 'k（基准 n）'), ('Boolean', 'predictable', '是否可预测'), ('String', 'grade', '数据可信等级'),
    ('String', 'gradeName', '可信等级名称'), ('String', 'note', '说明')])

gen('vo', 'InspectionVO', '内腐蚀检测记录', [
    ('Long', 'inspectionId', '记录ID'), ('Long', 'sourceSegmentId', '源管段ID'), ('String', 'segmentName', '管段'),
    ('LocalDate', 'inspectionDate', '内检日期'), ('Double', 'serviceYears', '检测时服役年限(年)'),
    ('Double', 'outerDiameterMm', '外径(mm)'), ('Double', 'wallThicknessMm', '壁厚(mm)'), ('String', 'materialGrade', '材质等级'),
    ('Double', 'designPressureMpa', '设计压力(MPa)'), ('Double', 'operatingPressureMpa', '运行压力(MPa)'),
    ('String', 'operatingPressureText', '运行压力原始值'), ('Double', 'pressureFactor', '压力系数'),
    ('String', 'operatingTemperature', '运行温度'), ('String', 'mediumType', '介质类型'), ('String', 'liquidAccumulation', '是否积液'),
    ('Double', 'co2MolPct', 'CO2(mol%)'), ('Double', 'h2sMolPct', 'H2S(mol%)'), ('Double', 'o2MolPct', 'O2(mol%)'),
    ('Double', 'waterCutPct', '含水率(%)'), ('Double', 'chlorideMgL', 'Cl-(mg/L)'), ('Double', 'ph', 'pH'),
    ('String', 'inhibitorDosing', '缓蚀剂投加'), ('String', 'inhibitorConcentration', '缓蚀剂浓度'),
    ('Integer', 'piggingCycleDays', '清管周期(天)'), ('Integer', 'defectCount', '内腐蚀缺陷数量'),
    ('Double', 'maxDepthPct', '最大腐蚀深度(%)'), ('Double', 'maxDepthMm', '最大腐蚀深度(mm)'), ('Double', 'avgErf', '平均ERF'),
    ('Double', 'maxErf', '最大ERF'), ('String', 'remark', '备注'), ('Boolean', 'isLatest', '是否为参数锚点（最近有效内检）')])

gen('vo', 'ModelParameterVO', '幂律模型参数', [
    ('Long', 'segmentId', '管段ID'), ('String', 'segmentName', '管段'), ('String', 'modelName', '模型名称'),
    ('String', 'modelVersion', '模型版本'), ('Double', 'n', '增长指数 n'), ('Double', 'kValue', '腐蚀发展系数 k'),
    ('String', 'kUnit', 'k 单位'), ('Double', 'd0Mm', 'd0(mm)'), ('Double', 'tYears', 'T(年)'),
    ('Double', 'maxDepthPct', '锚点最大腐蚀深度(%)'), ('Double', 'wallThicknessMm', '壁厚(mm)'),
    ('LocalDate', 'anchorDate', '锚点内检日期'), ('LocalDate', 'commissionDate', '投运日期'), ('String', 'timeOrigin', '时间原点'),
    ('String', 'timeOriginName', '时间原点说明'), ('String', 'grade', '可信等级'), ('String', 'gradeName', '可信等级名称'),
    ('String', 'parameterSource', '参数来源')])

gen('vo', 'CurvePointVO', '趋势曲线点', [
    ('LocalDate', 'date', '日期'), ('Double', 'tYears', '自投运起年数'), ('Double', 'depthMm', '深度(mm)')])

gen('vo', 'InspectionPointVO', '内检实测点', [
    ('LocalDate', 'date', '内检日期'), ('Double', 'tYears', '自投运起年数'), ('Double', 'depthMm', '最大深度(mm)'),
    ('Double', 'depthPct', '最大深度(%)'), ('Boolean', 'isAnchor', '是否为锚点')])

gen('vo', 'ScenarioResultVO', '情景预测结果', [
    ('Double', 'n', '增长指数 n'), ('Double', 'kValue', 'k'), ('Double', 'predDepthMm', '预测深度(mm)'),
    ('Double', 'depthRatio', '深度比（占名义壁厚）'), ('Double', 'remainingWallMm', '剩余壁厚(mm)'),
    ('String', 'limitFlag', '越界标识'), ('String', 'limitFlagName', '越界标识名称'),
    ('LocalDate', 'outOfRangeDate', '达到名义壁厚日期'), ('List<CurvePointVO>', 'curve', '趋势曲线')])

gen('vo', 'PredictionVO', '内腐蚀趋势预测结果', [
    ('String', 'calcId', '计算ID'), ('String', 'calcTime', '计算时间'), ('ModelParameterVO', 'parameter', '模型参数'),
    ('LocalDate', 'targetDate', '目标日期'), ('Double', 'deltaYears', '距最近内检年数'),
    ('ScenarioResultVO', 'main', '主情景结果'), ('List<ScenarioResultVO>', 'scenarios', '情景对比'),
    ('List<InspectionPointVO>', 'inspectionPoints', '内检实测点'), ('List<String>', 'warnings', '提示信息')])

gen('vo', 'PredictSummaryVO', '管段预测摘要（供失效风险服务调用）', [
    ('Long', 'segmentId', '管段ID'), ('String', 'segmentName', '管段'), ('LocalDate', 'targetDate', '目标日期'),
    ('Double', 'predDepthMm', '预测深度(mm)'), ('Double', 'depthRatio', '深度比'), ('Double', 'remainingWallMm', '剩余壁厚(mm)'),
    ('String', 'limitFlag', '越界标识'), ('String', 'limitFlagName', '越界标识名称')])

gen('vo', 'ApparentExponentVO', '双期记录表观指数', [
    ('Long', 'sourceSegmentId', '源管段ID'), ('String', 'segmentName', '管段'), ('LocalDate', 'date1', '第一期日期'),
    ('LocalDate', 'date2', '第二期日期'), ('Double', 't1Years', 'T1(年)'), ('Double', 't2Years', 'T2(年)'),
    ('Double', 'd1Pct', 'D1(%)'), ('Double', 'd2Pct', 'D2(%)'), ('Double', 'nApp', '表观指数 n_app'),
    ('String', 'direction', '变化方向'), ('String', 'directionName', '变化方向名称'), ('String', 'judgement', '数据判断')])

gen('vo', 'InspectionImportResultVO', '内腐蚀数据导入结果', [
    ('Integer', 'total', '总行数'), ('Integer', 'imported', '导入行数'), ('Integer', 'segmentCount', '物理管段数'),
    ('List<String>', 'messages', '提示信息')])
print('ok')
