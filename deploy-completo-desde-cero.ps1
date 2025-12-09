# Script completo para desplegar desde cero con configuración correcta de puertos gRPC
# Este script configura TODO: Resource Group, Environment, ACR, Identity, y todos los servicios

param(
    [string]$Location = "canadacentral",
    [string]$ResourceGroup = "combustibleSKT-rg",
    [string]$Environment = "combustible-env",
    [string]$ACR = "combustibleacr1935",
    [string]$IdentityName = "combustible-identity"
)

# MongoDB URI y JWT Secret
$MONGO_URI = 'mongodb+srv://907johan_db_user:piIe4vWfuADsnRM6@combustibleskt.4n4nf9z.mongodb.net/drivers_db?retryWrites=true&w=majority&readPreference=secondaryPreferred&maxPoolSize=50&minPoolSize=10&appName=combustibleskt'
$JWT_SECRET = "mySecretKey123456789012345678901234567890123456789012345678901234567890"

Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "        🚀 DESPLIEGUE COMPLETO DESDE CERO" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "`n📍 Location: $Location" -ForegroundColor Yellow
Write-Host "📦 Resource Group: $ResourceGroup" -ForegroundColor Yellow
Write-Host "🌐 Environment: $Environment" -ForegroundColor Yellow
Write-Host "🐳 ACR: $ACR" -ForegroundColor Yellow

# PASO 1: Crear Resource Group
Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PASO 1: Creando Resource Group" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

$rgExists = az group show --name $ResourceGroup --query "name" -o tsv 2>$null
if ($rgExists) {
    Write-Host "✅ Resource Group ya existe: $ResourceGroup" -ForegroundColor Green
} else {
    Write-Host "Creando Resource Group..." -ForegroundColor Yellow
    az group create --name $ResourceGroup --location $Location --only-show-errors | Out-Null
    Write-Host "✅ Resource Group creado" -ForegroundColor Green
}

# PASO 2: Registrar providers
Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PASO 2: Registrando Resource Providers" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

$providers = @("Microsoft.ContainerRegistry", "Microsoft.App", "Microsoft.OperationalInsights")
foreach ($provider in $providers) {
    Write-Host "Registrando $provider..." -ForegroundColor Yellow
    az provider register --namespace $provider --wait --only-show-errors | Out-Null
}
Write-Host "✅ Providers registrados" -ForegroundColor Green

# PASO 3: Crear Azure Container Registry
Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PASO 3: Creando Azure Container Registry" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

$acrExists = az acr show --name $ACR --resource-group $ResourceGroup --query "name" -o tsv 2>$null
if ($acrExists) {
    Write-Host "✅ ACR ya existe: $ACR" -ForegroundColor Green
} else {
    Write-Host "Creando ACR..." -ForegroundColor Yellow
    az acr create --name $ACR --resource-group $ResourceGroup --sku Basic --admin-enabled true --only-show-errors | Out-Null
    Write-Host "✅ ACR creado" -ForegroundColor Green
}

# PASO 4: Crear Managed Identity
Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PASO 4: Creando Managed Identity" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

$identityExists = az identity show --name $IdentityName --resource-group $ResourceGroup --query "name" -o tsv 2>$null
if ($identityExists) {
    Write-Host "✅ Identity ya existe: $IdentityName" -ForegroundColor Green
} else {
    Write-Host "Creando Managed Identity..." -ForegroundColor Yellow
    az identity create --name $IdentityName --resource-group $ResourceGroup --location $Location --only-show-errors | Out-Null
    Write-Host "✅ Identity creada" -ForegroundColor Green
}

$IDENTITY_ID = az identity show --name $IdentityName --resource-group $ResourceGroup --query id -o tsv
$IDENTITY_PRINCIPAL_ID = az identity show --name $IdentityName --resource-group $ResourceGroup --query principalId -o tsv

# Asignar rol AcrPull a la Identity
Write-Host "Asignando rol AcrPull a la Identity..." -ForegroundColor Yellow
az role assignment create `
    --assignee $IDENTITY_PRINCIPAL_ID `
    --role AcrPull `
    --scope "/subscriptions/$(az account show --query id -o tsv)/resourceGroups/$ResourceGroup/providers/Microsoft.ContainerRegistry/registries/$ACR" `
    --only-show-errors | Out-Null
Write-Host "✅ Rol asignado" -ForegroundColor Green

# PASO 5: Crear Log Analytics Workspace
Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PASO 5: Creando Log Analytics Workspace" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

$workspaceName = "combustible-logs"
$workspaceExists = az monitor log-analytics workspace show --resource-group $ResourceGroup --workspace-name $workspaceName --query "name" -o tsv 2>$null
if ($workspaceExists) {
    Write-Host "✅ Workspace ya existe" -ForegroundColor Green
} else {
    Write-Host "Creando Log Analytics Workspace..." -ForegroundColor Yellow
    az monitor log-analytics workspace create `
        --resource-group $ResourceGroup `
        --workspace-name $workspaceName `
        --location $Location `
        --only-show-errors | Out-Null
    Write-Host "✅ Workspace creado" -ForegroundColor Green
}

$WORKSPACE_ID = az monitor log-analytics workspace show --resource-group $ResourceGroup --workspace-name $workspaceName --query customerId -o tsv
$WORKSPACE_KEY = az monitor log-analytics workspace get-shared-keys --resource-group $ResourceGroup --workspace-name $workspaceName --query primarySharedKey -o tsv

# PASO 6: Crear Container Apps Environment
Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PASO 6: Creando Container Apps Environment" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

$envExists = az containerapp env show --name $Environment --resource-group $ResourceGroup --query "name" -o tsv 2>$null
if ($envExists) {
    Write-Host "✅ Environment ya existe: $Environment" -ForegroundColor Green
} else {
    Write-Host "Creando Container Apps Environment..." -ForegroundColor Yellow
    az containerapp env create `
        --name $Environment `
        --resource-group $ResourceGroup `
        --location $Location `
        --logs-workspace-id $WORKSPACE_ID `
        --logs-workspace-key $WORKSPACE_KEY `
        --only-show-errors | Out-Null
    Write-Host "✅ Environment creado" -ForegroundColor Green
}

$ENV_ID = "/subscriptions/$(az account show --query id -o tsv)/resourceGroups/$ResourceGroup/providers/Microsoft.App/managedEnvironments/$Environment"

# PASO 7: Autenticar en ACR
Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PASO 7: Autenticando en ACR" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

az acr login --name $ACR --only-show-errors | Out-Null
Write-Host "✅ Autenticado en ACR" -ForegroundColor Green

# PASO 8: Construir y subir imágenes (RECONSTRUCCIÓN NECESARIA por cambios en puertos gRPC)
Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PASO 8: Construyendo y subiendo imágenes Docker" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "⚠️  IMPORTANTE: Reconstruyendo imágenes con los nuevos cambios" -ForegroundColor Yellow
Write-Host "   (gRPC ahora usa el mismo puerto que HTTP)" -ForegroundColor Yellow

$services = @(
    @{Name="auth-service"; Dockerfile="src/auth-service/Dockerfile"; Port=8085},
    @{Name="drivers-service"; Dockerfile="src/drivers-service/Dockerfile"; Port=8081},
    @{Name="vehicles-service"; Dockerfile="src/vehicles-service/Dockerfile"; Port=8082},
    @{Name="routes-service"; Dockerfile="src/routes-service/Dockerfile"; Port=8083},
    @{Name="fuel-service"; Dockerfile="src/fuel-service/Dockerfile"; Port=8084},
    @{Name="gateway-service"; Dockerfile="src/gateway-service/Dockerfile"; Port=8090}
)

foreach ($svc in $services) {
    Write-Host "`n📦 $($svc.Name)..." -ForegroundColor Yellow
    Write-Host '  Construyendo imagen (reconstruccion completa)...' -ForegroundColor Gray
    docker build --no-cache -f $svc.Dockerfile -t "$ACR.azurecr.io/$($svc.Name):1.0" . 2>&1 | Select-String -Pattern 'ERROR|Successfully|Step' | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  ❌ Error construyendo $($svc.Name)" -ForegroundColor Red
        continue
    }
    
    Write-Host "  Subiendo a ACR..." -ForegroundColor Gray
    docker push "$ACR.azurecr.io/$($svc.Name):1.0" 2>&1 | Select-String -Pattern 'ERROR|Pushed|digest|already exists' | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ $($svc.Name) construido y subido correctamente" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  Error subiendo $($svc.Name)" -ForegroundColor Yellow
    }
}

# PASO 9: Desplegar servicios con puertos configurados
Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PASO 9: Desplegando servicios con puertos gRPC" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

function Deploy-ServiceWithPorts {
    param(
        [string]$ServiceName,
        [string]$Image,
        [int]$HttpPort,
        [hashtable]$EnvVars,
        [bool]$ExternalIngress = $true
    )
    
    $msg = 'Desplegando ' + $ServiceName + ' en puerto ' + $HttpPort
    Write-Host "`n🚀 $msg..." -ForegroundColor Yellow
    
    # Crear YAML usando StringBuilder para evitar problemas de parsing
    $yamlLines = @()
    $yamlLines += "location: $Location"
    $yamlLines += "properties:"
    $yamlLines += "  managedEnvironmentId: $ENV_ID"
    $yamlLines += "  configuration:"
    $yamlLines += "    ingress:"
    $externalValue = if ($ExternalIngress) { "true" } else { "false" }
    $yamlLines += "      external: $externalValue"
    $yamlLines += "      targetPort: $HttpPort"
    $yamlLines += "      transport: auto"
    $yamlLines += "      allowInsecure: true"
    $yamlLines += "    secrets:"
    $yamlLines += "      - name: mongo-uri"
    $yamlLines += "        value: '$($MONGO_URI -replace "'", "''")'"
    $yamlLines += "      - name: jwt-secret"
    $yamlLines += "        value: '$($JWT_SECRET -replace "'", "''")'"
    $yamlLines += "    registries:"
    $yamlLines += "      - server: $ACR.azurecr.io"
    $yamlLines += "        identity: $IDENTITY_ID"
    $yamlLines += "  template:"
    $yamlLines += "    containers:"
    $yamlLines += "      - name: $ServiceName"
    $yamlLines += "        image: $Image"
    $yamlLines += "        env:"
    $yamlLines += "          - name: SPRING_PROFILES_ACTIVE"
    $yamlLines += "            value: atlas"
    $yamlLines += "          - name: MONGODB_ATLAS_URI"
    $yamlLines += "            secretRef: mongo-uri"
    $yamlLines += "          - name: JWT_SECRET"
    $yamlLines += "            secretRef: jwt-secret"
    
    # Agregar variables de entorno adicionales
    foreach ($key in $EnvVars.Keys) {
        $value = $EnvVars[$key]
        $yamlLines += "          - name: $key"
        $yamlLines += "            value: `"$value`""
    }
    
    $yamlLines += "        ports:"
    $yamlLines += "          - containerPort: $HttpPort"
    $yamlLines += "            protocol: tcp"
    $yamlLines += "    scale:"
    $yamlLines += "      minReplicas: 1"
    $yamlLines += "      maxReplicas: 2"
    $yamlLines += "  identity:"
    $yamlLines += "    type: UserAssigned"
    $yamlLines += "    userAssignedIdentities:"
    $yamlLines += "      `"$IDENTITY_ID`": {}"
    
    $yaml = $yamlLines -join "`r`n"
    
    $yamlFile = [System.IO.Path]::GetTempFileName() + ".yaml"
    $yaml | Set-Content $yamlFile -Encoding UTF8
    
    # Debug: Verificar que el archivo YAML se creó correctamente
    if (-not (Test-Path $yamlFile)) {
        Write-Host "  ❌ Error: No se pudo crear el archivo YAML" -ForegroundColor Red
        return
    }
    
    # Crear Container App
    Write-Host '  Creando Container App...' -ForegroundColor Gray
    $createResult = az containerapp create `
        --name $ServiceName `
        --resource-group $ResourceGroup `
        --yaml $yamlFile `
        --only-show-errors 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ $ServiceName desplegado" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  Error al desplegar $ServiceName" -ForegroundColor Red
        if ($createResult) {
            Write-Host "  Detalles: $createResult" -ForegroundColor Yellow
        }
    }
    
    Remove-Item $yamlFile -ErrorAction SilentlyContinue
}

# Desplegar cada servicio (HTTP y gRPC comparten el mismo puerto)
# AUTH SERVICE - Solo interno
Deploy-ServiceWithPorts -ServiceName "auth-service" `
    -Image "$ACR.azurecr.io/auth-service:1.0" `
    -HttpPort 8085 `
    -EnvVars @{} `
    -ExternalIngress $false

# DRIVERS SERVICE - Solo interno
Deploy-ServiceWithPorts -ServiceName "drivers-service" `
    -Image "$ACR.azurecr.io/drivers-service:1.0" `
    -HttpPort 8081 `
    -EnvVars @{
        "GRPC_AUTH_HOST" = "auth-service"
        "GRPC_AUTH_PORT" = "8085"
    } `
    -ExternalIngress $false

# VEHICLES SERVICE - Solo interno
Deploy-ServiceWithPorts -ServiceName "vehicles-service" `
    -Image "$ACR.azurecr.io/vehicles-service:1.0" `
    -HttpPort 8082 `
    -EnvVars @{
        "GRPC_DRIVERS_HOST" = "drivers-service"
        "GRPC_DRIVERS_PORT" = "8081"
    } `
    -ExternalIngress $false

# ROUTES SERVICE - Solo interno
Deploy-ServiceWithPorts -ServiceName "routes-service" `
    -Image "$ACR.azurecr.io/routes-service:1.0" `
    -HttpPort 8083 `
    -EnvVars @{
        "GRPC_DRIVERS_HOST" = "drivers-service"
        "GRPC_DRIVERS_PORT" = "8081"
        "GRPC_VEHICLES_HOST" = "vehicles-service"
        "GRPC_VEHICLES_PORT" = "8082"
    } `
    -ExternalIngress $false

# FUEL SERVICE - Solo interno
Deploy-ServiceWithPorts -ServiceName "fuel-service" `
    -Image "$ACR.azurecr.io/fuel-service:1.0" `
    -HttpPort 8084 `
    -EnvVars @{
        "GRPC_ROUTES_HOST" = "routes-service"
        "GRPC_ROUTES_PORT" = "8083"
        "GRPC_VEHICLES_HOST" = "vehicles-service"
        "GRPC_VEHICLES_PORT" = "8082"
    } `
    -ExternalIngress $false

# GATEWAY SERVICE - EXTERNO (único servicio expuesto)
Deploy-ServiceWithPorts -ServiceName "gateway-service" `
    -Image "$ACR.azurecr.io/gateway-service:1.0" `
    -HttpPort 8090 `
    -EnvVars @{
        "GRPC_DRIVERS_HOST" = "drivers-service"
        "GRPC_DRIVERS_PORT" = "8081"
        "GRPC_VEHICLES_HOST" = "vehicles-service"
        "GRPC_VEHICLES_PORT" = "8082"
        "GRPC_ROUTES_HOST" = "routes-service"
        "GRPC_ROUTES_PORT" = "8083"
        "GRPC_FUEL_HOST" = "fuel-service"
        "GRPC_FUEL_PORT" = "8084"
        "GRPC_AUTH_HOST" = "auth-service"
        "GRPC_AUTH_PORT" = "8085"
    } `
    -ExternalIngress $true

# PASO 10: Verificar despliegue
Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PASO 10: Verificando despliegue" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

Write-Host "`nEsperando 60 segundos para que los servicios se inicien..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

Write-Host "`n=== ESTADO DE LOS SERVICIOS ===" -ForegroundColor Cyan
az containerapp list --resource-group $ResourceGroup --query "[].{Name:name, Status:properties.runningStatus, Revision:properties.latestRevisionName}" -o table

Write-Host "`n=== VERIFICANDO PUERTOS CONFIGURADOS ===" -ForegroundColor Cyan
foreach ($svc in @("drivers-service", "vehicles-service", "routes-service", "fuel-service", "auth-service", "gateway-service")) {
    Write-Host "`n$svc :" -ForegroundColor Yellow
    $targetPort = az containerapp show --name $svc --resource-group $ResourceGroup --query "properties.configuration.ingress.targetPort" -o tsv 2>$null
    if ($targetPort) {
        $portInfo = "Puerto configurado: $targetPort (HTTP y gRPC)"
        Write-Host "  ✅ $portInfo" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  No hay puerto configurado" -ForegroundColor Red
    }
}

Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "        ✅ DESPLIEGUE COMPLETADO" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "`n🎯 PRÓXIMOS PASOS:" -ForegroundColor Yellow
Write-Host "1. Verifica que los puertos estén configurados arriba" -ForegroundColor White
Write-Host "2. Si faltan puertos, configúralos manualmente en Azure Portal" -ForegroundColor White
Write-Host "3. Prueba la asignación de vehículo a chofer" -ForegroundColor White
Write-Host "`n📝 URLs de los servicios:" -ForegroundColor Cyan
$services = @("auth-service", "drivers-service", "vehicles-service", "routes-service", "fuel-service", "gateway-service")
foreach ($svc in $services) {
    $fqdn = az containerapp show --name $svc --resource-group $ResourceGroup --query "properties.configuration.ingress.fqdn" -o tsv 2>$null
    if ($fqdn) {
        Write-Host "  $svc : https://$fqdn" -ForegroundColor White
    }
}

