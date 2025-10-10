// Configuración de la API
const API_BASE_URL = 'http://localhost:8082/api/v1/vehicles';

// Variables globales
let vehicles = [];
let authToken = null;
let currentUser = null;
let currentFilter = {
    search: '',
    type: '',
    status: ''
};

// Elementos del DOM
let dashboardSection, vehiclesSection, vehicleFormSection;
let dashboardBtn, vehiclesBtn, addVehicleBtn, logoutBtn;

// Inicialización de la aplicación
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
});

function initializeApp() {
    // Obtener elementos del DOM
    dashboardSection = document.getElementById('dashboardSection');
    vehiclesSection = document.getElementById('vehiclesSection');
    vehicleFormSection = document.getElementById('vehicleFormSection');
    
    dashboardBtn = document.getElementById('dashboardBtn');
    vehiclesBtn = document.getElementById('vehiclesBtn');
    addVehicleBtn = document.getElementById('addVehicleBtn');
    logoutBtn = document.getElementById('logoutBtn');
    
    // Verificar autenticación de forma simplificada
    checkAuthenticationSimple();
}

function checkAuthenticationSimple() {
    console.log('🔍 Verificando autenticación (modo simple)...');
    
    // Primero verificar si hay datos en la URL (venimos del Auth Service)
    const urlParams = new URLSearchParams(window.location.search);
    const tokenFromUrl = urlParams.get('token');
    const userFromUrl = urlParams.get('user');
    
    if (tokenFromUrl && userFromUrl) {
        console.log('📥 Datos recibidos desde Auth Service');
        
        try {
            // Guardar en localStorage para futuras cargas
            localStorage.setItem('authToken', tokenFromUrl);
            localStorage.setItem('currentUser', userFromUrl);
            
            // Parsear y asignar datos
            authToken = tokenFromUrl;
            currentUser = JSON.parse(userFromUrl);
            
            console.log('✅ Datos guardados en localStorage');
            
            // Limpiar URL para que no se vean los parámetros
            window.history.replaceState({}, document.title, window.location.pathname);
            
            // Continuar con la autenticación
            proceedWithAuthentication();
            return;
            
        } catch (error) {
            console.error('❌ Error procesando datos de URL:', error);
            redirectToLogin();
            return;
        }
    }
    
    // Si no hay datos en URL, intentar cargar del localStorage
    const savedToken = localStorage.getItem('authToken');
    const savedUser = localStorage.getItem('currentUser');
    
    console.log('📝 Token en localStorage:', savedToken ? 'Sí' : 'No');
    console.log('👤 Usuario en localStorage:', savedUser ? 'Sí' : 'No');
    
    if (!savedToken || !savedUser) {
        console.log('❌ No hay datos de autenticación');
        showMessage('Debes iniciar sesión para acceder a este módulo', 'error');
        setTimeout(() => {
            window.location.href = 'http://localhost:8085/';
        }, 2000);
        return;
    }
    
    // Parsear datos del localStorage
    authToken = savedToken;
    currentUser = JSON.parse(savedUser);
    
    // Continuar con la autenticación
    proceedWithAuthentication();
}

function proceedWithAuthentication() {
    try {
        console.log('✅ Datos cargados:', {
            username: currentUser.username,
            tokenLength: authToken.length,
            tokenStart: authToken.substring(0, 20) + '...'
        });
        
        // Verificación básica del token JWT
        if (authToken.startsWith('eyJ') && currentUser && currentUser.username) {
            console.log('✅ Token JWT válido y usuario encontrado');
            console.log('🎉 Usuario autenticado:', currentUser.username);
            
            updateUserInfo();
            showDashboard();
            loadDashboardData();
        } else {
            console.log('❌ Token o usuario inválido');
            redirectToLogin();
        }
        
    } catch (error) {
        console.error('❌ Error procesando datos de autenticación:', error);
        redirectToLogin();
    }
}

function updateUserInfo() {
    if (currentUser) {
        console.log(`👤 Usuario autenticado: ${currentUser.nombre} (${currentUser.rol})`);
        
        const welcomeTitle = document.getElementById('welcomeTitle');
        const userInfo = document.getElementById('userInfo');
        
        if (welcomeTitle) {
            welcomeTitle.textContent = `¡Bienvenido, ${currentUser.nombre}!`;
        }
        
        if (userInfo) {
            userInfo.textContent = `${currentUser.nombre} ${currentUser.apellido} - ${currentUser.rol}`;
        }
    }
}

function redirectToLogin() {
    showMessage('Sesión inválida. Redirigiendo al login...', 'error');
    setTimeout(() => {
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
        window.location.href = 'http://localhost:8085/';
    }, 2000);
}

function setupEventListeners() {
    // Event listeners para navegación
    dashboardBtn.addEventListener('click', showDashboard);
    vehiclesBtn.addEventListener('click', showVehiclesList);
    addVehicleBtn.addEventListener('click', showAddVehicle);
    logoutBtn.addEventListener('click', logout);
    
    // Event listener para el formulario
    document.getElementById('vehicleForm').addEventListener('submit', handleFormSubmit);
    
    // Event listeners para filtros
    document.getElementById('searchInput').addEventListener('input', handleSearch);
    document.getElementById('filterType').addEventListener('change', handleFilterType);
    document.getElementById('filterStatus').addEventListener('change', handleFilterStatus);
}

// Funciones de navegación
function showDashboard() {
    hideAllSections();
    dashboardSection.classList.add('active');
    updateNavButtons('dashboard');
    loadDashboardData();
}

function showVehiclesList() {
    hideAllSections();
    vehiclesSection.classList.add('active');
    updateNavButtons('vehicles');
    loadVehicles();
}

function showAddVehicle() {
    hideAllSections();
    vehicleFormSection.classList.add('active');
    updateNavButtons('add');
    clearForm();
    document.getElementById('formTitle').textContent = 'Nuevo Vehículo';
}

function showEditVehicle(vehicle) {
    hideAllSections();
    vehicleFormSection.classList.add('active');
    updateNavButtons('add');
    fillForm(vehicle);
    document.getElementById('formTitle').textContent = 'Editar Vehículo';
}

function hideAllSections() {
    dashboardSection.classList.remove('active');
    vehiclesSection.classList.remove('active');
    vehicleFormSection.classList.remove('active');
}

function updateNavButtons(activeSection) {
    dashboardBtn.classList.remove('active');
    vehiclesBtn.classList.remove('active');
    addVehicleBtn.classList.remove('active');
    
    if (activeSection === 'dashboard') {
        dashboardBtn.classList.add('active');
    } else if (activeSection === 'vehicles') {
        vehiclesBtn.classList.add('active');
    } else if (activeSection === 'add') {
        addVehicleBtn.classList.add('active');
    }
}

function logout() {
    if (confirm('¿Estás seguro de que deseas salir?')) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
        authToken = null;
        currentUser = null;
        
        showMessage('Sesión cerrada correctamente', 'info');
        setTimeout(() => {
            window.location.href = 'http://localhost:8085/';
        }, 1000);
    }
}

function goBackToMainDashboard() {
    showMessage('Volviendo al dashboard principal...', 'info');
    setTimeout(() => {
        window.location.href = 'http://localhost:8085/';
    }, 500);
}

// Funciones de carga de datos
async function loadDashboardData() {
    try {
        await loadVehicles();
        updateDashboardStats();
        updateMachineryTypeCount();
    } catch (error) {
        console.error('Error cargando datos del dashboard:', error);
        showMessage('Error cargando datos del dashboard', 'error');
    }
}

async function loadVehicles() {
    try {
        console.log('📡 Cargando vehículos...');
        
        const response = await fetch(`${API_BASE_URL}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        console.log('📊 Respuesta:', response.status, response.statusText);
        
        if (response.ok) {
            vehicles = await response.json();
            console.log('✅ Vehículos cargados:', vehicles.length);
            renderVehicles();
            updateDashboardStats();
        } else if (response.status === 401) {
            console.log('❌ Error 401: No autorizado');
            showMessage('Sesión expirada. Redirigiendo al login...', 'error');
            setTimeout(() => redirectToLogin(), 2000);
        } else {
            throw new Error('Error al cargar vehículos');
        }
    } catch (error) {
        console.error('Error cargando vehículos:', error);
        showMessage('Error al cargar la lista de vehículos', 'error');
        vehicles = [];
    }
}

function updateDashboardStats() {
    const total = vehicles.length;
    const available = vehicles.filter(v => v.estadoOperativo === 'DISPONIBLE').length;
    const maintenance = vehicles.filter(v => v.estadoOperativo === 'MANTENIMIENTO').length;
    const inUse = vehicles.filter(v => v.estadoOperativo === 'EN_USO').length;
    
    document.getElementById('totalVehicles').textContent = total;
    document.getElementById('availableVehicles').textContent = available;
    document.getElementById('maintenanceVehicles').textContent = maintenance;
    document.getElementById('inUseVehicles').textContent = inUse;
}

function updateMachineryTypeCount() {
    const types = ['CAMION', 'VOLQUETE', 'EXCAVADORA', 'CARGADOR', 'GRUA', 'MOTONIVELADORA'];
    
    types.forEach(type => {
        const count = vehicles.filter(v => v.tipoMaquinaria === type).length;
        const element = document.getElementById(`count-${type}`);
        if (element) {
            element.textContent = `${count} unidad${count !== 1 ? 'es' : ''}`;
        }
    });
}

function renderVehicles() {
    const grid = document.getElementById('vehiclesGrid');
    
    if (!vehicles || vehicles.length === 0) {
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 40px; color: #6c757d;">
                <i class="fas fa-truck" style="font-size: 3rem; margin-bottom: 20px; opacity: 0.3;"></i>
                <p style="font-size: 1.2rem;">No hay vehículos registrados</p>
                <button class="btn btn-primary" onclick="showAddVehicle()" style="margin-top: 20px;">
                    <i class="fas fa-plus"></i> Agregar Primer Vehículo
                </button>
            </div>
        `;
        return;
    }
    
    // Aplicar filtros
    const filteredVehicles = vehicles.filter(vehicle => {
        const matchSearch = !currentFilter.search || 
            vehicle.placa.toLowerCase().includes(currentFilter.search.toLowerCase()) ||
            vehicle.marca.toLowerCase().includes(currentFilter.search.toLowerCase()) ||
            vehicle.modelo.toLowerCase().includes(currentFilter.search.toLowerCase());
        
        const matchType = !currentFilter.type || vehicle.tipoMaquinaria === currentFilter.type;
        const matchStatus = !currentFilter.status || vehicle.estadoOperativo === currentFilter.status;
        
        return matchSearch && matchType && matchStatus;
    });
    
    if (filteredVehicles.length === 0) {
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 40px; color: #6c757d;">
                <i class="fas fa-search" style="font-size: 3rem; margin-bottom: 20px; opacity: 0.3;"></i>
                <p style="font-size: 1.2rem;">No se encontraron vehículos con los filtros aplicados</p>
            </div>
        `;
        return;
    }
    
    grid.innerHTML = filteredVehicles.map(vehicle => createVehicleCard(vehicle)).join('');
}

function createVehicleCard(vehicle) {
    const statusClass = `status-${vehicle.estadoOperativo.toLowerCase()}`;
    const statusText = getStatusText(vehicle.estadoOperativo);
    const typeIcon = getTypeIcon(vehicle.tipoMaquinaria);
    
    return `
        <div class="vehicle-card">
            <div class="vehicle-card-header">
                <div class="vehicle-placa">
                    <i class="${typeIcon}"></i>
                    ${vehicle.placa}
                </div>
                <span class="vehicle-status ${statusClass}">${statusText}</span>
            </div>
            
            <div class="vehicle-info">
                <div class="info-row">
                    <span class="info-label"><i class="fas fa-industry"></i> Marca:</span>
                    <span class="info-value">${vehicle.marca}</span>
                </div>
                <div class="info-row">
                    <span class="info-label"><i class="fas fa-tag"></i> Modelo:</span>
                    <span class="info-value">${vehicle.modelo}</span>
                </div>
                <div class="info-row">
                    <span class="info-label"><i class="fas fa-calendar"></i> Año:</span>
                    <span class="info-value">${vehicle.anio}</span>
                </div>
                <div class="info-row">
                    <span class="info-label"><i class="fas fa-truck"></i> Tipo:</span>
                    <span class="info-value">${getTipoMaquinariaText(vehicle.tipoMaquinaria)}</span>
                </div>
                ${vehicle.kilometrajeActual ? `
                <div class="info-row">
                    <span class="info-label"><i class="fas fa-road"></i> Kilometraje:</span>
                    <span class="info-value">${formatNumber(vehicle.kilometrajeActual)} km</span>
                </div>
                ` : ''}
                ${vehicle.capacidadTanque ? `
                <div class="info-row">
                    <span class="info-label"><i class="fas fa-gas-pump"></i> Tanque:</span>
                    <span class="info-value">${formatNumber(vehicle.capacidadTanque)} L</span>
                </div>
                ` : ''}
            </div>
            
            <div class="vehicle-actions">
                <button class="btn btn-info btn-sm" onclick='viewVehicle(${JSON.stringify(vehicle).replace(/'/g, "&apos;")})'>
                    <i class="fas fa-eye"></i> Ver
                </button>
                <button class="btn btn-primary btn-sm" onclick='editVehicle(${JSON.stringify(vehicle).replace(/'/g, "&apos;")})'>
                    <i class="fas fa-edit"></i> Editar
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteVehicle('${vehicle.id}', '${vehicle.placa}')">
                    <i class="fas fa-trash"></i> Eliminar
                </button>
            </div>
        </div>
    `;
}

function viewVehicle(vehicle) {
    const modal = document.getElementById('vehicleModal');
    const details = document.getElementById('vehicleDetails');
    
    details.innerHTML = `
        <div style="display: grid; gap: 15px;">
            <div style="padding: 15px; background: #f8f9fa; border-radius: 10px;">
                <h3 style="margin-bottom: 15px; color: #1a1a2e;">Información General</h3>
                <div style="display: grid; gap: 10px;">
                    <p><strong>Placa:</strong> ${vehicle.placa}</p>
                    <p><strong>Marca:</strong> ${vehicle.marca}</p>
                    <p><strong>Modelo:</strong> ${vehicle.modelo}</p>
                    <p><strong>Año:</strong> ${vehicle.anio}</p>
                    <p><strong>Tipo:</strong> ${getTipoMaquinariaText(vehicle.tipoMaquinaria)}</p>
                    <p><strong>Estado:</strong> ${getStatusText(vehicle.estadoOperativo)}</p>
                </div>
            </div>
            
            ${vehicle.capacidadTanque || vehicle.consumoPromedio || vehicle.kilometrajeActual ? `
            <div style="padding: 15px; background: #f8f9fa; border-radius: 10px;">
                <h3 style="margin-bottom: 15px; color: #1a1a2e;">Detalles Técnicos</h3>
                <div style="display: grid; gap: 10px;">
                    ${vehicle.capacidadTanque ? `<p><strong>Capacidad Tanque:</strong> ${formatNumber(vehicle.capacidadTanque)} litros</p>` : ''}
                    ${vehicle.consumoPromedio ? `<p><strong>Consumo Promedio:</strong> ${formatNumber(vehicle.consumoPromedio)} L/100km</p>` : ''}
                    ${vehicle.kilometrajeActual ? `<p><strong>Kilometraje:</strong> ${formatNumber(vehicle.kilometrajeActual)} km</p>` : ''}
                </div>
            </div>
            ` : ''}
            
            <div style="padding: 15px; background: #f8f9fa; border-radius: 10px;">
                <h3 style="margin-bottom: 15px; color: #1a1a2e;">Información del Sistema</h3>
                <div style="display: grid; gap: 10px;">
                    <p><strong>ID:</strong> ${vehicle.id}</p>
                    <p><strong>Estado del Registro:</strong> ${vehicle.activo ? 'Activo' : 'Inactivo'}</p>
                    ${vehicle.fechaCreacion ? `<p><strong>Fecha de Registro:</strong> ${formatDate(vehicle.fechaCreacion)}</p>` : ''}
                    ${vehicle.fechaActualizacion ? `<p><strong>Última Actualización:</strong> ${formatDate(vehicle.fechaActualizacion)}</p>` : ''}
                </div>
            </div>
        </div>
    `;
    
    modal.style.display = 'block';
}

function closeModal() {
    document.getElementById('vehicleModal').style.display = 'none';
}

function editVehicle(vehicle) {
    showEditVehicle(vehicle);
}

async function deleteVehicle(id, placa) {
    if (!confirm(`¿Estás seguro de que deseas eliminar el vehículo ${placa}?`)) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok) {
            showMessage('Vehículo eliminado correctamente', 'success');
            await loadVehicles();
        } else if (response.status === 401) {
            showMessage('Sesión expirada. Redirigiendo al login...', 'error');
            setTimeout(() => redirectToLogin(), 2000);
        } else {
            throw new Error('Error al eliminar el vehículo');
        }
    } catch (error) {
        console.error('Error eliminando vehículo:', error);
        showMessage('Error al eliminar el vehículo', 'error');
    }
}

// Funciones de filtrado
function handleSearch(event) {
    currentFilter.search = event.target.value;
    renderVehicles();
}

function handleFilterType(event) {
    currentFilter.type = event.target.value;
    renderVehicles();
}

function handleFilterStatus(event) {
    currentFilter.status = event.target.value;
    renderVehicles();
}

function filterByType(type) {
    if (type === 'DISPONIBLE') {
        document.getElementById('filterStatus').value = type;
        currentFilter.status = type;
        currentFilter.type = '';
    } else {
        document.getElementById('filterType').value = type;
        currentFilter.type = type;
        currentFilter.status = '';
    }
    showVehiclesList();
}

// Funciones del formulario
async function handleFormSubmit(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const vehicleData = {
        placa: formData.get('placa'),
        marca: formData.get('marca'),
        modelo: formData.get('modelo'),
        anio: parseInt(formData.get('anio')),
        tipoMaquinaria: formData.get('tipoMaquinaria'),
        estadoOperativo: formData.get('estadoOperativo'),
        capacidadTanque: formData.get('capacidadTanque') ? parseFloat(formData.get('capacidadTanque')) : null,
        consumoPromedio: formData.get('consumoPromedio') ? parseFloat(formData.get('consumoPromedio')) : null,
        kilometrajeActual: formData.get('kilometrajeActual') ? parseFloat(formData.get('kilometrajeActual')) : null,
        activo: formData.get('activo') === 'true'
    };
    
    const vehicleId = formData.get('id');
    
    try {
        if (vehicleId) {
            await updateVehicle(vehicleId, vehicleData);
        } else {
            await createVehicle(vehicleData);
        }
    } catch (error) {
        console.error('Error en el formulario:', error);
    }
}

async function createVehicle(vehicleData) {
    try {
        const response = await fetch(`${API_BASE_URL}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify(vehicleData)
        });
        
        if (response.ok) {
            showMessage('Vehículo creado correctamente', 'success');
            await loadVehicles();
            showVehiclesList();
        } else if (response.status === 401) {
            showMessage('Sesión expirada. Redirigiendo al login...', 'error');
            setTimeout(() => redirectToLogin(), 2000);
        } else {
            const error = await response.json();
            showMessage('Error al crear el vehículo: ' + (error.message || 'Error desconocido'), 'error');
        }
    } catch (error) {
        console.error('Error creando vehículo:', error);
        showMessage('Error de conexión al crear el vehículo', 'error');
    }
}

async function updateVehicle(id, vehicleData) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify(vehicleData)
        });
        
        if (response.ok) {
            showMessage('Vehículo actualizado correctamente', 'success');
            await loadVehicles();
            showVehiclesList();
        } else if (response.status === 401) {
            showMessage('Sesión expirada. Redirigiendo al login...', 'error');
            setTimeout(() => redirectToLogin(), 2000);
        } else {
            const error = await response.json();
            showMessage('Error al actualizar el vehículo: ' + (error.message || 'Error desconocido'), 'error');
        }
    } catch (error) {
        console.error('Error actualizando vehículo:', error);
        showMessage('Error de conexión al actualizar el vehículo', 'error');
    }
}

function clearForm() {
    document.getElementById('vehicleForm').reset();
    document.getElementById('vehicleId').value = '';
}

function fillForm(vehicle) {
    document.getElementById('vehicleId').value = vehicle.id;
    document.getElementById('placa').value = vehicle.placa;
    document.getElementById('marca').value = vehicle.marca;
    document.getElementById('modelo').value = vehicle.modelo;
    document.getElementById('anio').value = vehicle.anio;
    document.getElementById('tipoMaquinaria').value = vehicle.tipoMaquinaria;
    document.getElementById('estadoOperativo').value = vehicle.estadoOperativo;
    document.getElementById('capacidadTanque').value = vehicle.capacidadTanque || '';
    document.getElementById('consumoPromedio').value = vehicle.consumoPromedio || '';
    document.getElementById('kilometrajeActual').value = vehicle.kilometrajeActual || '';
    document.getElementById('activo').value = vehicle.activo ? 'true' : 'false';
}

function fillTestData() {
    document.getElementById('placa').value = 'TEST-' + Math.floor(Math.random() * 1000);
    document.getElementById('marca').value = 'Volvo';
    document.getElementById('modelo').value = 'FH16';
    document.getElementById('anio').value = 2023;
    document.getElementById('tipoMaquinaria').value = 'CAMION';
    document.getElementById('estadoOperativo').value = 'DISPONIBLE';
    document.getElementById('capacidadTanque').value = 300;
    document.getElementById('consumoPromedio').value = 25.5;
    document.getElementById('kilometrajeActual').value = 15000;
    document.getElementById('activo').value = 'true';
    
    showMessage('Datos de prueba cargados', 'info');
}

// Funciones auxiliares
function getTipoMaquinariaText(tipo) {
    const tipos = {
        'CAMION': 'Camión',
        'VOLQUETE': 'Volquete',
        'EXCAVADORA': 'Excavadora',
        'CARGADOR': 'Cargador',
        'GRUA': 'Grúa',
        'MOTONIVELADORA': 'Motoniveladora'
    };
    return tipos[tipo] || tipo;
}

function getStatusText(status) {
    const estados = {
        'DISPONIBLE': 'Disponible',
        'EN_USO': 'En Uso',
        'MANTENIMIENTO': 'Mantenimiento',
        'FUERA_SERVICIO': 'Fuera de Servicio'
    };
    return estados[status] || status;
}

function getTypeIcon(type) {
    const icons = {
        'CAMION': 'fas fa-truck',
        'VOLQUETE': 'fas fa-truck-pickup',
        'EXCAVADORA': 'fas fa-hard-hat',
        'CARGADOR': 'fas fa-dolly',
        'GRUA': 'fas fa-crane',
        'MOTONIVELADORA': 'fas fa-road'
    };
    return icons[type] || 'fas fa-truck';
}

function formatNumber(number) {
    return new Intl.NumberFormat('es-ES', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
    }).format(number);
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Sistema de notificaciones
function showMessage(message, type = 'info') {
    const container = document.getElementById('notificationContainer');
    
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    
    const icon = getNotificationIcon(type);
    notification.innerHTML = `
        <div style="display: flex; align-items: center; gap: 10px;">
            <i class="${icon}" style="color: ${getNotificationColor(type)};"></i>
            <span style="color: #1a1a2e; font-weight: 500;">${message}</span>
        </div>
    `;
    
    container.appendChild(notification);
    
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 5000);
}

function getNotificationIcon(type) {
    switch (type) {
        case 'success': return 'fas fa-check-circle';
        case 'error': return 'fas fa-exclamation-circle';
        case 'info': return 'fas fa-info-circle';
        default: return 'fas fa-info-circle';
    }
}

function getNotificationColor(type) {
    switch (type) {
        case 'success': return '#28a745';
        case 'error': return '#dc3545';
        case 'info': return '#17a2b8';
        default: return '#6c757d';
    }
}

// Cerrar modal al hacer clic fuera de él
window.onclick = function(event) {
    const modal = document.getElementById('vehicleModal');
    if (event.target == modal) {
        closeModal();
    }
}

console.log('🚛 SKT Combustible - Gestión de Vehículos (Modo Simple) iniciado');
console.log('📡 API Base URL:', API_BASE_URL);
