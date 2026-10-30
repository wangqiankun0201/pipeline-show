"""由各服务 resources/db 下的脚本生成 PostgreSQL 部署脚本 deploy/sql/*.sql（保证与服务内脚本同源）。

用法：python tools/build_sql.py
"""
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SERVICES = [
    ('01_pims_gis.sql', 'pims-gis', 'pims_gis', '管道位置服务'),
    ('02_pims_corrosion.sql', 'pims-corrosion', 'pims_corrosion', '内腐蚀趋势预测服务'),
    ('03_pims_risk.sql', 'pims-risk', 'pims_risk', '失效风险计算服务'),
]


def read(path):
    with open(path, encoding='utf-8') as fp:
        return fp.read().strip() + '\n'


def main():
    out_dir = os.path.join(ROOT, 'deploy', 'sql')
    os.makedirs(out_dir, exist_ok=True)
    for filename, module, schema, title in SERVICES:
        db_dir = os.path.join(ROOT, 'backend', module, 'src', 'main', 'resources', 'db')
        parts = [
            f'-- {title}（{module}）PostgreSQL 初始化脚本，由 tools/build_sql.py 生成，请勿手工修改\n',
            f'-- 执行用户须对 schema {schema} 具有建表权限\n',
            f'CREATE SCHEMA IF NOT EXISTS {schema};\n',
            f'SET search_path TO {schema};\n\n',
            read(os.path.join(db_dir, 'schema.sql')),
        ]
        data = os.path.join(db_dir, 'data.sql')
        if os.path.exists(data):
            parts += ['\n', read(data)]
        with open(os.path.join(out_dir, filename), 'w', encoding='utf-8', newline='\n') as fp:
            fp.write(''.join(parts))
        print('generated', filename)


if __name__ == '__main__':
    main()
