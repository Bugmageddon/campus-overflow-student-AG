# Q&A 冒烟脚本（PowerShell 7）
# 用途：注册 -> 登录 -> 提问 -> 回答 -> 投票 -> 采纳 全链路
# 运行前：后端需在 http://127.0.0.1:8080 启动，MySQL/Redis 正常
# 运行：pwsh -NoProfile -File http/qa-smoke.ps1

$ErrorActionPreference = 'Stop'

$base = 'http://127.0.0.1:8080'
$session = $null

$suffix = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds().ToString()
$suffix = $suffix.Substring($suffix.Length - 8)
$userA = "smokeA$suffix"
$userB = "smokeB$suffix"
$password = 'SmokePass123'
$csrf = $null

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        $Body = $null
    )

    $headers = @{}
    if ($csrf) { $headers['X-XSRF-TOKEN'] = $csrf }

    $params = @{
        Uri         = "$base$Path"
        Method      = $Method
        WebSession  = $session
        Headers     = $headers
        ErrorAction = 'Stop'
    }

    if ($null -ne $Body) {
        $params['ContentType'] = 'application/json'
        $params['Body'] = ($Body | ConvertTo-Json -Compress -Depth 10)
    }

    Invoke-RestMethod @params
}

function Get-Csrf {
    Invoke-RestMethod -Uri "$base/api/v1/auth/csrf" -WebSession $session | Out-Null
    $cookie = $session.Cookies.GetCookies($base) | Where-Object Name -eq 'XSRF-TOKEN'
    if (-not $cookie) { throw 'no XSRF-TOKEN cookie' }
    $cookie.Value
}

$first = Invoke-RestMethod -Uri "$base/api/v1/auth/csrf" -SessionVariable session
$csrf = ($session.Cookies.GetCookies($base) | Where-Object Name -eq 'XSRF-TOKEN').Value
Write-Host "[0] CSRF ok"

Invoke-Api POST '/api/v1/auth/register' @{
    username    = $userA
    displayName = "冒烟甲$suffix"
    email       = "$userA@example.com"
    password    = $password
    college     = '计算机学院'
    className   = '软工2401'
} | Out-Null
Write-Host "[1] register A ok ($userA)"

Invoke-Api POST '/api/v1/auth/login' @{
    username = $userA
    password = $password
} | Out-Null
$csrf = Get-Csrf
Write-Host "[2] login A ok"

$question = Invoke-Api POST '/api/v1/questions' @{
    courseId = $null
    title    = '冒烟测试：红黑树插入后如何判断旋转方向？'
    body     = '插入节点后出现连续红色节点，什么时候左旋、什么时候右旋？请给出判断步骤。'
    tags     = @('数据结构', '红黑树')
}
$questionId = $question.id
Write-Host "[3] question ok (id=$questionId)"

Invoke-Api POST '/api/v1/auth/logout' | Out-Null
$csrf = Get-Csrf
Write-Host "[4] logout A ok"

Invoke-Api POST '/api/v1/auth/register' @{
    username    = $userB
    displayName = "冒烟乙$suffix"
    email       = "$userB@example.com"
    password    = $password
    college     = '计算机学院'
    className   = '软工2401'
} | Out-Null
Write-Host "[5] register B ok ($userB)"

Invoke-Api POST '/api/v1/auth/login' @{
    username = $userB
    password = $password
} | Out-Null
$csrf = Get-Csrf
Write-Host "[6] login B ok"

$answer = Invoke-Api POST "/api/v1/questions/$questionId/answers" @{
    body = '看叔叔节点颜色和插入位置：叔叔红则变色上移，叔叔黑则按 LL/LR/RR/RL 旋转。'
}
$answerId = $answer.id
Write-Host "[7] answer ok (id=$answerId)"

Invoke-Api POST '/api/v1/auth/logout' | Out-Null
$csrf = Get-Csrf
Write-Host "[8] logout B ok"

Invoke-Api POST '/api/v1/auth/login' @{
    username = $userA
    password = $password
} | Out-Null
$csrf = Get-Csrf
Write-Host "[9] login A again ok"

Invoke-Api PUT '/api/v1/votes' @{
    targetType = 'ANSWER'
    targetId   = $answerId
    value      = 1
} | Out-Null
Write-Host "[10] vote ok"

Invoke-Api PUT "/api/v1/questions/$questionId/accepted-answer" @{
    answerId = $answerId
} | Out-Null
Write-Host "[11] accept ok"

$detail = Invoke-Api GET "/api/v1/questions/$questionId"
if ([int]$detail.acceptedAnswerId -ne [int]$answerId) {
    throw "acceptedAnswerId=$($detail.acceptedAnswerId) but answerId=$answerId"
}

Write-Host "[12] detail ok (acceptedAnswerId=$($detail.acceptedAnswerId))"
Write-Host "SMOKE PASSED questionId=$questionId answerId=$answerId"
