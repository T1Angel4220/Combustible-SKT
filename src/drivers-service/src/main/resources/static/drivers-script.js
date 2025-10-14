// Configuración de la API - Usando Gateway
const API_BASE_URL = 'http://localhost:8090/api/v1/drivers';

// Variables globales
let drivers = [];
let authToken = null;
let currentUser = null;
let currentFilter = {
    search: '',
    machinery: '',
    status: '',
    active: ''
};

// Variables para modales
let currentDriverId = null;
let currentDriverName = null;

// Variables para paginación
let currentPage = 0;
let pageSize = 12;
let totalPages = 0;
let totalElements = 0;
let isLoading = false;

// Elementos del DOM
let dashboardSection, driversSection, driverFormSection;
let dashboardBtn, driversBtn, addDriverBtn;

// Inicialización de la aplicación
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
});

function initializeApp() {
    // Obtener elementos del DOM
    dashboardSection = document.getElementById('dashboardSection');
    driversSection = document.getElementById('driversSection');
    driverFormSection = document.getElementById('driverFormSection');
    
    dashboardBtn = document.getElementById('dashboardBtn');
    driversBtn = document.getElementById('driversBtn');
    addDriverBtn = document.getElementById('addDriverBtn');
    
    // Verificar autenticación
    checkAuthentication();
}

function checkAuthentication() {
    console.log('🔍 Verificando autenticación...');
    
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
    driversBtn.addEventListener('click', showDriversList);
    addDriverBtn.addEventListener('click', showAddDriver);
    
    // Event listener para el formulario
    document.getElementById('driverForm').addEventListener('submit', handleFormSubmit);
    
    // Event listeners para filtros
    document.getElementById('searchInput').addEventListener('input', handleSearch);
    document.getElementById('filterMachinery').addEventListener('change', handleFilterMachinery);
    document.getElementById('filterStatus').addEventListener('change', handleFilterStatus);
    document.getElementById('filterActive').addEventListener('change', handleFilterActive);
}

// Funciones de navegación
function showDashboard() {
    hideAllSections();
    dashboardSection.classList.add('active');
    updateNavButtons('dashboard');
    loadDashboardData();
}

function showDriversList() {
    console.log('📋 showDriversList() ejecutada');
    hideAllSections();
    driversSection.classList.add('active');
    updateNavButtons('drivers');
    loadDrivers();
}

// Función específica para "Ver Todos" que limpia filtros
function showAllDrivers() {
    console.log('👥 showAllDrivers() ejecutada');
    hideAllSections();
    driversSection.classList.add('active');
    updateNavButtons('drivers');
    // Limpiar todos los filtros cuando se accede a "Ver Todos"
    clearAllFilters();
    loadDrivers();
}

function showAddDriver() {
    hideAllSections();
    driverFormSection.classList.add('active');
    updateNavButtons('add');
    clearForm();
    document.getElementById('formTitle').textContent = 'Nuevo Chofer';
}

function showEditDriver(driver) {
    hideAllSections();
    driverFormSection.classList.add('active');
    updateNavButtons('add');
    fillForm(driver);
    document.getElementById('formTitle').textContent = 'Editar Chofer';
}

function hideAllSections() {
    dashboardSection.classList.remove('active');
    driversSection.classList.remove('active');
    driverFormSection.classList.remove('active');
}

function updateNavButtons(activeSection) {
    dashboardBtn.classList.remove('active');
    driversBtn.classList.remove('active');
    addDriverBtn.classList.remove('active');
    
    if (activeSection === 'dashboard') {
        dashboardBtn.classList.add('active');
    } else if (activeSection === 'drivers') {
        driversBtn.classList.add('active');
    } else if (activeSection === 'add') {
        addDriverBtn.classList.add('active');
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
        await loadDrivers();
        updateDashboardStats();
        updateMachineryTypeCount();
    } catch (error) {
        console.error('Error cargando datos del dashboard:', error);
        showMessage('Error cargando datos del dashboard', 'error');
    }
}

async function loadDrivers(page = 0, size = 12, forceReload = false) {
    if (isLoading && !forceReload) return;
    
    try {
        isLoading = true;
        console.log(`📡 Cargando choferes página ${page + 1}, tamaño ${size}...`);
        
        // Mostrar indicador de carga
        showPaginationLoading();
        
        // Construir URL con parámetros de paginación
        const url = new URL(`${API_BASE_URL}/all`);
        url.searchParams.append('page', page);
        url.searchParams.append('size', size);
        url.searchParams.append('sortBy', 'nombre');
        url.searchParams.append('sortDir', 'asc');
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        console.log('📊 Respuesta:', response.status, response.statusText);
        
        if (response.ok) {
            const data = await response.json();
            
            // Manejar respuesta paginada
            if (data.content !== undefined) {
                drivers = data.content;
                currentPage = data.number;
                pageSize = data.size;
                totalPages = data.totalPages;
                totalElements = data.totalElements;
            } else {
                // Fallback para respuesta no paginada
                drivers = Array.isArray(data) ? data : [];
                currentPage = 0;
                totalPages = 1;
                totalElements = drivers.length;
            }
            
            console.log(`✅ Choferes cargados: ${drivers.length} de ${totalElements} total (página ${currentPage + 1} de ${totalPages})`);
            
            renderDrivers();
            updatePaginationControls();
            updateDashboardStats();
            
        } else if (response.status === 401) {
            console.log('❌ Error 401: No autorizado');
            showMessage('Sesión expirada. Redirigiendo al login...', 'error');
            setTimeout(() => redirectToLogin(), 2000);
        } else {
            throw new Error('Error al cargar choferes');
        }
    } catch (error) {
        console.error('Error cargando choferes:', error);
        showMessage('Error al cargar la lista de choferes', 'error');
        showNoDriversMessage();
    } finally {
        isLoading = false;
        hidePaginationLoading();
    }
}

function updateDashboardStats() {
    const total = drivers.length;
    const active = drivers.filter(d => d.activo && d.estado === 'DISPONIBLE').length;
    const available = drivers.filter(d => d.activo && d.estado === 'DISPONIBLE').length;
    const inactive = drivers.filter(d => !d.activo || d.estado === 'INACTIVO').length;
    
    document.getElementById('totalDrivers').textContent = total;
    document.getElementById('availableDrivers').textContent = available;
    document.getElementById('activeDrivers').textContent = active;
    document.getElementById('inactiveDrivers').textContent = inactive;
}

function updateMachineryTypeCount() {
    const types = ['CAMION', 'VOLQUETE', 'EXCAVADORA', 'CARGADOR', 'GRUA', 'MOTONIVELADORA'];
    
    types.forEach(type => {
        const count = drivers.filter(d => d.tipoMaquinariaAsignada === type).length;
        const element = document.getElementById(`count-${type}`);
        if (element) {
            element.textContent = `${count} chofer${count !== 1 ? 'es' : ''}`;
        }
    });
}

function renderDrivers() {
    const grid = document.getElementById('driversGrid');
    
    if (!drivers || drivers.length === 0) {
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 40px; color: #6c757d;">
                <i class="fas fa-users" style="font-size: 3rem; margin-bottom: 20px; opacity: 0.3;"></i>
                <p style="font-size: 1.2rem;">No hay choferes registrados</p>
                <button class="btn btn-primary" onclick="showAddDriver()" style="margin-top: 20px;">
                    <i class="fas fa-plus"></i> Agregar Primer Chofer
                </button>
            </div>
        `;
        return;
    }
    
    // Aplicar filtros
    const filteredDrivers = drivers.filter(driver => {
        const matchSearch = !currentFilter.search || 
            driver.nombre.toLowerCase().includes(currentFilter.search.toLowerCase()) ||
            driver.apellido.toLowerCase().includes(currentFilter.search.toLowerCase()) ||
            driver.dni.includes(currentFilter.search) ||
            driver.licencia.toLowerCase().includes(currentFilter.search.toLowerCase());
        
        const matchMachinery = !currentFilter.machinery || driver.tipoMaquinariaAsignada === currentFilter.machinery;
        const matchStatus = !currentFilter.status || driver.estado === currentFilter.status;
        const matchActive = !currentFilter.active || driver.activo.toString() === currentFilter.active;
        
        return matchSearch && matchMachinery && matchStatus && matchActive;
    });
    
    if (filteredDrivers.length === 0) {
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 40px; color: #6c757d;">
                <i class="fas fa-search" style="font-size: 3rem; margin-bottom: 20px; opacity: 0.3;"></i>
                <p style="font-size: 1.2rem;">No se encontraron choferes con los filtros aplicados</p>
            </div>
        `;
        return;
    }
    
    // Ordenar choferes: activos primero, luego inactivos
    filteredDrivers.sort((a, b) => {
        if (a.activo !== b.activo) {
            return b.activo - a.activo; // activo=true (1) va antes que activo=false (0)
        }
        return a.nombre.localeCompare(b.nombre);
    });
    
    grid.innerHTML = filteredDrivers.map(driver => createDriverCard(driver)).join('');
}

function createDriverCard(driver) {
    const statusClass = `status-${driver.estado.toLowerCase().replace('_', '')}`;
    const statusText = getStatusText(driver.estado);
    const machineryText = driver.tipoMaquinariaAsignada ? getTipoMaquinariaText(driver.tipoMaquinariaAsignada) : 'Sin asignar';
    
    return `
        <div class="driver-card ${!driver.activo ? 'driver-inactive' : ''}">
            <div class="driver-card-header">
                <div class="driver-name">
                    <i class="fas fa-user-tie"></i>
                    ${driver.nombre} ${driver.apellido}
                    ${!driver.activo ? '<span class="inactive-badge">INACTIVO</span>' : ''}
                </div>
                <span class="driver-status ${statusClass}">${statusText}</span>
            </div>
            
            <div class="driver-info">
                <div class="info-row">
                    <span class="info-label"><i class="fas fa-id-card"></i> DNI:</span>
                    <span class="info-value">${driver.dni}</span>
                </div>
                <div class="info-row">
                    <span class="info-label"><i class="fas fa-id-badge"></i> Licencia:</span>
                    <span class="info-value">${driver.licencia}</span>
                </div>
                ${driver.telefono ? `
                <div class="info-row">
                    <span class="info-label"><i class="fas fa-phone"></i> Teléfono:</span>
                    <span class="info-value">${driver.telefono}</span>
                </div>
                ` : ''}
                ${driver.email ? `
                <div class="info-row">
                    <span class="info-label"><i class="fas fa-envelope"></i> Email:</span>
                    <span class="info-value">${driver.email}</span>
                </div>
                ` : ''}
                <div class="info-row">
                    <span class="info-label"><i class="fas fa-truck"></i> Maquinaria:</span>
                    <span class="info-value">${machineryText}</span>
                </div>
            </div>
            
            <div class="driver-actions">
                <button class="btn btn-info btn-sm" onclick='viewDriver(${JSON.stringify(driver).replace(/'/g, "&apos;")})'>
                    <i class="fas fa-eye"></i> Ver
                </button>
                <button class="btn btn-primary btn-sm" onclick='editDriver(${JSON.stringify(driver).replace(/'/g, "&apos;")})'>
                    <i class="fas fa-edit"></i> Editar
                </button>
                ${driver.activo ? `
                <button class="btn btn-warning btn-sm" onclick="showDeactivateModal('${driver.id}', '${driver.nombre} ${driver.apellido}')">
                    <i class="fas fa-user-slash"></i> Poner Inactivo
                </button>
                ` : `
                <button class="btn btn-success btn-sm" onclick="showActivateModal('${driver.id}', '${driver.nombre} ${driver.apellido}')">
                    <i class="fas fa-user-check"></i> Volver Activo
                </button>
                `}
                <button class="btn btn-danger btn-sm" onclick="showDeleteModal('${driver.id}', '${driver.nombre} ${driver.apellido}')">
                    <i class="fas fa-trash"></i> Eliminar
                </button>
            </div>
        </div>
    `;
}

function viewDriver(driver) {
    const modal = document.getElementById('driverModal');
    const details = document.getElementById('driverDetails');
    
    details.innerHTML = `
        <div style="display: grid; gap: 15px;">
            <div style="padding: 15px; background: #f8f9fa; border-radius: 10px;">
                <h3 style="margin-bottom: 15px; color: #1a1a2e;">Información Personal</h3>
                <div style="display: grid; gap: 10px;">
                    <p><strong>Nombre Completo:</strong> ${driver.nombre} ${driver.apellido}</p>
                    <p><strong>DNI:</strong> ${driver.dni}</p>
                    <p><strong>Licencia:</strong> ${driver.licencia}</p>
                    ${driver.telefono ? `<p><strong>Teléfono:</strong> ${driver.telefono}</p>` : ''}
                    ${driver.email ? `<p><strong>Email:</strong> ${driver.email}</p>` : ''}
                    ${driver.fechaContratacion ? `<p><strong>Fecha de Contratación:</strong> ${formatDate(driver.fechaContratacion)}</p>` : ''}
                </div>
            </div>
            
            <div style="padding: 15px; background: #f8f9fa; border-radius: 10px;">
                <h3 style="margin-bottom: 15px; color: #1a1a2e;">Información Laboral</h3>
                <div style="display: grid; gap: 10px;">
                    <p><strong>Estado:</strong> ${getStatusText(driver.estado)}</p>
                    <p><strong>Maquinaria Asignada:</strong> ${driver.tipoMaquinariaAsignada ? getTipoMaquinariaText(driver.tipoMaquinariaAsignada) : 'Sin asignar'}</p>
                </div>
            </div>
            
            <div style="padding: 15px; background: #f8f9fa; border-radius: 10px;">
                <h3 style="margin-bottom: 15px; color: #1a1a2e;">Información del Sistema</h3>
                <div style="display: grid; gap: 10px;">
                    <p><strong>ID:</strong> ${driver.id}</p>
                    <p><strong>Estado del Registro:</strong> ${driver.activo ? 'Activo' : 'Inactivo'}</p>
                    ${driver.createdAt ? `<p><strong>Fecha de Registro:</strong> ${formatDate(driver.createdAt)}</p>` : ''}
                    ${driver.updatedAt ? `<p><strong>Última Actualización:</strong> ${formatDate(driver.updatedAt)}</p>` : ''}
                </div>
            </div>
        </div>
    `;
    
    modal.style.display = 'block';
}

function closeModal() {
    document.getElementById('driverModal').style.display = 'none';
}

function editDriver(driver) {
    showEditDriver(driver);
}

// ==========================================
// FUNCIONES DE MODALES PERSONALIZADOS
// ==========================================

// Mostrar modal de desactivación
function showDeactivateModal(id, nombre) {
    currentDriverId = id;
    currentDriverName = nombre;
    
    document.getElementById('deactivateDriverName').textContent = nombre;
    document.getElementById('deactivateModal').style.display = 'block';
}

// Mostrar modal de eliminación permanente
function showDeleteModal(id, nombre) {
    currentDriverId = id;
    currentDriverName = nombre;
    
    document.getElementById('deleteDriverName').textContent = nombre;
    document.getElementById('deleteModal').style.display = 'block';
}

// Mostrar modal de reactivación
function showActivateModal(id, nombre) {
    currentDriverId = id;
    currentDriverName = nombre;
    
    document.getElementById('activateDriverName').textContent = nombre;
    document.getElementById('activateModal').style.display = 'block';
}

// Cerrar modales
function closeDeactivateModal() {
    document.getElementById('deactivateModal').style.display = 'none';
    currentDriverId = null;
    currentDriverName = null;
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    currentDriverId = null;
    currentDriverName = null;
}

function closeActivateModal() {
    document.getElementById('activateModal').style.display = 'none';
    currentDriverId = null;
    currentDriverName = null;
}

// Confirmar acciones
async function confirmDeactivate() {
    if (!currentDriverId) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/${currentDriverId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok || response.status === 204) {
            showMessage('Chofer puesto como inactivo correctamente', 'success');
            closeDeactivateModal();
            await reloadDrivers();
        } else if (response.status === 401) {
            showMessage('Sesión expirada. Redirigiendo al login...', 'error');
            setTimeout(() => redirectToLogin(), 2000);
        } else {
            throw new Error('Error al desactivar el chofer');
        }
    } catch (error) {
        console.error('Error desactivando chofer:', error);
        showMessage('Error al desactivar el chofer', 'error');
    }
}

async function confirmDelete() {
    if (!currentDriverId) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/${currentDriverId}/permanent`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok || response.status === 204) {
            showMessage('Chofer eliminado permanentemente de la base de datos', 'success');
            closeDeleteModal();
            await reloadDrivers();
        } else if (response.status === 401) {
            showMessage('Sesión expirada. Redirigiendo al login...', 'error');
            setTimeout(() => redirectToLogin(), 2000);
        } else {
            throw new Error('Error al eliminar permanentemente el chofer');
        }
    } catch (error) {
        console.error('Error eliminando chofer permanentemente:', error);
        showMessage('Error al eliminar permanentemente el chofer', 'error');
    }
}

async function confirmActivate() {
    if (!currentDriverId) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/${currentDriverId}/activate`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok) {
            showMessage('Chofer reactivado correctamente', 'success');
            closeActivateModal();
            await reloadDrivers();
        } else if (response.status === 401) {
            showMessage('Sesión expirada. Redirigiendo al login...', 'error');
            setTimeout(() => redirectToLogin(), 2000);
        } else {
            throw new Error('Error al reactivar el chofer');
        }
    } catch (error) {
        console.error('Error reactivando chofer:', error);
        showMessage('Error al reactivar el chofer', 'error');
    }
}

// Cerrar modales al hacer clic fuera
window.onclick = function(event) {
    const deactivateModal = document.getElementById('deactivateModal');
    const deleteModal = document.getElementById('deleteModal');
    const activateModal = document.getElementById('activateModal');
    
    if (event.target === deactivateModal) {
        closeDeactivateModal();
    }
    if (event.target === deleteModal) {
        closeDeleteModal();
    }
    if (event.target === activateModal) {
        closeActivateModal();
    }
}

// ==========================================
// FUNCIONES DE PAGINACIÓN
// ==========================================

// Mostrar indicador de carga
function showPaginationLoading() {
    const grid = document.getElementById('driversGrid');
    grid.innerHTML = `
        <div class="pagination-loading">
            <i class="fas fa-spinner"></i>
            Cargando choferes...
        </div>
    `;
}

// Ocultar indicador de carga
function hidePaginationLoading() {
    // El contenido se actualizará en renderDrivers()
}

// Mostrar mensaje cuando no hay choferes
function showNoDriversMessage() {
    const grid = document.getElementById('driversGrid');
    grid.innerHTML = `
        <div class="no-drivers-message">
            <i class="fas fa-users"></i>
            <h3>No hay choferes registrados</h3>
            <p>Comienza agregando tu primer chofer al sistema.</p>
            <button class="btn btn-primary" onclick="showAddDriver()">
                <i class="fas fa-user-plus"></i> Agregar Primer Chofer
            </button>
        </div>
    `;
}

// Actualizar controles de paginación
function updatePaginationControls() {
    const paginationInfo = document.getElementById('paginationInfo');
    const firstPageBtn = document.getElementById('firstPageBtn');
    const prevPageBtn = document.getElementById('prevPageBtn');
    const nextPageBtn = document.getElementById('nextPageBtn');
    const lastPageBtn = document.getElementById('lastPageBtn');
    const pageNumbers = document.getElementById('pageNumbers');
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    
    if (!paginationInfo) return;
    
    // Actualizar información
    const startItem = (currentPage * pageSize) + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, totalElements);
    paginationInfo.textContent = `Mostrando ${startItem}-${endItem} de ${totalElements} choferes`;
    
    // Actualizar estado de botones
    firstPageBtn.disabled = currentPage === 0;
    prevPageBtn.disabled = currentPage === 0;
    nextPageBtn.disabled = currentPage >= totalPages - 1;
    lastPageBtn.disabled = currentPage >= totalPages - 1;
    
    // Actualizar selector de tamaño
    pageSizeSelect.value = pageSize;
    
    // Generar números de página
    generatePageNumbers(pageNumbers);
}

// Generar números de página
function generatePageNumbers(container) {
    if (!container) return;
    
    container.innerHTML = '';
    
    const maxVisiblePages = 7;
    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
    
    // Ajustar si estamos cerca del final
    if (endPage - startPage < maxVisiblePages - 1) {
        startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }
    
    // Primera página si no está visible
    if (startPage > 0) {
        addPageNumber(container, 0);
        if (startPage > 1) {
            addEllipsis(container);
        }
    }
    
    // Páginas visibles
    for (let i = startPage; i <= endPage; i++) {
        addPageNumber(container, i);
    }
    
    // Última página si no está visible
    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            addEllipsis(container);
        }
        addPageNumber(container, totalPages - 1);
    }
}

// Agregar número de página
function addPageNumber(container, pageNum) {
    const pageElement = document.createElement('span');
    pageElement.className = `page-number ${pageNum === currentPage ? 'active' : ''}`;
    pageElement.textContent = pageNum + 1;
    pageElement.onclick = () => goToPage(pageNum);
    container.appendChild(pageElement);
}

// Agregar puntos suspensivos
function addEllipsis(container) {
    const ellipsis = document.createElement('span');
    ellipsis.className = 'page-number ellipsis';
    ellipsis.textContent = '...';
    container.appendChild(ellipsis);
}

// Navegación de páginas
function goToFirstPage() {
    if (currentPage > 0) {
        goToPage(0);
    }
}

function goToPreviousPage() {
    if (currentPage > 0) {
        goToPage(currentPage - 1);
    }
}

function goToNextPage() {
    if (currentPage < totalPages - 1) {
        goToPage(currentPage + 1);
    }
}

function goToLastPage() {
    if (currentPage < totalPages - 1) {
        goToPage(totalPages - 1);
    }
}

function goToPage(page) {
    if (page !== currentPage && page >= 0 && page < totalPages) {
        loadDrivers(page, pageSize, true);
    }
}

// Cambiar tamaño de página
function changePageSize(newSize) {
    const size = parseInt(newSize);
    if (size !== pageSize) {
        pageSize = size;
        currentPage = 0; // Volver a la primera página
        loadDrivers(currentPage, pageSize, true);
    }
}

// Función para recargar datos (usada después de crear/editar/eliminar)
async function reloadDrivers() {
    // Mantener la página actual si es válida
    if (currentPage >= totalPages && totalPages > 0) {
        currentPage = totalPages - 1;
    }
    await loadDrivers(currentPage, pageSize, true);
}

// Funciones de filtrado
function handleSearch(event) {
    currentFilter.search = event.target.value;
    // Con paginación, necesitamos recargar desde la primera página
    currentPage = 0;
    loadDrivers(currentPage, pageSize, true);
}

function clearSearch() {
    currentFilter.search = '';
    const searchInput = document.getElementById('searchInput');
    if (searchInput) searchInput.value = '';
    currentPage = 0;
    loadDrivers(currentPage, pageSize, true);
}

function handleFilterMachinery(event) {
    currentFilter.machinery = event.target.value;
    currentPage = 0;
    loadDrivers(currentPage, pageSize, true);
}

function handleFilterStatus(event) {
    currentFilter.status = event.target.value;
    currentPage = 0;
    loadDrivers(currentPage, pageSize, true);
}

function handleFilterActive(event) {
    currentFilter.active = event.target.value;
    currentPage = 0;
    loadDrivers(currentPage, pageSize, true);
}

function clearAllFilters() {
    // Limpiar objeto de filtros
    currentFilter = {
        search: '',
        machinery: '',
        status: '',
        active: ''
    };
    
    // Limpiar elementos del DOM
    const searchInput = document.getElementById('searchInput');
    const filterMachinery = document.getElementById('filterMachinery');
    const filterStatus = document.getElementById('filterStatus');
    const filterActive = document.getElementById('filterActive');
    
    if (searchInput) searchInput.value = '';
    if (filterMachinery) filterMachinery.value = '';
    if (filterStatus) filterStatus.value = '';
    if (filterActive) filterActive.value = '';
    
    currentPage = 0;
    loadDrivers(currentPage, pageSize, true);
}

function filterByStatus(status) {
    document.getElementById('filterStatus').value = status;
    currentFilter.status = status;
    // Limpiar otros filtros cuando se filtra por estado operativo
    currentFilter.machinery = '';
    document.getElementById('filterMachinery').value = '';
    // Para "Disponibles" necesitamos mantener activo = true
    if (status !== 'DISPONIBLE') {
        currentFilter.active = '';
        document.getElementById('filterActive').value = '';
    }
    renderDrivers();
}

function filterByActive(active) {
    document.getElementById('filterActive').value = active;
    currentFilter.active = active;
    // Limpiar otros filtros cuando se filtra por estado activo
    currentFilter.status = '';
    currentFilter.machinery = '';
    document.getElementById('filterStatus').value = '';
    document.getElementById('filterMachinery').value = '';
    renderDrivers();
}

function filterByMachinery(machinery) {
    console.log('🚛 filterByMachinery() ejecutada para:', machinery);
    clearAllFilters();
    currentFilter.machinery = machinery;
    document.getElementById('filterMachinery').value = machinery;
    console.log('📊 Filtro aplicado - maquinaria:', currentFilter.machinery);
    // Navegar a la sección de lista de choferes
    showDriversList();
}

// Función específica para choferes disponibles (activos y con estado DISPONIBLE)
function filterAvailableDrivers() {
    console.log('✅ filterAvailableDrivers() ejecutada');
    clearAllFilters();
    currentFilter.status = 'DISPONIBLE';
    currentFilter.active = 'true';
    document.getElementById('filterStatus').value = 'DISPONIBLE';
    document.getElementById('filterActive').value = 'true';
    console.log('📊 Filtros aplicados - estado:', currentFilter.status, 'activo:', currentFilter.active);
    // Navegar a la sección de lista de choferes
    showDriversList();
}

// Función específica para choferes inactivos
function filterInactiveDrivers() {
    console.log('🔍 filterInactiveDrivers() ejecutada');
    clearAllFilters();
    currentFilter.active = 'false';
    document.getElementById('filterActive').value = 'false';
    console.log('📊 Filtro aplicado - activo:', currentFilter.active);
    // Navegar a la sección de lista de choferes
    showDriversList();
}

// Función para mostrar búsqueda (ir a la lista con foco en búsqueda)
function showSearchDrivers() {
    console.log('🔍 showSearchDrivers() ejecutada');
    clearAllFilters();
    showDriversList();
    // Enfocar el campo de búsqueda
    setTimeout(() => {
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.focus();
        }
    }, 500);
}

// Funciones del formulario
async function handleFormSubmit(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const driverData = {
        nombre: formData.get('nombre'),
        apellido: formData.get('apellido'),
        dni: formData.get('dni'),
        licencia: formData.get('licencia'),
        telefono: formData.get('telefono') || null,
        email: formData.get('email') || null,
        fechaContratacion: formData.get('fechaContratacion') || null,
        estado: formData.get('estado'),
        tipoMaquinariaAsignada: formData.get('tipoMaquinariaAsignada') || null,
        activo: formData.get('activo') === 'true'
    };
    
    const driverId = formData.get('id');
    
    try {
        if (driverId) {
            await updateDriver(driverId, driverData);
        } else {
            await createDriver(driverData);
        }
    } catch (error) {
        console.error('Error en el formulario:', error);
    }
}

async function createDriver(driverData) {
    try {
        const response = await fetch(`${API_BASE_URL}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify(driverData)
        });
        
        if (response.ok) {
            showMessage('Chofer creado correctamente', 'success');
            await loadDrivers();
            showDriversList();
        } else if (response.status === 401) {
            showMessage('Sesión expirada. Redirigiendo al login...', 'error');
            setTimeout(() => redirectToLogin(), 2000);
        } else {
            const error = await response.json();
            showMessage('Error al crear el chofer: ' + (error.message || 'Error desconocido'), 'error');
        }
    } catch (error) {
        console.error('Error creando chofer:', error);
        showMessage('Error de conexión al crear el chofer', 'error');
    }
}

async function updateDriver(id, driverData) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify(driverData)
        });
        
        if (response.ok) {
            showMessage('Chofer actualizado correctamente', 'success');
            await loadDrivers();
            showDriversList();
        } else if (response.status === 401) {
            showMessage('Sesión expirada. Redirigiendo al login...', 'error');
            setTimeout(() => redirectToLogin(), 2000);
        } else {
            const error = await response.json();
            showMessage('Error al actualizar el chofer: ' + (error.message || 'Error desconocido'), 'error');
        }
    } catch (error) {
        console.error('Error actualizando chofer:', error);
        showMessage('Error de conexión al actualizar el chofer', 'error');
    }
}

function clearForm() {
    document.getElementById('driverForm').reset();
    document.getElementById('driverId').value = '';
    document.getElementById('estado').value = 'DISPONIBLE';
    document.getElementById('activo').value = 'true';
}

function fillForm(driver) {
    document.getElementById('driverId').value = driver.id;
    document.getElementById('nombre').value = driver.nombre;
    document.getElementById('apellido').value = driver.apellido;
    document.getElementById('dni').value = driver.dni;
    document.getElementById('licencia').value = driver.licencia;
    document.getElementById('telefono').value = driver.telefono || '';
    document.getElementById('email').value = driver.email || '';
    document.getElementById('fechaContratacion').value = driver.fechaContratacion || '';
    document.getElementById('estado').value = driver.estado || 'DISPONIBLE';
    document.getElementById('tipoMaquinariaAsignada').value = driver.tipoMaquinariaAsignada || '';
    document.getElementById('activo').value = driver.activo ? 'true' : 'false';
}

function fillTestData() {
    const randomNum = Math.floor(Math.random() * 1000);
    document.getElementById('nombre').value = 'Juan';
    document.getElementById('apellido').value = 'Pérez';
    document.getElementById('dni').value = '12345' + randomNum.toString().padStart(3, '0');
    document.getElementById('licencia').value = 'LIC-' + randomNum;
    document.getElementById('telefono').value = '+51987654321';
    document.getElementById('email').value = `chofer${randomNum}@skt.com`;
    document.getElementById('fechaContratacion').value = '2024-01-15';
    document.getElementById('estado').value = 'DISPONIBLE';
    document.getElementById('tipoMaquinariaAsignada').value = 'CAMION';
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
        'ASIGNADO': 'Asignado',
        'EN_RUTA': 'En Ruta',
        'DESCANSANDO': 'Descansando',
        'VACACIONES': 'En Vacaciones',
        'ENFERMO': 'Enfermo',
        'LICENCIA': 'En Licencia'
    };
    return estados[status] || status;
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
    const modal = document.getElementById('driverModal');
    if (event.target == modal) {
        closeModal();
    }
}

console.log('👷 SKT Combustible - Gestión de Choferes iniciado');
console.log('📡 API Base URL:', API_BASE_URL);

