// Vehicles Management Script
const API_BASE_URL = 'http://localhost:8082/api/v1/vehicles';
const ASSIGNMENTS_API_URL = 'http://localhost:8082/api/v1/assignments';
const DRIVERS_API_URL = 'http://localhost:8081/api/v1/drivers';

let vehicles = [];
let drivers = [];
let vehicleAssignments = {}; // Mapa de vehicleId -> asignación activa
let currentFilter = 'all';
let editingVehicleId = null;
let assigningVehicleId = null;
let confirmationCallback = null; // Callback para el modal de confirmación
let currentUserRole = null; // Rol del usuario actual

document.addEventListener('DOMContentLoaded', async function() {
    await checkAuth();
    loadVehicles();
    loadDrivers();
    setupEventListeners();
    
    // Agregar listener para el formulario de filtros de reportes
    const reportFiltersForm = document.getElementById('reportFiltersForm');
    if (reportFiltersForm) {
        reportFiltersForm.addEventListener('submit', handleReportFiltersSubmit);
    }
});

async function checkAuth() {
    // Primero verificar si hay token en la URL (viene del dashboard)
    const urlParams = new URLSearchParams(window.location.search);
    const tokenFromUrl = urlParams.get('token');
    const userFromUrl = urlParams.get('user');
    
    if (tokenFromUrl && userFromUrl) {
        // Guardar en localStorage y sessionStorage
        localStorage.setItem('authToken', tokenFromUrl);
        localStorage.setItem('currentUser', userFromUrl);
        sessionStorage.setItem('authToken', tokenFromUrl);
        sessionStorage.setItem('currentUser', userFromUrl);
        
        // Limpiar URL
        window.history.replaceState({}, document.title, window.location.pathname);
    }
    
    // Verificar token en localStorage o sessionStorage
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    if (!token) {
        window.location.href = 'http://localhost:8085/';
        return;
    }
    
    // Si no estaba en localStorage, copiarlo desde sessionStorage
    if (!localStorage.getItem('authToken') && sessionStorage.getItem('authToken')) {
        localStorage.setItem('authToken', sessionStorage.getItem('authToken'));
        if (sessionStorage.getItem('currentUser')) {
            localStorage.setItem('currentUser', sessionStorage.getItem('currentUser'));
        }
    }
    
    // Obtener el rol del usuario desde el token
    try {
        const response = await fetch('http://localhost:8085/api/auth/me', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const userData = await response.json();
            currentUserRole = userData.rol || userData.role || null;
            // Aplicar restricciones de UI según el rol
            applyRoleBasedUI();
        }
    } catch (error) {
        console.error('Error obteniendo información del usuario:', error);
    }
}

function applyRoleBasedUI() {
    // Ocultar botones según el rol
    const addVehicleBtn = document.querySelector('.add-vehicle-btn');
    
    // Solo ADMIN puede crear vehículos
    if (currentUserRole !== 'ADMIN') {
        if (addVehicleBtn) addVehicleBtn.style.display = 'none';
    }
    
    // Re-renderizar la tabla para ocultar botones de acciones
    if (vehicles.length > 0) {
        renderVehicles();
    }
}

function setupEventListeners() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', filterVehicles);
    }
    
    const vehicleForm = document.getElementById('vehicleForm');
    if (vehicleForm) {
        vehicleForm.addEventListener('submit', handleVehicleSubmit);
    }
    
    const assignDriverForm = document.getElementById('assignDriverForm');
    if (assignDriverForm) {
        assignDriverForm.addEventListener('submit', handleAssignDriverSubmit);
    }
    
    // Interceptar clics en enlaces externos para compartir token
    document.querySelectorAll('a[href^="http://localhost:8083"], a[href^="http://localhost:8081"], a[href^="http://localhost:8085"]').forEach(link => {
        link.addEventListener('click', function(e) {
            const url = new URL(this.href);
            const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
            if (token) {
                url.searchParams.set('token', token);
                const user = localStorage.getItem('currentUser') || sessionStorage.getItem('currentUser');
                if (user) {
                    url.searchParams.set('user', user);
                }
                this.href = url.toString();
            }
        });
    });
}

async function loadVehicles() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(API_BASE_URL, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            vehicles = Array.isArray(data) ? data : [];
            updateMetrics();
            // Cargar asignaciones después de cargar vehículos
            await loadVehicleAssignments();
            // Asegurar que los conductores estén cargados antes de renderizar
            if (drivers.length === 0) {
                await loadDrivers();
            }
            renderVehicles();
            // Cargar nombres de conductores de forma asíncrona si faltan
            await loadMissingDriverNames();
        } else {
            console.error('Error loading vehicles');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

// Cargar nombres de conductores que faltan en la lista
async function loadMissingDriverNames() {
    const missingDrivers = [];
    for (const vehicleId in vehicleAssignments) {
        const asignacion = vehicleAssignments[vehicleId];
        if (asignacion && asignacion.choferId) {
            const driver = drivers.find(d => d.id === asignacion.choferId);
            if (!driver) {
                missingDrivers.push(asignacion.choferId);
            }
        }
    }
    
    // Cargar conductores faltantes
    for (const driverId of missingDrivers) {
        try {
            const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
            const response = await fetch(`${DRIVERS_API_URL}/${driverId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (response.ok) {
                const driver = await response.json();
                if (driver && !drivers.find(d => d.id === driver.id)) {
                    drivers.push(driver);
                }
            }
        } catch (error) {
            console.error(`Error cargando conductor ${driverId}:`, error);
        }
    }
    
    // Re-renderizar si se cargaron conductores
    if (missingDrivers.length > 0) {
        renderVehicles();
    }
}

// Cargar TODOS los conductores (no solo los disponibles, para mostrar nombres de asignados)
async function loadDrivers() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        // Cargar todos los conductores, no solo los disponibles
        const response = await fetch(`${DRIVERS_API_URL}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            // Si la respuesta es un objeto con una lista, extraerla
            if (data && Array.isArray(data)) {
                drivers = data;
            } else if (data && data.drivers && Array.isArray(data.drivers)) {
                drivers = data.drivers;
            } else if (data && data.content && Array.isArray(data.content)) {
                drivers = data.content;
            } else {
                drivers = [];
            }
            console.log('Conductores cargados:', drivers.length);
        } else {
            console.error('Error loading drivers:', response.status);
            // Si falla, intentar con /disponibles como fallback
            const fallbackResponse = await fetch(`${DRIVERS_API_URL}/disponibles`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (fallbackResponse.ok) {
                const fallbackData = await fallbackResponse.json();
                drivers = Array.isArray(fallbackData) ? fallbackData : [];
            }
        }
    } catch (error) {
        console.error('Error loading drivers:', error);
    }
}

// Cargar asignaciones para todos los vehículos
async function loadVehicleAssignments() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        // Cargar asignaciones para cada vehículo
        for (const vehicle of vehicles) {
            try {
                const response = await fetch(`${ASSIGNMENTS_API_URL}/vehiculo/${vehicle.id}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                
                if (response.ok) {
                    const asignaciones = await response.json();
                    // Buscar asignación activa
                    const asignacionActiva = asignaciones.find(a => a.estado === 'ACTIVA');
                    vehicleAssignments[vehicle.id] = asignacionActiva || null;
                } else {
                    vehicleAssignments[vehicle.id] = null;
                }
            } catch (error) {
                console.error(`Error cargando asignación para vehículo ${vehicle.id}:`, error);
                vehicleAssignments[vehicle.id] = null;
            }
        }
    } catch (error) {
        console.error('Error cargando asignaciones:', error);
    }
}

// Obtener conductor asignado para un vehículo
async function getAssignedDriver(vehicleId) {
    const asignacion = vehicleAssignments[vehicleId];
    if (asignacion && asignacion.choferId) {
        // Buscar el conductor en la lista de drivers
        let driver = drivers.find(d => d.id === asignacion.choferId);
        
        // Si no está en la lista, intentar cargarlo
        if (!driver) {
            try {
                const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
                const response = await fetch(`${DRIVERS_API_URL}/${asignacion.choferId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                if (response.ok) {
                    driver = await response.json();
                    // Agregar a la lista para futuras referencias
                    if (driver && !drivers.find(d => d.id === driver.id)) {
                        drivers.push(driver);
                    }
                }
            } catch (error) {
                console.error('Error cargando conductor:', error);
            }
        }
        
        if (driver) {
            return `${driver.nombre} ${driver.apellido}`;
        }
        // Si aún no se encuentra, mostrar el ID del conductor en lugar de "Conductor asignado"
        return `Conductor ID: ${asignacion.choferId.substring(0, 8)}...`;
    }
    return 'Sin asignar';
}

// Versión síncrona para usar en renderVehicles (mostrará "Cargando..." mientras se obtiene)
function getAssignedDriverSync(vehicleId) {
    const asignacion = vehicleAssignments[vehicleId];
    if (asignacion && asignacion.choferId) {
        const driver = drivers.find(d => d.id === asignacion.choferId);
        if (driver) {
            return `${driver.nombre} ${driver.apellido}`;
        }
        // Si no está en la lista, intentar cargarlo de forma asíncrona
        getAssignedDriver(vehicleId).then(() => {
            // Recargar la tabla después de obtener el conductor
            renderVehicles();
        });
        return 'Cargando...';
    }
    return 'Sin asignar';
}

function updateMetrics() {
    const total = vehicles.length;
    const light = vehicles.filter(v => isLightMachinery(v.tipoMaquinaria)).length;
    const heavy = vehicles.filter(v => isHeavyMachinery(v.tipoMaquinaria)).length;
    // Contar vehículos activos (activo=true y estado DISPONIBLE o EN_USO)
    const active = vehicles.filter(v => v.activo && 
        (v.estadoOperativo === 'DISPONIBLE' || v.estadoOperativo === 'EN_USO' || v.estadoOperativo === 'ASIGNADO')).length;
    
    document.getElementById('totalVehicles').textContent = total;
    document.getElementById('lightMachinery').textContent = light;
    document.getElementById('heavyMachinery').textContent = heavy;
    document.getElementById('activeVehicles').textContent = active;
}

function isLightMachinery(tipo) {
    return tipo === 'CAMION' || tipo === 'VOLQUETE';
}

function isHeavyMachinery(tipo) {
    return ['EXCAVADORA', 'CARGADOR', 'GRUA', 'MOTONIVELADORA'].includes(tipo);
}

function filterByType(type) {
    currentFilter = type;
    
    // Update tabs
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    filterVehicles();
}

function filterVehicles() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    
    let filtered = vehicles;
    
    // Filter by type
    if (currentFilter === 'liviana') {
        filtered = filtered.filter(v => isLightMachinery(v.tipoMaquinaria));
    } else if (currentFilter === 'pesada') {
        filtered = filtered.filter(v => isHeavyMachinery(v.tipoMaquinaria));
    }
    
    // Filter by search
    if (searchTerm) {
        filtered = filtered.filter(v => 
            v.placa?.toLowerCase().includes(searchTerm) ||
            v.marca?.toLowerCase().includes(searchTerm) ||
            v.modelo?.toLowerCase().includes(searchTerm)
        );
    }
    
    renderVehicles(filtered);
}

function renderVehicles(vehiclesToRender = vehicles) {
    const tbody = document.getElementById('vehiclesTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = vehiclesToRender.map(vehicle => {
        const isLight = isLightMachinery(vehicle.tipoMaquinaria);
        const typeClass = isLight ? 'liviana' : 'pesada';
        const typeText = isLight ? 'Liviana' : 'Pesada';
        
        const statusClass = getStatusClass(vehicle.estadoOperativo);
        const statusText = getStatusText(vehicle.estadoOperativo);
        
        return `
            <tr>
                <td class="vehicle-code">${vehicle.placa || 'N/A'}</td>
                <td>
                    <span class="type-badge ${typeClass}">${typeText}</span>
                </td>
                <td>${vehicle.marca || ''} ${vehicle.modelo || ''}</td>
                <td>${vehicle.anio || 'N/A'}</td>
                <td>${vehicle.capacidadTanque ? vehicle.capacidadTanque + ' L' : 'N/A'}</td>
                <td>
                    ${vehicle.consumoPromedio || 'N/A'}
                    ${vehicle.estadoOperativo === 'MANTENIMIENTO' ? '<i class="fas fa-exclamation-triangle warning-icon"></i>' : ''}
                </td>
                <td>
                    <span class="status-badge ${statusClass}">${statusText}</span>
                </td>
                <td>
                    <span class="driver-assigned" id="driver-${vehicle.id}">${getAssignedDriverSync(vehicle.id)}</span>
                </td>
                <td>${formatDate(vehicle.fechaActualizacion)}</td>
                <td>
                    <div class="table-actions">
                        ${(currentUserRole === 'ADMIN' || currentUserRole === 'SUPERVISOR')
                            ? `<button class="action-btn edit" onclick="editVehicle('${vehicle.id}')" title="Editar">
                                <i class="fas fa-edit"></i>
                            </button>`
                            : ''
                        }
                        ${(currentUserRole === 'ADMIN' || currentUserRole === 'SUPERVISOR')
                            ? (getAssignedDriverSync(vehicle.id) !== 'Sin asignar' && getAssignedDriverSync(vehicle.id) !== 'Cargando...'
                                ? `<button class="action-btn unassign" onclick="unassignVehicle('${vehicle.id}')" title="Desasignar conductor">
                                    <i class="fas fa-user-minus"></i>
                                </button>`
                                : `<button class="action-btn assign" onclick="assignDriver('${vehicle.id}')" title="Asignar conductor">
                                    <i class="fas fa-user-plus"></i>
                                </button>`
                            )
                            : ''
                        }
                        ${currentUserRole === 'ADMIN'
                            ? `<button class="action-btn delete" onclick="deleteVehicle('${vehicle.id}')" title="Eliminar">
                                <i class="fas fa-trash"></i>
                            </button>`
                            : ''
                        }
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function getStatusClass(estado) {
    if (estado === 'DISPONIBLE') return 'activo';
    if (estado === 'EN_USO' || estado === 'ASIGNADO') return 'activo'; // En uso también es activo visualmente
    if (estado === 'MANTENIMIENTO') return 'mantenimiento';
    return 'fuera-servicio';
}

function getStatusText(estado) {
    const statusMap = {
        'DISPONIBLE': 'Disponible',
        'EN_USO': 'En Uso',
        'ASIGNADO': 'Asignado',
        'MANTENIMIENTO': 'Mantenimiento',
        'FUERA_SERVICIO': 'Fuera de Servicio'
    };
    return statusMap[estado] || estado;
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function showAddVehicleModal() {
    document.getElementById('addVehicleModal').classList.add('active');
}

function closeAddVehicleModal() {
    document.getElementById('addVehicleModal').classList.remove('active');
    document.getElementById('vehicleForm').reset();
    editingVehicleId = null;
    // Cambiar el título del modal
    document.querySelector('#addVehicleModal .modal-header h2').textContent = 'Agregar Vehículo';
}

async function handleVehicleSubmit(e) {
    e.preventDefault();
    
    const vehicleData = {
        placa: document.getElementById('vehiclePlaca').value.trim(),
        marca: document.getElementById('vehicleMarca').value.trim(),
        modelo: document.getElementById('vehicleModelo').value.trim(),
        anio: parseInt(document.getElementById('vehicleAnio').value),
        tipoMaquinaria: document.getElementById('vehicleTipoMaquinaria').value,
        estadoOperativo: document.getElementById('vehicleEstado').value
    };
    
    // Agregar campos opcionales solo si tienen valor
    const capacidad = document.getElementById('vehicleCapacidad').value.trim();
    if (capacidad) {
        vehicleData.capacidadTanque = parseFloat(capacidad);
    }
    
    const consumo = document.getElementById('vehicleConsumo').value.trim();
    if (consumo) {
        vehicleData.consumoPromedio = parseFloat(consumo);
    }
    
    if (!editingVehicleId) {
        vehicleData.activo = true;
    }
    
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const url = editingVehicleId ? `${API_BASE_URL}/${editingVehicleId}` : API_BASE_URL;
        const method = editingVehicleId ? 'PUT' : 'POST';
        
        console.log(`${method} ${url}`, vehicleData);
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(vehicleData)
        });
        
        if (response.ok) {
            closeAddVehicleModal();
            loadVehicles();
            showNotification('success', 'Éxito', editingVehicleId ? 'Vehículo actualizado correctamente' : 'Vehículo agregado correctamente');
        } else {
            const errorText = await response.text();
            console.error('Error response:', response.status, errorText);
            let errorMessage = 'Error al procesar la solicitud';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = editingVehicleId ? 'Error al actualizar vehículo' : 'Error al agregar vehículo';
            }
            showNotification('error', 'Error', errorMessage);
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('error', 'Error', editingVehicleId ? 'Error al actualizar vehículo' : 'Error al agregar vehículo');
    }
}

function editVehicle(id) {
    const vehicle = vehicles.find(v => v.id === id);
    if (!vehicle) return;
    
    // Establecer el ID del vehículo que se está editando
    editingVehicleId = id;
    
    // Cambiar el título del modal
    document.querySelector('#addVehicleModal .modal-header h2').textContent = 'Editar Vehículo';
    
    // Populate form and show modal
    document.getElementById('vehiclePlaca').value = vehicle.placa || '';
    document.getElementById('vehicleMarca').value = vehicle.marca || '';
    document.getElementById('vehicleModelo').value = vehicle.modelo || '';
    document.getElementById('vehicleAnio').value = vehicle.anio || '';
    document.getElementById('vehicleTipoMaquinaria').value = vehicle.tipoMaquinaria || '';
    document.getElementById('vehicleEstado').value = vehicle.estadoOperativo || 'DISPONIBLE';
    document.getElementById('vehicleCapacidad').value = vehicle.capacidadTanque || '';
    document.getElementById('vehicleConsumo').value = vehicle.consumoPromedio || '';
    
    showAddVehicleModal();
}

async function deleteVehicle(id) {
    showConfirmationModal(
        'Eliminar Vehículo',
        '¿Estás seguro de eliminar este vehículo? Esta acción no se puede deshacer.',
        async () => {
            await performDeleteVehicle(id);
        }
    );
}

async function performDeleteVehicle(id) {
    
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok || response.status === 204) {
            loadVehicles();
            showNotification('success', 'Éxito', 'Vehículo eliminado correctamente');
        } else {
            const errorText = await response.text();
            console.error('Error response:', response.status, errorText);
            let errorMessage = 'Error al eliminar vehículo';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = `Error al eliminar vehículo: ${response.status}`;
            }
            showNotification('error', 'Error', errorMessage);
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('error', 'Error', 'Error al eliminar vehículo');
    }
}

// Notification functions
function showNotification(type, title, message) {
    const modal = document.getElementById('notificationModal');
    const icon = document.getElementById('notificationIcon');
    const titleEl = document.getElementById('notificationTitle');
    const messageEl = document.getElementById('notificationMessage');
    
    // Remove all icon classes
    icon.className = 'notification-icon';
    
    // Set icon based on type
    if (type === 'success') {
        icon.classList.add('success');
        icon.innerHTML = '<i class="fas fa-check-circle"></i>';
    } else if (type === 'error') {
        icon.classList.add('error');
        icon.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
    } else if (type === 'warning') {
        icon.classList.add('warning');
        icon.innerHTML = '<i class="fas fa-exclamation-triangle"></i>';
    } else {
        icon.classList.add('info');
        icon.innerHTML = '<i class="fas fa-info-circle"></i>';
    }
    
    titleEl.textContent = title;
    messageEl.textContent = message;
    modal.classList.add('active');
    
    // Auto close after 3 seconds for success messages
    if (type === 'success') {
        setTimeout(() => {
            closeNotification();
        }, 3000);
    }
}

function closeNotification() {
    document.getElementById('notificationModal').classList.remove('active');
}

// Funciones de asignación de conductores
function assignDriver(vehicleId) {
    const vehicle = vehicles.find(v => v.id === vehicleId);
    if (!vehicle) return;
    
    assigningVehicleId = vehicleId;
    
    // Llenar información del vehículo
    document.getElementById('assignVehicleInfo').value = `${vehicle.placa} - ${vehicle.marca} ${vehicle.modelo}`;
    
    // Llenar select de conductores
    const select = document.getElementById('assignDriverSelect');
    select.innerHTML = '<option value="">Seleccione un conductor</option>';
    
    // Filtrar conductores disponibles (que no tengan 2 vehículos asignados)
    drivers.forEach(driver => {
        const option = document.createElement('option');
        option.value = driver.id;
        option.textContent = `${driver.nombre} ${driver.apellido} - ${driver.licencia}`;
        select.appendChild(option);
    });
    
    // Cambiar texto del botón
    document.getElementById('assignDriverSubmitBtn').textContent = 'Asignar';
    document.getElementById('assignDriverModalTitle').textContent = 'Asignar Conductor';
    
    // Limpiar formulario
    document.getElementById('assignObservations').value = '';
    
    // Mostrar modal
    document.getElementById('assignDriverModal').classList.add('active');
}

function closeAssignDriverModal() {
    document.getElementById('assignDriverModal').classList.remove('active');
    document.getElementById('assignDriverForm').reset();
    assigningVehicleId = null;
}

async function handleAssignDriverSubmit(e) {
    e.preventDefault();
    
    const choferId = document.getElementById('assignDriverSelect').value;
    if (!choferId) {
        showNotification('warning', 'Advertencia', 'Por favor seleccione un conductor');
        return;
    }
    
    const vehicle = vehicles.find(v => v.id === assigningVehicleId);
    if (!vehicle) {
        showNotification('error', 'Error', 'Vehículo no encontrado');
        return;
    }
    
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        
        const assignmentData = {
            vehicleId: vehicle.id, // Enviar el ID como String (ObjectId de MongoDB)
            choferId: choferId,
            // No enviar fechaAsignacion, el backend usará la fecha actual
            observaciones: document.getElementById('assignObservations').value.trim() || null
        };
        
        const response = await fetch(ASSIGNMENTS_API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(assignmentData)
        });
        
        if (response.ok) {
            closeAssignDriverModal();
            // Recargar conductores primero para tener los nombres actualizados
            await loadDrivers();
            await loadVehicleAssignments();
            await loadVehicles();
            showNotification('success', 'Éxito', 'Conductor asignado correctamente');
        } else {
            const errorText = await response.text();
            console.error('Error response:', response.status, errorText);
            let errorMessage = 'Error al asignar conductor';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = `Error al asignar conductor: ${response.status}`;
            }
            showNotification('error', 'Error', errorMessage);
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('error', 'Error', 'Error al asignar conductor');
    }
}

async function unassignVehicle(vehicleId) {
    const vehicle = vehicles.find(v => v.id === vehicleId);
    if (!vehicle) return;
    
    showConfirmationModal(
        'Desasignar Conductor',
        `¿Estás seguro de desasignar el conductor del vehículo ${vehicle.placa}?`,
        async () => {
            await performUnassign(vehicleId);
        }
    );
}

async function performUnassign(vehicleId) {
    
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${ASSIGNMENTS_API_URL}/vehiculo/${vehicleId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok || response.status === 204) {
            // Recargar conductores primero para tener los nombres actualizados
            await loadDrivers();
            await loadVehicleAssignments();
            await loadVehicles();
            showNotification('success', 'Éxito', 'Conductor desasignado correctamente');
        } else {
            const errorText = await response.text();
            console.error('Error response:', response.status, errorText);
            let errorMessage = 'Error al desasignar conductor';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = `Error al desasignar conductor: ${response.status}`;
            }
            showNotification('error', 'Error', errorMessage);
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('error', 'Error', 'Error al desasignar conductor');
    }
}

function logout() {
    showConfirmationModal(
        'Cerrar Sesión',
        '¿Estás seguro de cerrar sesión?',
        () => {
            // Limpiar tokens primero
            localStorage.removeItem('authToken');
            localStorage.removeItem('currentUser');
            sessionStorage.removeItem('authToken');
            sessionStorage.removeItem('currentUser');
            // Redirigir con parámetro de logout para evitar redirección automática
            window.location.href = 'http://localhost:8085/index.html?logout=true';
        }
    );
}

// Funciones para el modal de confirmación
function showConfirmationModal(title, message, callback) {
    confirmationCallback = callback;
    document.getElementById('confirmationTitle').textContent = title;
    document.getElementById('confirmationMessage').textContent = message;
    document.getElementById('confirmationModal').classList.add('active');
}

function closeConfirmationModal(confirmed) {
    document.getElementById('confirmationModal').classList.remove('active');
    if (confirmed && confirmationCallback) {
        confirmationCallback();
        confirmationCallback = null;
    }
}

// Funciones para el modal de filtros de reportes
function showReportFiltersModal() {
    document.getElementById('reportFiltersModal').classList.add('active');
}

function closeReportFiltersModal() {
    document.getElementById('reportFiltersModal').classList.remove('active');
    document.getElementById('reportFiltersForm').reset();
}

function handleReportFiltersSubmit(e) {
    e.preventDefault();
    const filters = {
        tipoMaquinaria: document.getElementById('reportFilterTipoMaquinaria').value,
        estado: document.getElementById('reportFilterEstado').value,
        soloActivos: document.getElementById('reportFilterSoloActivos').checked
    };
    closeReportFiltersModal();
    generateReport(filters);
}

// Función para generar el reporte en PDF
async function generateReport(filters = {}) {
    // Filtrar vehículos según los filtros
    let filteredVehicles = [...vehicles];
    
    if (filters.tipoMaquinaria) {
        filteredVehicles = filteredVehicles.filter(v => v.tipoMaquinaria === filters.tipoMaquinaria);
    }
    
    if (filters.estado) {
        filteredVehicles = filteredVehicles.filter(v => v.estadoOperativo === filters.estado);
    }
    
    if (filters.soloActivos !== false) {
        filteredVehicles = filteredVehicles.filter(v => v.activo);
    }
    
    if (filteredVehicles.length === 0) {
        showNotification('warning', 'Advertencia', 'No hay vehículos que coincidan con los filtros seleccionados');
        return;
    }
    
    try {
        // Usar jsPDF desde window.jspdf
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF('landscape');
        
        // Configuración de colores
        const primaryColor = [220, 53, 69]; // Rojo principal
        const darkColor = [13, 17, 23];
        const lightGray = [240, 240, 240];
        
        let yPosition = 20;
        const pageWidth = doc.internal.pageSize.width;
        const margin = 15;
        const contentWidth = pageWidth - (margin * 2);
        
        // Encabezado del reporte
        doc.setFillColor(...primaryColor);
        doc.rect(0, 0, pageWidth, 40, 'F');
        
        doc.setTextColor(255, 255, 255);
        doc.setFontSize(24);
        doc.setFont('helvetica', 'bold');
        doc.text('SKT FUEL SYSTEM', margin, 25);
        
        doc.setFontSize(16);
        doc.setFont('helvetica', 'normal');
        doc.text('Reporte de Vehículos', margin, 35);
        
        // Fecha de generación
        doc.setFontSize(10);
        const fechaGeneracion = new Date().toLocaleString('es-ES', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
        doc.text(`Generado el: ${fechaGeneracion}`, pageWidth - margin, 35, { align: 'right' });
        
        yPosition = 50;
        
        // Estadísticas generales
        doc.setTextColor(...darkColor);
        doc.setFontSize(14);
        doc.setFont('helvetica', 'bold');
        doc.text('Resumen Ejecutivo', margin, yPosition);
        yPosition += 10;
        
        doc.setFontSize(10);
        doc.setFont('helvetica', 'normal');
        
        const total = filteredVehicles.length;
        const light = filteredVehicles.filter(v => isLightMachinery(v.tipoMaquinaria)).length;
        const heavy = filteredVehicles.filter(v => isHeavyMachinery(v.tipoMaquinaria)).length;
        const activos = filteredVehicles.filter(v => v.activo && v.estadoOperativo !== 'FUERA_SERVICIO').length;
        const enMantenimiento = filteredVehicles.filter(v => v.estadoOperativo === 'MANTENIMIENTO').length;
        const enUso = filteredVehicles.filter(v => v.estadoOperativo === 'EN_USO' || v.estadoOperativo === 'ASIGNADO').length;
        const disponibles = filteredVehicles.filter(v => v.estadoOperativo === 'DISPONIBLE').length;
        
        const stats = [
            `Total de Vehículos: ${total}`,
            `Maquinaria Liviana: ${light}`,
            `Maquinaria Pesada: ${heavy}`,
            `Vehículos Activos: ${activos}`,
            `Disponibles: ${disponibles}`,
            `En Uso: ${enUso}`,
            `En Mantenimiento: ${enMantenimiento}`
        ];
        
        stats.forEach((stat, index) => {
            if (yPosition > 270) {
                doc.addPage();
                yPosition = 20;
            }
            doc.text(`• ${stat}`, margin + 5, yPosition);
            yPosition += 7;
        });
        
        yPosition += 5;
        
        // Tabla de vehículos
        if (yPosition > 250) {
            doc.addPage();
            yPosition = 20;
        }
        
        doc.setFontSize(14);
        doc.setFont('helvetica', 'bold');
        doc.text('Detalle de Vehículos', margin, yPosition);
        yPosition += 10;
        
        // Encabezado de tabla
        doc.setFillColor(...lightGray);
        doc.rect(margin, yPosition - 5, contentWidth, 8, 'F');
        
        doc.setFontSize(7);
        doc.setFont('helvetica', 'bold');
        doc.setTextColor(...darkColor);
        
        const colWidths = [30, 25, 50, 18, 35, 30, 30, 35];
        const headers = ['Placa', 'Tipo', 'Marca/Modelo', 'Año', 'Capacidad', 'Consumo', 'Estado', 'Conductor'];
        let xPos = margin + 2;
        
        headers.forEach((header, index) => {
            doc.text(header, xPos, yPosition);
            xPos += colWidths[index];
        });
        
        yPosition += 8;
        
        // Datos de vehículos
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(6);
        
        filteredVehicles.forEach((vehicle, index) => {
            // Verificar si necesita nueva página
            if (yPosition > 180) {
                doc.addPage();
                yPosition = 20;
                
                // Reimprimir encabezados
                doc.setFillColor(...lightGray);
                doc.rect(margin, yPosition - 5, contentWidth, 7, 'F');
                doc.setFont('helvetica', 'bold');
                doc.setFontSize(7);
                xPos = margin + 2;
                headers.forEach((header, idx) => {
                    doc.text(header, xPos, yPosition);
                    xPos += colWidths[idx];
                });
                yPosition += 7;
                doc.setFont('helvetica', 'normal');
                doc.setFontSize(6);
            }
            
            // Fila de datos
            const placa = vehicle.placa || 'N/A';
            const tipo = isLightMachinery(vehicle.tipoMaquinaria) ? 'Liviana' : 'Pesada';
            const marcaModelo = `${vehicle.marca || ''} ${vehicle.modelo || ''}`.trim() || 'N/A';
            const anio = vehicle.anio || 'N/A';
            const capacidad = vehicle.capacidadTanque ? `${vehicle.capacidadTanque} L` : 'N/A';
            const consumo = vehicle.consumoPromedio ? `${vehicle.consumoPromedio} L/100km` : 'N/A';
            const estado = getStatusText(vehicle.estadoOperativo);
            const conductor = getAssignedDriverSync(vehicle.id);
            
            xPos = margin + 2;
            const rowData = [
                placa.substring(0, 18),
                tipo.substring(0, 15),
                marcaModelo.substring(0, 28),
                anio.toString().substring(0, 6),
                capacidad.substring(0, 18),
                consumo.substring(0, 18),
                estado.substring(0, 18),
                conductor.substring(0, 20)
            ];
            
            // Alternar color de fondo
            if (index % 2 === 0) {
                doc.setFillColor(250, 250, 250);
                doc.rect(margin, yPosition - 4, contentWidth, 6, 'F');
            }
            
            rowData.forEach((data, idx) => {
                doc.setTextColor(...darkColor);
                doc.text(data, xPos, yPosition);
                xPos += colWidths[idx];
            });
            
            yPosition += 7;
        });
        
        // Pie de página en todas las páginas
        const totalPages = doc.internal.pages.length - 1;
        for (let i = 1; i <= totalPages; i++) {
            doc.setPage(i);
            doc.setFontSize(8);
            doc.setTextColor(128, 128, 128);
            doc.text(
                `Página ${i} de ${totalPages}`,
                pageWidth / 2,
                doc.internal.pageSize.height - 10,
                { align: 'center' }
            );
            
            // Firma/Información de contacto
            doc.setFontSize(7);
            doc.text(
                'Sistema de Gestión de Combustible SKT',
                margin,
                doc.internal.pageSize.height - 10
            );
        }
        
        // Guardar el PDF
        const fechaArchivo = new Date().toISOString().split('T')[0];
        const nombreArchivo = `Reporte_Vehiculos_${fechaArchivo}.pdf`;
        doc.save(nombreArchivo);
        
        showNotification('success', 'Éxito', 'Reporte generado exitosamente');
        
    } catch (error) {
        console.error('Error generando reporte:', error);
        showNotification('error', 'Error', 'Error al generar el reporte: ' + error.message);
    }
}