// Routes Management Script
const API_BASE_URL = 'http://localhost:8083/api/v1/routes';
const VEHICLES_API_URL = 'http://localhost:8082/api/v1/vehicles';
const DRIVERS_API_URL = 'http://localhost:8081/api/v1/drivers';

let routes = [];
let vehicles = [];
let drivers = [];
let editingRouteId = null;
let confirmationCallback = null;

document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    loadRoutes();
    loadVehicles();
    loadDrivers();
    setupEventListeners();
});

function checkAuth() {
    const urlParams = new URLSearchParams(window.location.search);
    const tokenFromUrl = urlParams.get('token');
    const userFromUrl = urlParams.get('user');
    
    if (tokenFromUrl && userFromUrl) {
        localStorage.setItem('authToken', tokenFromUrl);
        localStorage.setItem('currentUser', userFromUrl);
        sessionStorage.setItem('authToken', tokenFromUrl);
        sessionStorage.setItem('currentUser', userFromUrl);
        window.history.replaceState({}, document.title, window.location.pathname);
    }
    
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    if (!token) {
        window.location.href = 'http://localhost:8085/';
        return;
    }
    
    if (!localStorage.getItem('authToken') && sessionStorage.getItem('authToken')) {
        localStorage.setItem('authToken', sessionStorage.getItem('authToken'));
        if (sessionStorage.getItem('currentUser')) {
            localStorage.setItem('currentUser', sessionStorage.getItem('currentUser'));
        }
    }
}

function setupEventListeners() {
    const searchInput = document.getElementById('searchInput');
    const routeSearchInput = document.getElementById('routeSearchInput');
    const routeForm = document.getElementById('routeForm');
    const routeChoferSelect = document.getElementById('routeChofer');
    
    // Interceptar clics en enlaces externos para compartir token
    document.querySelectorAll('a[href^="http://localhost:8081"], a[href^="http://localhost:8082"], a[href^="http://localhost:8085"]').forEach(link => {
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
    
    if (searchInput) {
        searchInput.addEventListener('input', filterRoutes);
    }
    
    if (routeSearchInput) {
        routeSearchInput.addEventListener('input', filterRoutes);
    }
    
    if (routeForm) {
        routeForm.addEventListener('submit', handleRouteSubmit);
    }
    
    // Listener para cuando se seleccione un conductor (auto-completar vehículo y tipo de maquinaria)
    if (routeChoferSelect) {
        routeChoferSelect.addEventListener('change', async function() {
            const choferId = this.value;
            if (choferId) {
                await loadDriverAssignments(choferId);
            } else {
                // Limpiar campos si no hay conductor seleccionado
                document.getElementById('routeVehiculo').value = '';
                document.getElementById('routeTipoMaquinaria').value = '';
                document.getElementById('routeTipoMaquinaria').disabled = true;
                hideVehicleInfo();
            }
        });
    }
}

async function loadRoutes() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(API_BASE_URL, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            routes = Array.isArray(data) ? data : [];
            updateMetrics();
            renderRoutes();
        } else {
            console.error('Error loading routes');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function loadVehicles() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(VEHICLES_API_URL, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            vehicles = Array.isArray(data) ? data : [];
            populateVehicleSelect();
        } else {
            console.error('Error loading vehicles');
        }
    } catch (error) {
        console.error('Error loading vehicles:', error);
    }
}

async function loadDrivers() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        // Usar el nuevo endpoint que filtra conductores sin rutas activas
        const response = await fetch(`${API_BASE_URL}/available-drivers`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            drivers = Array.isArray(data) ? data : [];
            populateDriverSelect();
        } else {
            console.error('Error loading available drivers');
            // Fallback: cargar todos los conductores disponibles del drivers-service
            try {
                const fallbackResponse = await fetch(`${DRIVERS_API_URL}/disponibles`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                if (fallbackResponse.ok) {
                    const fallbackData = await fallbackResponse.json();
                    drivers = Array.isArray(fallbackData) ? fallbackData : [];
                    populateDriverSelect();
                }
            } catch (fallbackError) {
                console.error('Error loading drivers from fallback:', fallbackError);
            }
        }
    } catch (error) {
        console.error('Error loading drivers:', error);
    }
}

function populateVehicleSelect() {
    const select = document.getElementById('routeVehiculo');
    if (!select) return;
    
    select.innerHTML = '<option value="">Seleccione un vehículo</option>';
    vehicles.forEach(vehicle => {
        const option = document.createElement('option');
        option.value = vehicle.id;
        option.textContent = `${vehicle.placa} - ${vehicle.marca} ${vehicle.modelo}`;
        select.appendChild(option);
    });
}

function populateDriverSelect() {
    const select = document.getElementById('routeChofer');
    if (!select) return;
    
    select.innerHTML = '<option value="">Seleccione un conductor</option>';
    drivers.forEach(driver => {
        const option = document.createElement('option');
        option.value = driver.id;
        option.textContent = `${driver.nombre} ${driver.apellido}`;
        select.appendChild(option);
    });
}

async function loadDriverAssignments(choferId) {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${API_BASE_URL}/driver/${choferId}/assignments`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const asignaciones = await response.json();
            if (asignaciones && asignaciones.length > 0) {
                // Tomar la primera asignación activa
                const asignacion = asignaciones[0];
                const vehiculoId = asignacion.vehicleId;
                const tipoMaquinariaVehiculo = asignacion.tipoMaquinariaVehiculo;
                
                if (vehiculoId) {
                    // Establecer el vehículo (aunque esté oculto)
                    const vehiculoField = document.getElementById('routeVehiculo');
                    vehiculoField.value = vehiculoId;
                    vehiculoField.disabled = true;
                    
                    // Cargar información completa del vehículo
                    await loadAndDisplayVehicleInfo(vehiculoId, tipoMaquinariaVehiculo);
                } else {
                    // Ocultar información del vehículo
                    hideVehicleInfo();
                    showNotification('warning', 'Advertencia', 
                        'El conductor seleccionado no tiene un vehículo asignado activo');
                    // Limpiar campos
                    document.getElementById('routeVehiculo').value = '';
                    document.getElementById('routeTipoMaquinaria').value = '';
                    document.getElementById('routeTipoMaquinaria').disabled = false;
                }
            } else {
                // Ocultar información del vehículo
                hideVehicleInfo();
                showNotification('warning', 'Advertencia', 
                    'El conductor seleccionado no tiene vehículos asignados activos. Debe asignar un vehículo al conductor primero.');
                // Limpiar campos
                document.getElementById('routeVehiculo').value = '';
                document.getElementById('routeTipoMaquinaria').value = '';
                document.getElementById('routeTipoMaquinaria').disabled = false;
            }
        } else {
            console.error('Error cargando asignaciones del conductor:', response.status);
            hideVehicleInfo();
            showNotification('error', 'Error', 'No se pudieron cargar las asignaciones del conductor');
            // Limpiar campos
            document.getElementById('routeVehiculo').value = '';
            document.getElementById('routeTipoMaquinaria').value = '';
            document.getElementById('routeTipoMaquinaria').disabled = false;
        }
    } catch (error) {
        console.error('Error cargando asignaciones:', error);
        hideVehicleInfo();
        showNotification('error', 'Error', 'Error de red al obtener asignaciones del conductor');
        // Limpiar campos
        document.getElementById('routeVehiculo').value = '';
        document.getElementById('routeTipoMaquinaria').value = '';
        document.getElementById('routeTipoMaquinaria').disabled = false;
    }
}

async function loadAndDisplayVehicleInfo(vehiculoId, tipoMaquinariaVehiculo) {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${VEHICLES_API_URL}/${vehiculoId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const vehiculo = await response.json();
            
            // Mostrar información del vehículo
            document.getElementById('vehiclePlaca').textContent = vehiculo.placa || 'N/A';
            document.getElementById('vehicleMarca').textContent = vehiculo.marca || 'N/A';
            document.getElementById('vehicleModelo').textContent = vehiculo.modelo || 'N/A';
            document.getElementById('vehicleAnio').textContent = vehiculo.anio || 'N/A';
            document.getElementById('vehicleEstado').textContent = vehiculo.estadoOperativo || 'N/A';
            document.getElementById('vehicleCapacidad').textContent = vehiculo.capacidadTanque || 'N/A';
            
            // Mostrar el contenedor de información del vehículo
            document.getElementById('vehicleInfoContainer').style.display = 'block';
            
            // Establecer el tipo de maquinaria
            if (tipoMaquinariaVehiculo || vehiculo.tipoMaquinaria) {
                const tipoMaq = tipoMaquinariaVehiculo || vehiculo.tipoMaquinaria;
                document.getElementById('routeTipoMaquinaria').value = tipoMaq;
                document.getElementById('routeTipoMaquinaria').disabled = true;
            }
            
            showNotification('success', 'Vehículo cargado', 
                `Vehículo asignado: ${vehiculo.placa} - ${vehiculo.marca} ${vehiculo.modelo}`);
        } else {
            // Si no se puede cargar el vehículo completo, mostrar información básica de la asignación
            console.warn('No se pudo cargar información completa del vehículo, usando datos de asignación');
            hideVehicleInfo();
            showNotification('warning', 'Advertencia', 
                'Se asignó el vehículo pero no se pudieron cargar todos los detalles');
        }
    } catch (error) {
        console.error('Error cargando información del vehículo:', error);
        hideVehicleInfo();
        showNotification('warning', 'Advertencia', 
            'Se asignó el vehículo pero no se pudieron cargar todos los detalles');
    }
}

function hideVehicleInfo() {
    document.getElementById('vehicleInfoContainer').style.display = 'none';
    document.getElementById('vehiclePlaca').textContent = '-';
    document.getElementById('vehicleMarca').textContent = '-';
    document.getElementById('vehicleModelo').textContent = '-';
    document.getElementById('vehicleAnio').textContent = '-';
    document.getElementById('vehicleEstado').textContent = '-';
    document.getElementById('vehicleCapacidad').textContent = '-';
}

async function loadVehicleById(vehiculoId) {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${VEHICLES_API_URL}/${vehiculoId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const vehiculo = await response.json();
            // Agregar a la lista de vehículos si no está
            if (!vehicles.find(v => v.id === vehiculo.id)) {
                vehicles.push(vehiculo);
                populateVehicleSelect();
            }
            
            // Establecer el vehículo y tipo de maquinaria
            document.getElementById('routeVehiculo').value = vehiculo.id;
            // Bloquear el campo de vehículo
            document.getElementById('routeVehiculo').disabled = true;
            
            if (vehiculo.tipoMaquinaria) {
                document.getElementById('routeTipoMaquinaria').value = vehiculo.tipoMaquinaria;
                // Bloquear el campo de tipo de maquinaria
                document.getElementById('routeTipoMaquinaria').disabled = true;
            }
            
            showNotification('info', 'Información', 
                `Vehículo asignado cargado: ${vehiculo.placa} - ${vehiculo.marca} ${vehiculo.modelo}. Los campos están bloqueados.`);
        }
    } catch (error) {
        console.error('Error cargando vehículo:', error);
    }
}

function updateMetrics() {
    const totalRoutes = routes.length;
    const activeRoutes = routes.filter(r => r.estado === 'EN_CURSO').length;
    const completedToday = routes.filter(r => {
        if (r.estado !== 'COMPLETADA') return false;
        if (!r.fechaFin) return false;
        const fechaFin = new Date(r.fechaFin);
        const hoy = new Date();
        return fechaFin.toDateString() === hoy.toDateString();
    }).length;
    
    const totalDistance = routes.reduce((sum, r) => sum + (r.distanciaKm || 0), 0);
    
    document.getElementById('totalRoutes').textContent = totalRoutes;
    document.getElementById('activeRoutes').textContent = activeRoutes;
    document.getElementById('completedToday').textContent = completedToday;
    document.getElementById('totalDistance').textContent = Math.round(totalDistance);
    
    // Actualizar resumen de combustible
    const totalFuel = routes.reduce((sum, r) => sum + (r.consumoEstimadoLitros || 0), 0);
    const activeFuel = routes
        .filter(r => r.estado === 'EN_CURSO')
        .reduce((sum, r) => sum + (r.consumoEstimadoLitros || 0), 0);
    const pendingFuel = routes
        .filter(r => r.estado === 'PENDIENTE')
        .reduce((sum, r) => sum + (r.consumoEstimadoLitros || 0), 0);
    
    document.getElementById('totalFuelEstimated').textContent = Math.round(totalFuel);
    document.getElementById('activeRoutesFuel').textContent = Math.round(activeFuel) + ' L';
    document.getElementById('pendingRoutesFuel').textContent = Math.round(pendingFuel) + ' L';
    
    // Actualizar estadísticas de distancia
    const inProgressDistance = routes
        .filter(r => r.estado === 'EN_CURSO')
        .reduce((sum, r) => sum + (r.distanciaKm || 0), 0);
    const completedDistance = routes
        .filter(r => r.estado === 'COMPLETADA')
        .reduce((sum, r) => sum + (r.distanciaKm || 0), 0);
    
    document.getElementById('totalDistanceStats').textContent = Math.round(totalDistance);
    document.getElementById('inProgressDistance').textContent = Math.round(inProgressDistance) + ' km';
    document.getElementById('completedDistance').textContent = Math.round(completedDistance) + ' km';
}

function renderRoutes() {
    const tbody = document.getElementById('routesTableBody');
    if (!tbody) return;
    
    if (routes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align: center; padding: 40px; color: var(--text-secondary);">No hay rutas registradas</td></tr>';
        return;
    }
    
    tbody.innerHTML = routes.map(route => {
        const estadoClass = getEstadoClass(route.estado);
        const estadoText = getEstadoText(route.estado);
        const conductor = route.nombreChofer && route.apellidoChofer 
            ? `${route.nombreChofer} ${route.apellidoChofer}` 
            : 'Sin asignar';
        const vehiculo = route.placaVehiculo || 'Sin asignar';
        const combustible = route.consumoEstimadoLitros 
            ? Math.round(route.consumoEstimadoLitros) + ' L' 
            : 'N/A';
        const tiempoEst = route.duracionEstimadaHoras 
            ? formatTime(route.duracionEstimadaHoras) 
            : 'N/A';
        const horaInicio = route.horaInicio || 'N/A';
        
        return `
            <tr>
                <td class="route-code">${route.codigo || 'N/A'}</td>
                <td>
                    <div>
                        <div style="font-weight: 600;">${route.nombreRuta || 'N/A'}</div>
                        ${horaInicio !== 'N/A' ? `<div style="font-size: 12px; color: var(--text-secondary);">Inicio: ${horaInicio}</div>` : ''}
                    </div>
                </td>
                <td>${route.origen || 'N/A'} - ${route.destino || 'N/A'}</td>
                <td>${route.distanciaKm || 0} km</td>
                <td>${tiempoEst}</td>
                <td>${conductor}</td>
                <td>${vehiculo}</td>
                <td>${combustible}</td>
                <td>
                    <span class="status-badge ${estadoClass}">${estadoText}</span>
                </td>
                <td>
                    <div class="table-actions">
                        <button class="action-btn edit" onclick="editRoute('${route.id}')" title="Editar">
                            <i class="fas fa-edit"></i>
                        </button>
                        ${route.estado === 'PENDIENTE' 
                            ? `<button class="action-btn start" onclick="startRoute('${route.id}')" title="Iniciar">
                                <i class="fas fa-play"></i>
                            </button>`
                            : ''
                        }
                        ${route.estado === 'EN_CURSO' 
                            ? `<button class="action-btn complete" onclick="completeRoute('${route.id}')" title="Completar">
                                <i class="fas fa-check"></i>
                            </button>`
                            : ''
                        }
                        ${route.estado !== 'COMPLETADA' && route.estado !== 'CANCELADA'
                            ? `<button class="action-btn cancel" onclick="cancelRoute('${route.id}')" title="Cancelar">
                                <i class="fas fa-times"></i>
                            </button>`
                            : ''
                        }
                        <button class="action-btn delete" onclick="deleteRoute('${route.id}')" title="Eliminar">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function getEstadoClass(estado) {
    const estadoMap = {
        'PENDIENTE': 'pendiente',
        'EN_CURSO': 'en-curso',
        'COMPLETADA': 'completada',
        'CANCELADA': 'cancelada'
    };
    return estadoMap[estado] || 'pendiente';
}

function getEstadoText(estado) {
    const estadoMap = {
        'PENDIENTE': 'Pendiente',
        'EN_CURSO': 'En Curso',
        'COMPLETADA': 'Completada',
        'CANCELADA': 'Cancelada'
    };
    return estadoMap[estado] || estado;
}

function formatTime(hours) {
    const h = Math.floor(hours);
    const m = Math.round((hours - h) * 60);
    return `${h}h ${m}min`;
}

function filterRoutes() {
    const searchTerm = (document.getElementById('searchInput')?.value || '').toLowerCase();
    const routeSearchTerm = (document.getElementById('routeSearchInput')?.value || '').toLowerCase();
    const term = searchTerm || routeSearchTerm;
    
    if (!term) {
        renderRoutes();
        return;
    }
    
    const filtered = routes.filter(route => 
        route.codigo?.toLowerCase().includes(term) ||
        route.nombreRuta?.toLowerCase().includes(term) ||
        route.origen?.toLowerCase().includes(term) ||
        route.destino?.toLowerCase().includes(term) ||
        route.nombreChofer?.toLowerCase().includes(term) ||
        route.placaVehiculo?.toLowerCase().includes(term)
    );
    
    const tbody = document.getElementById('routesTableBody');
    if (!tbody) return;
    
    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align: center; padding: 40px; color: var(--text-secondary);">No se encontraron rutas</td></tr>';
        return;
    }
    
    tbody.innerHTML = filtered.map(route => {
        const estadoClass = getEstadoClass(route.estado);
        const estadoText = getEstadoText(route.estado);
        const conductor = route.nombreChofer && route.apellidoChofer 
            ? `${route.nombreChofer} ${route.apellidoChofer}` 
            : 'Sin asignar';
        const vehiculo = route.placaVehiculo || 'Sin asignar';
        const combustible = route.consumoEstimadoLitros 
            ? Math.round(route.consumoEstimadoLitros) + ' L' 
            : 'N/A';
        const tiempoEst = route.duracionEstimadaHoras 
            ? formatTime(route.duracionEstimadaHoras) 
            : 'N/A';
        const horaInicio = route.horaInicio || 'N/A';
        
        return `
            <tr>
                <td class="route-code">${route.codigo || 'N/A'}</td>
                <td>
                    <div>
                        <div style="font-weight: 600;">${route.nombreRuta || 'N/A'}</div>
                        ${horaInicio !== 'N/A' ? `<div style="font-size: 12px; color: var(--text-secondary);">Inicio: ${horaInicio}</div>` : ''}
                    </div>
                </td>
                <td>${route.origen || 'N/A'} - ${route.destino || 'N/A'}</td>
                <td>${route.distanciaKm || 0} km</td>
                <td>${tiempoEst}</td>
                <td>${conductor}</td>
                <td>${vehiculo}</td>
                <td>${combustible}</td>
                <td>
                    <span class="status-badge ${estadoClass}">${estadoText}</span>
                </td>
                <td>
                    <div class="table-actions">
                        <button class="action-btn edit" onclick="editRoute('${route.id}')" title="Editar">
                            <i class="fas fa-edit"></i>
                        </button>
                        ${route.estado === 'PENDIENTE' 
                            ? `<button class="action-btn start" onclick="startRoute('${route.id}')" title="Iniciar">
                                <i class="fas fa-play"></i>
                            </button>`
                            : ''
                        }
                        ${route.estado === 'EN_CURSO' 
                            ? `<button class="action-btn complete" onclick="completeRoute('${route.id}')" title="Completar">
                                <i class="fas fa-check"></i>
                            </button>`
                            : ''
                        }
                        ${route.estado !== 'COMPLETADA' && route.estado !== 'CANCELADA'
                            ? `<button class="action-btn cancel" onclick="cancelRoute('${route.id}')" title="Cancelar">
                                <i class="fas fa-times"></i>
                            </button>`
                            : ''
                        }
                        <button class="action-btn delete" onclick="deleteRoute('${route.id}')" title="Eliminar">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function showAddRouteModal() {
    editingRouteId = null;
    document.getElementById('routeModalTitle').textContent = 'Nueva Ruta';
    document.getElementById('routeForm').reset();
    
    // Ocultar y deshabilitar el campo de vehículo (se carga automáticamente)
    const vehiculoField = document.getElementById('routeVehiculo');
    if (vehiculoField) {
        vehiculoField.value = '';
        vehiculoField.disabled = true;
        vehiculoField.parentElement.style.display = 'none';
    }
    
    // Limpiar y habilitar campos
    document.getElementById('routeTipoMaquinaria').value = '';
    document.getElementById('routeTipoMaquinaria').disabled = true;
    document.getElementById('routeChofer').value = '';
    document.getElementById('routeChofer').disabled = false;
    
    // Ocultar información del vehículo
    hideVehicleInfo();
    
    document.getElementById('routeModal').classList.add('active');
}

function closeRouteModal() {
    document.getElementById('routeModal').classList.remove('active');
    editingRouteId = null;
    document.getElementById('routeForm').reset();
    
    // Limpiar y resetear campos
    const vehiculoField = document.getElementById('routeVehiculo');
    if (vehiculoField) {
        vehiculoField.value = '';
        vehiculoField.disabled = true;
        vehiculoField.parentElement.style.display = 'none';
    }
    document.getElementById('routeTipoMaquinaria').value = '';
    document.getElementById('routeTipoMaquinaria').disabled = true;
    document.getElementById('routeChofer').value = '';
    document.getElementById('routeChofer').disabled = false;
    
    // Ocultar información del vehículo
    hideVehicleInfo();
}

async function handleRouteSubmit(e) {
    e.preventDefault();
    
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    const formData = {
        nombreRuta: document.getElementById('routeNombre').value,
        origen: document.getElementById('routeOrigen').value,
        destino: document.getElementById('routeDestino').value,
        distanciaKm: parseFloat(document.getElementById('routeDistancia').value),
        horaInicio: document.getElementById('routeHoraInicio').value || null,
        vehiculoId: document.getElementById('routeVehiculo').value || null,
        choferId: document.getElementById('routeChofer').value || null,
        tipoMaquinaria: document.getElementById('routeTipoMaquinaria').value || null,
        observaciones: document.getElementById('routeObservaciones').value || null
    };
    
    try {
        let response;
        if (editingRouteId) {
            // Actualizar
            response = await fetch(`${API_BASE_URL}/${editingRouteId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(formData)
            });
        } else {
            // Crear
            response = await fetch(API_BASE_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(formData)
            });
        }
        
        if (response.ok) {
            closeRouteModal();
            await loadRoutes();
            showNotification('success', 'Éxito', editingRouteId ? 'Ruta actualizada correctamente' : 'Ruta creada correctamente');
        } else {
            const errorText = await response.text();
            let errorMessage = editingRouteId ? 'Error al actualizar ruta' : 'Error al crear ruta';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = `${errorMessage}: ${response.status}`;
            }
            showNotification('error', 'Error', errorMessage);
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('error', 'Error', editingRouteId ? 'Error al actualizar ruta' : 'Error al crear ruta');
    }
}

function editRoute(id) {
    const route = routes.find(r => r.id === id);
    if (!route) return;
    
    editingRouteId = id;
    document.getElementById('routeModalTitle').textContent = 'Editar Ruta';
    document.getElementById('routeNombre').value = route.nombreRuta || '';
    document.getElementById('routeOrigen').value = route.origen || '';
    document.getElementById('routeDestino').value = route.destino || '';
    document.getElementById('routeDistancia').value = route.distanciaKm || '';
    document.getElementById('routeHoraInicio').value = route.horaInicio || '';
    
    // Ocultar y deshabilitar el campo de vehículo (se carga automáticamente)
    const vehiculoField = document.getElementById('routeVehiculo');
    if (vehiculoField) {
        vehiculoField.value = route.vehiculoId || '';
        vehiculoField.disabled = true;
        vehiculoField.parentElement.style.display = 'none';
    }
    
    document.getElementById('routeChofer').value = route.choferId || '';
    document.getElementById('routeTipoMaquinaria').value = route.tipoMaquinaria || '';
    document.getElementById('routeTipoMaquinaria').disabled = true; // Bloquear tipo de maquinaria al editar
    document.getElementById('routeObservaciones').value = route.observaciones || '';
    
    // Bloquear el chofer al editar (no se puede cambiar)
    document.getElementById('routeChofer').disabled = true;
    
    document.getElementById('routeModal').classList.add('active');
}

async function startRoute(id) {
    showConfirmationModal(
        'Iniciar Ruta',
        '¿Estás seguro de iniciar esta ruta?',
        async () => {
            await performStartRoute(id);
        }
    );
}

async function performStartRoute(id) {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${API_BASE_URL}/${id}/start`, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            await loadRoutes();
            showNotification('success', 'Éxito', 'Ruta iniciada correctamente');
        } else {
            const errorText = await response.text();
            let errorMessage = 'Error al iniciar ruta';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = `${errorMessage}: ${response.status}`;
            }
            showNotification('error', 'Error', errorMessage);
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('error', 'Error', 'Error al iniciar ruta');
    }
}

async function completeRoute(id) {
    showConfirmationModal(
        'Completar Ruta',
        '¿Estás seguro de completar esta ruta?',
        async () => {
            await performCompleteRoute(id);
        }
    );
}

async function performCompleteRoute(id) {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${API_BASE_URL}/${id}/complete`, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            await loadRoutes();
            showNotification('success', 'Éxito', 'Ruta completada correctamente');
        } else {
            const errorText = await response.text();
            let errorMessage = 'Error al completar ruta';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = `${errorMessage}: ${response.status}`;
            }
            showNotification('error', 'Error', errorMessage);
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('error', 'Error', 'Error al completar ruta');
    }
}

async function cancelRoute(id) {
    showConfirmationModal(
        'Cancelar Ruta',
        '¿Estás seguro de cancelar esta ruta?',
        async () => {
            await performCancelRoute(id);
        }
    );
}

async function performCancelRoute(id) {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${API_BASE_URL}/${id}/cancel`, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            await loadRoutes();
            showNotification('success', 'Éxito', 'Ruta cancelada correctamente');
        } else {
            const errorText = await response.text();
            let errorMessage = 'Error al cancelar ruta';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = `${errorMessage}: ${response.status}`;
            }
            showNotification('error', 'Error', errorMessage);
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('error', 'Error', 'Error al cancelar ruta');
    }
}

async function deleteRoute(id) {
    showConfirmationModal(
        'Eliminar Ruta',
        '¿Estás seguro de eliminar esta ruta? Esta acción no se puede deshacer.',
        async () => {
            await performDeleteRoute(id);
        }
    );
}

async function performDeleteRoute(id) {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok || response.status === 204) {
            await loadRoutes();
            showNotification('success', 'Éxito', 'Ruta eliminada correctamente');
        } else {
            const errorText = await response.text();
            let errorMessage = 'Error al eliminar ruta';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = `${errorMessage}: ${response.status}`;
            }
            showNotification('error', 'Error', errorMessage);
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('error', 'Error', 'Error al eliminar ruta');
    }
}

function showNotification(type, title, message) {
    const modal = document.getElementById('notificationModal');
    const icon = document.getElementById('notificationIcon');
    const titleEl = document.getElementById('notificationTitle');
    const messageEl = document.getElementById('notificationMessage');
    
    icon.className = 'notification-icon';
    icon.innerHTML = '';
    
    switch(type) {
        case 'success':
            icon.classList.add('success');
            icon.innerHTML = '<i class="fas fa-check-circle"></i>';
            break;
        case 'error':
            icon.classList.add('error');
            icon.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
            break;
        case 'warning':
            icon.classList.add('warning');
            icon.innerHTML = '<i class="fas fa-exclamation-triangle"></i>';
            break;
        case 'info':
            icon.classList.add('info');
            icon.innerHTML = '<i class="fas fa-info-circle"></i>';
            break;
    }
    
    titleEl.textContent = title;
    messageEl.textContent = message;
    modal.classList.add('active');
}

function closeNotificationModal() {
    document.getElementById('notificationModal').classList.remove('active');
}

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

function logout() {
    showConfirmationModal(
        'Cerrar Sesión',
        '¿Estás seguro de cerrar sesión?',
        () => {
            localStorage.removeItem('authToken');
            localStorage.removeItem('currentUser');
            sessionStorage.removeItem('authToken');
            sessionStorage.removeItem('currentUser');
            window.location.href = 'http://localhost:8085/';
        }
    );
}

