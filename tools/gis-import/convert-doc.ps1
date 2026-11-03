$ErrorActionPreference = 'Stop'
$src = 'E:\desktop\gis传送\真实gis数据'
$out = 'E:\desktop\gis传送\pipeline-platform\tools\gis-import\work'
New-Item -ItemType Directory -Force -Path $out | Out-Null

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
try {
    Get-ChildItem -Path $src -Filter *.doc | ForEach-Object {
        $target = Join-Path $out ($_.BaseName + '.docx')
        $doc = $word.Documents.Open($_.FullName, $false, $true)
        try {
            $doc.SaveAs2($target, 16)
        } finally {
            $doc.Close(0)
        }
        Write-Output ($_.Name + ' -> ' + $target)
    }
} finally {
    $word.Quit()
}
