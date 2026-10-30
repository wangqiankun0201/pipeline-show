"""一次性代码生成脚本：生成 pims-risk 的表模型、VO、Feign DTO 数据类。"""
import os
import sys

BASE = os.path.join(sys.argv[1], 'com', 'petrochina', 'xnyqt', 'pims', 'risk')
ROOT_PKG = 'com.petrochina.xnyqt.pims.risk'

HDR = '''/**
 * @Name: {n}
 * @Description: {d}
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */'''


def gen(pkg, n, d, fields, table=None, id_field=None, id_type='INPUT', swagger=True):
    imps = {'lombok.Data'}
    if swagger:
        imps |= {'io.swagger.annotations.ApiModel', 'io.swagger.annotations.ApiModelProperty'}
    if table:
        imps |= {'com.baomidou.mybatisplus.annotation.IdType', 'com.baomidou.mybatisplus.annotation.TableId',
                 'com.baomidou.mybatisplus.annotation.TableName'}
    body = []
    for t, f, c in fields:
        ann = []
        if swagger:
            ann.append(f'    @ApiModelProperty("{c}")')
        else:
            ann.append(f'    //{c}')
        if f == id_field:
            ann.append(f'    @TableId(type = IdType.{id_type})')
        if t == 'LocalDate':
            imps.add('java.time.LocalDate')
            if not table:
                imps.add('com.fasterxml.jackson.annotation.JsonFormat')
                ann.append('    @JsonFormat(pattern = "yyyy-MM-dd")')
        if t == 'LocalDateTime':
            imps.add('java.time.LocalDateTime')
        if t.startswith('List<'):
            imps |= {'java.util.List', 'java.util.ArrayList'}
        if t.startswith('Map<'):
            imps |= {'java.util.Map', 'java.util.LinkedHashMap'}
        init = ' = new ArrayList<>()' if t.startswith('List<') else (' = new LinkedHashMap<>()' if t.startswith('Map<') else '')
        body.append('\n'.join(ann) + f'\n    private {t} {f}{init};\n')
    imp = '\n'.join(f'import {i};' for i in sorted(imps))
    annos = '@Data\n'
    if table:
        annos += f'@TableName("{table}")\n'
    elif swagger:
        annos += f'@ApiModel("{d}")\n'
    src = (f'package {ROOT_PKG}.{pkg};\n\n{imp}\n\n{HDR.format(n=n, d=d)}\n{annos}'
           f'public class {n} {{\n\n' + '\n'.join(body) + '}\n')
    folder = os.path.join(BASE, *pkg.split('.'))
    os.makedirs(folder, exist_ok=True)
    with open(os.path.join(folder, n + '.java'), 'w', encoding='utf-8') as fp:
        fp.write(src)


# ---------- 表模型 ----------
gen('model', 'RiskFactorModel', '风险因素定义，对应表 t_risk_factor', [
    ('String', 'factorCode', '因素编码'), ('String', 'groupCode', '分组 POF/COF'), ('String', 'factorName', '因素名称'),
    ('String', 'unit', '单位'), ('String', 'inputType', '输入类型 number/select'),
    ('String', 'source', '数据来源 CORROSION/GIS/MANUAL'), ('String', 'optionsJson', '选项 JSON [{label,value}]'),
    ('Boolean', 'required', '是否必填'), ('Double', 'minValue', '最小值'), ('Double', 'maxValue', '最大值'),
    ('Integer', 'sortOrder', '排序'), ('Boolean', 'enabled', '是否启用')],
    table='t_risk_factor', id_field='factorCode', swagger=False)

gen('model', 'RiskAssessmentModel', '失效风险评价记录，对应表 t_risk_assessment', [
    ('String', 'assessmentId', '评价ID'), ('Long', 'segmentId', '管段ID'), ('String', 'segmentName', '管段名称'),
    ('Double', 'evaluateYears', '评价年限（内检后年数）'), ('String', 'modelCode', '计算模型编码'),
    ('String', 'status', '状态'), ('Integer', 'pofLevel', '失效可能性等级'), ('Integer', 'cofLevel', '失效后果等级'),
    ('Double', 'pofValue', '失效可能性值'), ('Double', 'cofValue', '失效后果值'), ('String', 'riskLevel', '风险等级'),
    ('String', 'message', '计算说明'), ('String', 'inputsJson', '输入快照 JSON'), ('String', 'operator', '操作人'),
    ('LocalDateTime', 'assessTime', '评价时间')],
    table='t_risk_assessment', id_field='assessmentId', swagger=False)

# ---------- VO ----------
gen('vo', 'FactorOptionVO', '选项', [('String', 'label', '显示名称'), ('String', 'value', '取值')])
gen('vo', 'RiskFactorVO', '风险因素', [
    ('String', 'code', '因素编码'), ('String', 'name', '因素名称'), ('String', 'unit', '单位'),
    ('String', 'inputType', '输入类型 number/select'), ('String', 'source', '数据来源'), ('String', 'sourceName', '数据来源名称'),
    ('List<FactorOptionVO>', 'options', '选项'), ('Boolean', 'required', '是否必填'), ('Double', 'min', '最小值'),
    ('Double', 'max', '最大值')])
gen('vo', 'RiskFactorGroupVO', '风险因素分组', [
    ('String', 'groupCode', '分组编码'), ('String', 'groupName', '分组名称'), ('List<RiskFactorVO>', 'factors', '因素')])
gen('vo', 'MatrixLevelVO', '矩阵等级', [('Integer', 'level', '等级'), ('String', 'name', '名称')])
gen('vo', 'RiskLevelVO', '风险等级', [('String', 'code', '编码'), ('String', 'name', '名称'), ('String', 'color', '颜色')])
gen('vo', 'RiskMatrixVO', '风险矩阵配置', [
    ('List<MatrixLevelVO>', 'pofLevels', '失效可能性等级'), ('List<MatrixLevelVO>', 'cofLevels', '失效后果等级'),
    ('List<RiskLevelVO>', 'riskLevels', '风险等级'), ('List<List<String>>', 'cells', '矩阵单元 cells[pof-1][cof-1]')])
gen('vo', 'RiskSegmentVO', '管段风险概况', [
    ('Long', 'segmentId', '管段ID'), ('String', 'mine', '气矿'), ('String', 'pipelineName', '管线'),
    ('String', 'segmentName', '管段'), ('Double', 'predDepthMm', '预测腐蚀深度(mm)'), ('Double', 'depthRatio', '预测深度比'),
    ('String', 'corrosionLimitFlag', '腐蚀越界标识'), ('Integer', 'pofLevel', '失效可能性等级'),
    ('Integer', 'cofLevel', '失效后果等级'), ('String', 'riskLevel', '风险等级'), ('String', 'riskLevelName', '风险等级名称'),
    ('String', 'status', '状态'), ('String', 'statusName', '状态名称'), ('String', 'lastAssessTime', '最近评价时间')])
gen('vo', 'RiskInputsVO', '风险评价预填输入', [
    ('Long', 'segmentId', '管段ID'), ('Map<String, Object>', 'values', '因素取值'), ('List<String>', 'messages', '预填说明')])
gen('vo', 'AssessmentVO', '失效风险评价结果', [
    ('String', 'assessmentId', '评价ID'), ('Long', 'segmentId', '管段ID'), ('String', 'segmentName', '管段'),
    ('String', 'assessTime', '评价时间'), ('Double', 'evaluateYears', '评价年限'), ('String', 'modelCode', '计算模型'),
    ('String', 'status', '状态'), ('String', 'statusName', '状态名称'), ('Integer', 'pofLevel', '失效可能性等级'),
    ('Integer', 'cofLevel', '失效后果等级'), ('Double', 'pofValue', '失效可能性值'), ('Double', 'cofValue', '失效后果值'),
    ('String', 'riskLevel', '风险等级'), ('String', 'riskLevelName', '风险等级名称'), ('String', 'message', '说明'),
    ('String', 'operator', '操作人'), ('Map<String, Object>', 'inputs', '输入快照')])

# ---------- Feign 远程对象 ----------
gen('feign.dto', 'CorrosionSegmentDTO', '内腐蚀管段汇总（远程）', [
    ('Long', 'segmentId', '管段ID'), ('String', 'mine', '气矿'), ('String', 'pipelineName', '管线'),
    ('String', 'segmentName', '管段'), ('Double', 'wallThicknessMm', '壁厚'), ('Boolean', 'predictable', '是否可预测')],
    swagger=False)
gen('feign.dto', 'PredictSummaryDTO', '管段预测摘要（远程）', [
    ('Long', 'segmentId', '管段ID'), ('String', 'segmentName', '管段'), ('LocalDate', 'targetDate', '目标日期'),
    ('Double', 'predDepthMm', '预测深度'), ('Double', 'depthRatio', '深度比'), ('Double', 'remainingWallMm', '剩余壁厚'),
    ('String', 'limitFlag', '越界标识')], swagger=False)
gen('feign.dto', 'InspectionDTO', '内检记录（远程）', [
    ('LocalDate', 'inspectionDate', '内检日期'), ('Double', 'operatingPressureMpa', '运行压力'),
    ('Double', 'h2sMolPct', 'H2S'), ('Double', 'co2MolPct', 'CO2'), ('String', 'inhibitorDosing', '缓蚀剂投加'),
    ('Integer', 'piggingCycleDays', '清管周期')], swagger=False)
gen('feign.dto', 'GisSegmentDTO', '管段基础数据（远程）', [
    ('Long', 'segmentId', '管段ID'), ('String', 'segmentName', '管段'), ('LocalDate', 'commissionDate', '投运日期'),
    ('Double', 'outerDiameterMm', '外径'), ('Double', 'wallThicknessMm', '壁厚'), ('String', 'materialGrade', '材质'),
    ('Double', 'designPressureMpa', '设计压力'), ('String', 'mediumType', '介质')], swagger=False)

# ---------- 计算上下文 ----------
gen('calculator', 'RiskCalcContextDTO', '风险计算上下文', [
    ('Long', 'segmentId', '管段ID'), ('Double', 'evaluateYears', '评价年限'), ('Map<String, Object>', 'inputs', '因素取值')],
    swagger=False)
gen('calculator', 'RiskCalcResultDTO', '风险计算结果', [
    ('String', 'status', '状态'), ('Double', 'pofValue', '失效可能性值'), ('Double', 'cofValue', '失效后果值'),
    ('Integer', 'pofLevel', '失效可能性等级'), ('Integer', 'cofLevel', '失效后果等级'), ('String', 'riskLevel', '风险等级'),
    ('String', 'message', '说明')], swagger=False)
print('ok')
