param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { }

$script:falhas = 0
$script:total  = 0
$api = $BaseUrl.TrimEnd("/") + "/pokemon"

function Invoke-Teste {
    param([string]$Metodo, [string]$Url, [string]$Corpo)

    $params = @{
        Uri                = $Url
        Method             = $Metodo
        UseBasicParsing    = $true
        TimeoutSec         = 30
    }
    if ($Corpo) {
        $params.ContentType = "application/json; charset=utf-8"
        $params.Body        = [System.Text.Encoding]::UTF8.GetBytes($Corpo)
    }

    try {
        $r = Invoke-WebRequest @params
        return [pscustomobject]@{
            Status  = [int]$r.StatusCode
            Corpo   = $r.Content
            Headers = $r.Headers
        }
    }
    catch {
        $resp = $_.Exception.Response
        if ($null -eq $resp) {
            return [pscustomobject]@{ Status = 0; Corpo = $_.Exception.Message; Headers = @{} }
        }
        $leitor = New-Object System.IO.StreamReader($resp.GetResponseStream())
        $texto  = $leitor.ReadToEnd()
        return [pscustomobject]@{
            Status  = [int]$resp.StatusCode
            Corpo   = $texto
            Headers = $resp.Headers
        }
    }
}

function Checar {
    param([string]$Nome, [int]$Esperado, $Resultado, [switch]$MostrarCorpo)

    $script:total++
    if ($Resultado.Status -eq $Esperado) {
        Write-Host ("  [OK]    {0,-42} -> {1}" -f $Nome, $Resultado.Status) -ForegroundColor Green
    }
    else {
        $script:falhas++
        Write-Host ("  [FALHA] {0,-42} -> esperado {1}, obtido {2}" -f $Nome, $Esperado, $Resultado.Status) -ForegroundColor Red
    }
    if ($MostrarCorpo -and $Resultado.Corpo) {
        $linha = $Resultado.Corpo
        if ($linha.Length -gt 160) { $linha = $linha.Substring(0, 160) + "..." }
        Write-Host ("          " + $linha) -ForegroundColor DarkGray
    }
}

function Novo-PokemonJson {
    param([string]$Nome, [string]$Tipo, [int]$Nivel, [string]$Data)
    return (@{ nome = $Nome; tipo = $Tipo; nivel = $Nivel; dataCaptura = $Data } | ConvertTo-Json -Compress)
}

Write-Host ""
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host " POKEDEX API - teste dos endpoints" -ForegroundColor Cyan
Write-Host " API: $api" -ForegroundColor Cyan
Write-Host "=============================================================" -ForegroundColor Cyan

$sufixo = Get-Random -Minimum 1000 -Maximum 9999
$nomeTeste = "TesteMon$sufixo"

Write-Host "`n[1] GET /pokemon (findAll)" -ForegroundColor Yellow
$r = Invoke-Teste "GET" $api
Checar "GET /pokemon" 200 $r -MostrarCorpo

Write-Host "`n[2] GET /pokemon/{codigo} (findByCodigo)" -ForegroundColor Yellow
$r = Invoke-Teste "GET" "$api/1"
Checar "GET /pokemon/1" 200 $r -MostrarCorpo

$r = Invoke-Teste "GET" "$api/999999"
Checar "GET /pokemon/999999 (nao existe)" 404 $r

Write-Host "`n[3] POST /pokemon (save)" -ForegroundColor Yellow
$corpo = Novo-PokemonJson $nomeTeste "Fogo" 25 "2025-03-15"
$r = Invoke-Teste "POST" $api $corpo
Checar "POST /pokemon (valido)" 201 $r -MostrarCorpo

$novoCodigo = $null
if ($r.Status -eq 201 -and $r.Corpo) {
    try { $novoCodigo = ($r.Corpo | ConvertFrom-Json).codigo } catch { }
}
Write-Host ("          codigo criado: " + $novoCodigo) -ForegroundColor DarkGray

$corpo = Novo-PokemonJson "FuturoMon$sufixo" "Agua" 10 "2099-01-01"
$r = Invoke-Teste "POST" $api $corpo
Checar "POST /pokemon (data futura)" 400 $r -MostrarCorpo

$corpo = Novo-PokemonJson "NivelMon$sufixo" "Agua" 150 "2025-01-01"
$r = Invoke-Teste "POST" $api $corpo
Checar "POST /pokemon (nivel 150)" 400 $r -MostrarCorpo

$corpo = Novo-PokemonJson $nomeTeste "Agua" 10 "2025-01-01"
$r = Invoke-Teste "POST" $api $corpo
Checar "POST /pokemon (nome repetido)" 400 $r -MostrarCorpo

$corpo = '{"nome":"","tipo":"Fogo","nivel":10,"dataCaptura":"2025-01-01"}'
$r = Invoke-Teste "POST" $api $corpo
Checar "POST /pokemon (nome vazio)" 400 $r -MostrarCorpo

Write-Host "`n[4] PUT /pokemon/{codigo} (update)" -ForegroundColor Yellow
if ($novoCodigo) {
    $corpo = Novo-PokemonJson ($nomeTeste + "X") "Fogo" 30 "2025-03-16"
    $r = Invoke-Teste "PUT" "$api/$novoCodigo" $corpo
    Checar "PUT /pokemon/$novoCodigo (valido)" 201 $r -MostrarCorpo
}

$corpo = Novo-PokemonJson "Inexistente$sufixo" "Fogo" 30 "2025-03-16"
$r = Invoke-Teste "PUT" "$api/999999" $corpo
Checar "PUT /pokemon/999999 (nao existe)" 404 $r

Write-Host "`n[5] DELETE /pokemon/{codigo} (delete)" -ForegroundColor Yellow
if ($novoCodigo) {
    $r = Invoke-Teste "DELETE" "$api/$novoCodigo"
    Checar "DELETE /pokemon/$novoCodigo (valido)" 204 $r

    $r = Invoke-Teste "DELETE" "$api/$novoCodigo"
    Checar "DELETE /pokemon/$novoCodigo (ja apagado)" 404 $r
}

Write-Host "`n[6] Erros de contrato e CORS" -ForegroundColor Yellow
$r = Invoke-Teste "GET" "$api/abc"
Checar "GET /pokemon/abc (codigo invalido)" 400 $r -MostrarCorpo

$r = Invoke-Teste "GET" ($BaseUrl.TrimEnd("/") + "/pokemons")
Checar "GET /pokemons (rota inexistente)" 404 $r -MostrarCorpo

$r = Invoke-Teste "OPTIONS" $api
Checar "OPTIONS /pokemon (preflight CORS)" 200 $r

$cors = $r.Headers["Access-Control-Allow-Origin"]
if ($cors) {
    Write-Host ("  [OK]    cabecalho CORS Access-Control-Allow-Origin = " + $cors) -ForegroundColor Green
}
else {
    Write-Host "  [FALHA] cabecalho CORS Access-Control-Allow-Origin ausente" -ForegroundColor Red
    $script:falhas++
}
$script:total++

Write-Host ""
Write-Host "=============================================================" -ForegroundColor Cyan
if ($script:falhas -eq 0) {
    Write-Host (" RESULTADO: $($script:total) testes, TODOS PASSARAM") -ForegroundColor Green
    Write-Host "=============================================================" -ForegroundColor Cyan
    exit 0
}
else {
    Write-Host (" RESULTADO: $($script:total) testes, $($script:falhas) FALHARAM") -ForegroundColor Red
    Write-Host "=============================================================" -ForegroundColor Cyan
    exit 1
}
