// Routes Management Script
const API_BASE_URL = 'http://localhost:8083/api/v1/routes';
const VEHICLES_API_URL = 'http://localhost:8082/api/v1/vehicles';
const DRIVERS_API_URL = 'http://localhost:8081/api/v1/drivers';

let routes = [];
let vehicles = [];
let drivers = [];
let editingRouteId = null;
let confirmationCallback = null;
let currentUserRole = null; // Rol del usuario actual
let currentDriverId = null; // ID del conductor actual (si es CONDUCTOR)

// Variables para Leaflet (mapa gratuito)
let map = null;
let mapMarker = null;
let currentLocationField = null; // 'origen' o 'destino'
let selectedVehicle = null; // Para calcular consumo estimado

document.addEventListener('DOMContentLoaded', async function() {
    await checkAuth();
    loadRoutes();
    loadVehicles();
    // loadDrivers se llamará después de applyRoleBasedUI para que currentDriverId esté disponible
    await applyRoleBasedUI();
    loadDrivers();
    setupEventListeners();
});

async function checkAuth() {
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

async function applyRoleBasedUI() {
    // Ocultar botones según el rol
    const addRouteBtn = document.querySelector('.btn-primary');
    
    // ADMIN, SUPERVISOR y CONDUCTOR pueden crear rutas (CONDUCTOR solo para sí mismo)
    if (currentUserRole !== 'ADMIN' && currentUserRole !== 'SUPERVISOR' && currentUserRole !== 'CONDUCTOR') {
        if (addRouteBtn && addRouteBtn.textContent.includes('Nueva Ruta')) {
            addRouteBtn.style.display = 'none';
        }
    }
    
    // Si es CONDUCTOR, obtener su ID de conductor
    if (currentUserRole === 'CONDUCTOR') {
        await loadCurrentDriverId();
    }
    
    // Re-renderizar la tabla para ocultar botones de acciones
    if (routes.length > 0) {
        renderRoutes();
    }
}

/**
 * Decodifica un token JWT (sin verificar la firma, solo para obtener claims)
 */
function decodeJwtToken(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (error) {
        console.error('Error decodificando token JWT:', error);
        return null;
    }
}

async function loadCurrentDriverId() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        if (!token) {
            console.warn('No se encontró token para cargar conductor');
            return;
        }
        
        let userId = null;
        
        // Primero intentar obtener userId desde auth-service usando el endpoint /me
        try {
            const authResponse = await fetch('http://localhost:8085/api/auth/me', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (authResponse.ok) {
                const userData = await authResponse.json();
                // El userId puede estar en diferentes campos según la respuesta
                userId = userData.id || userData.userId || (userData.user && (userData.user.id || userData.user.userId));
                console.log('UserId obtenido desde auth-service:', userId);
            }
        } catch (error) {
            console.warn('Error obteniendo userId desde auth-service:', error);
        }
        
        // Si no se pudo obtener desde auth-service, intentar del token JWT directamente
        if (!userId) {
            const decodedToken = decodeJwtToken(token);
            if (decodedToken) {
                // El userId puede estar en "sub" o "userId"
                userId = decodedToken.sub || decodedToken.userId;
                // Verificar que no sea un username (si parece ser un ObjectId de MongoDB, es válido)
                if (userId && !userId.match(/^[0-9a-fA-F]{24}$/)) {
                    // Parece ser un username, no un userId
                    console.warn('El valor obtenido parece ser un username, no un userId:', userId);
                    userId = null;
                }
            }
        }
        
        // Si aún no se tiene, intentar del localStorage
        if (!userId) {
            const userData = JSON.parse(localStorage.getItem('currentUser') || sessionStorage.getItem('currentUser') || '{}');
            userId = userData.id || userData.userId;
        }
        
        if (!userId) {
            console.warn('No se pudo obtener userId del token, auth-service o localStorage');
            return;
        }
        
        console.log('UserId final obtenido:', userId);
        
        // Obtener conductor por usuarioId desde drivers-service
        let response = await fetch(`${DRIVERS_API_URL}/by-usuario/${userId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const driver = await response.json();
            currentDriverId = driver.id;
            console.log('Conductor actual cargado por usuarioId:', currentDriverId);
        } else if (response.status === 404) {
            console.warn('No se encontró conductor por usuarioId. Intentando buscar por email...');
            
            // Si no se encontró por usuarioId, intentar buscar por email
            const userData = await fetch('http://localhost:8085/api/auth/me', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }).then(r => r.ok ? r.json() : null);
            
            if (userData && userData.email) {
                const email = encodeURIComponent(userData.email);
                response = await fetch(`${DRIVERS_API_URL}/by-email/${email}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                
                if (response.ok) {
                    const driver = await response.json();
                    currentDriverId = driver.id;
                    console.log('Conductor actual cargado por email:', currentDriverId);
                } else {
                    console.warn('No se encontró conductor asociado al usuario ni por usuarioId ni por email. El usuario puede no tener un conductor vinculado.');
                }
            } else {
                console.warn('No se pudo obtener el email del usuario para buscar conductor.');
            }
        } else {
            console.error('Error obteniendo conductor. Status:', response.status);
            const errorText = await response.text();
            console.error('Error response:', errorText);
        }
    } catch (error) {
        console.error('Error cargando ID del conductor actual:', error);
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
    
    // Listener para cuando se seleccione un conductor (cargar vehículos disponibles)
    if (routeChoferSelect) {
        routeChoferSelect.addEventListener('change', async function() {
            const choferId = this.value;
            const vehiculoField = document.getElementById('routeVehiculo');
            
            if (choferId) {
                // Habilitar el campo de vehículo
                if (vehiculoField) {
                    vehiculoField.disabled = false;
                    vehiculoField.innerHTML = '<option value="">Cargando vehículos...</option>';
                }
                
                // Cargar vehículos asociados al conductor
                await loadDriverAssignments(choferId);
            } else {
                // Limpiar campos si no hay conductor seleccionado
                if (vehiculoField) {
                    vehiculoField.disabled = true;
                    vehiculoField.innerHTML = '<option value="">Primero seleccione un conductor</option>';
                    vehiculoField.value = '';
                }
                document.getElementById('routeTipoMaquinaria').value = '';
                document.getElementById('routeTipoMaquinaria').disabled = true;
                hideVehicleInfo();
            }
        });
    }
    
    // Listener para cuando se seleccione un vehículo (cargar información del vehículo)
    const routeVehiculoSelect = document.getElementById('routeVehiculo');
    if (routeVehiculoSelect) {
        routeVehiculoSelect.addEventListener('change', async function() {
            const vehiculoId = this.value;
            if (vehiculoId) {
                // Buscar el vehículo en la lista global
                let vehiculo = vehicles.find(v => v.id === vehiculoId || v.id === String(vehiculoId));
                
                if (vehiculo) {
                    // Cargar y mostrar información del vehículo
                    await loadAndDisplayVehicleInfo(vehiculo.id, vehiculo.tipoMaquinaria);
                } else {
                    // Si no está en la lista, cargarlo desde la API
                    try {
                        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
                        const response = await fetch(`${VEHICLES_API_URL}/${vehiculoId}`, {
                            headers: {
                                'Authorization': `Bearer ${token}`
                            }
                        });
                        
                        if (response.ok) {
                            vehiculo = await response.json();
                            // Agregar a la lista global si no está
                            if (!vehicles.find(v => v.id === vehiculo.id)) {
                                vehicles.push(vehiculo);
                            }
                            // Cargar y mostrar información del vehículo
                            await loadAndDisplayVehicleInfo(vehiculo.id, vehiculo.tipoMaquinaria);
                        } else {
                            hideVehicleInfo();
                            showNotification('error', 'Error', 'No se pudo cargar la información del vehículo');
                        }
                    } catch (error) {
                        console.error('Error cargando vehículo:', error);
                        hideVehicleInfo();
                        showNotification('error', 'Error', 'Error al cargar información del vehículo');
                    }
                }
            } else {
                // Si no hay vehículo seleccionado, ocultar información
                hideVehicleInfo();
                document.getElementById('routeTipoMaquinaria').value = '';
                document.getElementById('routeTipoMaquinaria').disabled = true;
            }
        });
    }
    
    // Listeners para botones de mapa
    const btnSelectOrigen = document.getElementById('btnSelectOrigen');
    const btnSelectDestino = document.getElementById('btnSelectDestino');
    const btnCalculateDistance = document.getElementById('btnCalculateDistance');
    const btnConfirmLocation = document.getElementById('btnConfirmLocation');
    const btnCancelMap = document.getElementById('btnCancelMap');
    
    if (btnSelectOrigen) {
        btnSelectOrigen.addEventListener('click', () => showMapSelector('origen'));
    }
    if (btnSelectDestino) {
        btnSelectDestino.addEventListener('click', () => showMapSelector('destino'));
    }
    if (btnCalculateDistance) {
        btnCalculateDistance.addEventListener('click', calculateDistance);
    }
    if (btnConfirmLocation) {
        btnConfirmLocation.addEventListener('click', confirmMapLocation);
    }
    if (btnCancelMap) {
        btnCancelMap.addEventListener('click', cancelMapSelection);
    }
    
    // Leaflet se inicializa cuando se abre el selector de mapa
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

// Cargar solo conductores disponibles (sin rutas activas) - para crear nuevas rutas
async function loadDrivers() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        
        // Si es CONDUCTOR, cargar solo su propio conductor
        if (currentUserRole === 'CONDUCTOR' && currentDriverId) {
            try {
                const response = await fetch(`${DRIVERS_API_URL}/${currentDriverId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                if (response.ok) {
                    const driver = await response.json();
                    drivers = [driver];
                    populateDriverSelect();
                    return;
                }
            } catch (error) {
                console.error('Error cargando conductor actual:', error);
            }
        }
        
        // Para otros roles, usar el endpoint que filtra conductores sin rutas activas
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

// Cargar TODOS los conductores (incluyendo los que tienen rutas activas) - para editar rutas
async function loadAllDrivers() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        // Cargar todos los conductores del drivers-service
        const response = await fetch(`${DRIVERS_API_URL}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            // Manejar diferentes formatos de respuesta
            let allDrivers = [];
            if (Array.isArray(data)) {
                allDrivers = data;
            } else if (data && data.drivers && Array.isArray(data.drivers)) {
                allDrivers = data.drivers;
            } else if (data && data.content && Array.isArray(data.content)) {
                allDrivers = data.content;
            }
            
            // Actualizar la lista global de drivers y poblar el select
            drivers = allDrivers;
            populateDriverSelect();
            return allDrivers;
        } else {
            console.error('Error loading all drivers:', response.status);
            return [];
        }
    } catch (error) {
        console.error('Error loading all drivers:', error);
        return [];
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
    
    // Si es CONDUCTOR, solo mostrar su propio nombre y auto-seleccionarlo
    if (currentUserRole === 'CONDUCTOR' && currentDriverId) {
        const driver = drivers.find(d => d.id === currentDriverId);
        if (driver) {
            const option = document.createElement('option');
            option.value = driver.id;
            option.textContent = `${driver.nombre} ${driver.apellido}`;
            option.selected = true;
            select.appendChild(option);
            select.disabled = true; // Deshabilitar el campo para que no pueda cambiar
            return;
        }
    }
    
    // Para otros roles, mostrar todos los conductores
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
        
        const vehiculoField = document.getElementById('routeVehiculo');
        
        if (response.ok) {
            const asignaciones = await response.json();
            console.log('Asignaciones recibidas para conductor', choferId, ':', asignaciones);
            console.log('Estructura de asignaciones:', JSON.stringify(asignaciones, null, 2));
            
            // Limpiar el select
            if (vehiculoField) {
                vehiculoField.innerHTML = '<option value="">Seleccione un vehículo</option>';
            }
            
            if (asignaciones && asignaciones.length > 0) {
                console.log('Procesando', asignaciones.length, 'asignaciones');
                
                // Crear un mapa para evitar duplicados por vehicleId
                const vehiculosMap = new Map();
                
                for (const asignacion of asignaciones) {
                    console.log('Procesando asignación:', asignacion);
                    
                    // Intentar diferentes formas de obtener el ID del vehículo
                    let vid = asignacion.vehicleId;
                    if (!vid && asignacion.vehicle) {
                        vid = asignacion.vehicle.id || asignacion.vehicle.$id || (typeof asignacion.vehicle === 'string' ? asignacion.vehicle : null);
                    }
                    
                    console.log('vehicleId extraído de asignación:', vid);
                    
                    if (vid) {
                        const vehiculoIdStr = String(vid);
                        
                        // Si ya tenemos este vehículo, saltarlo
                        if (vehiculosMap.has(vehiculoIdStr)) {
                            continue;
                        }
                        
                        // Crear objeto vehículo con la información disponible en la asignación
                        const vehiculoInfo = {
                            id: vehiculoIdStr,
                            placa: asignacion.placaVehiculo || null,
                            marca: asignacion.marcaVehiculo || null,
                            modelo: asignacion.modeloVehiculo || null
                        };
                        
                        // Si tenemos información básica, usarla directamente
                        if (vehiculoInfo.placa || vehiculoInfo.marca || vehiculoInfo.modelo) {
                            vehiculosMap.set(vehiculoIdStr, vehiculoInfo);
                            console.log('Vehículo agregado desde asignación:', vehiculoInfo);
                        } else {
                            // Si no tenemos información, intentar cargarlo desde la API
                            try {
                                console.log('Cargando vehículo completo desde API para ID:', vehiculoIdStr);
                                const vehiculoResponse = await fetch(`${VEHICLES_API_URL}/${vehiculoIdStr}`, {
                                    headers: {
                                        'Authorization': `Bearer ${token}`
                                    }
                                });
                                
                                if (vehiculoResponse.ok) {
                                    const vehiculo = await vehiculoResponse.json();
                                    console.log('Vehículo cargado desde API:', vehiculo);
                                    vehiculosMap.set(vehiculoIdStr, vehiculo);
                                    // Agregar a la lista global si no está
                                    if (!vehicles.find(v => String(v.id) === String(vehiculo.id))) {
                                        vehicles.push(vehiculo);
                                    }
                                } else {
                                    console.error(`Error cargando vehículo ${vehiculoIdStr}:`, vehiculoResponse.status);
                                    // Agregar con información mínima
                                    vehiculosMap.set(vehiculoIdStr, { id: vehiculoIdStr, placa: vehiculoIdStr });
                                }
                            } catch (error) {
                                console.error(`Error cargando vehículo ${vehiculoIdStr}:`, error);
                                // Agregar con información mínima
                                vehiculosMap.set(vehiculoIdStr, { id: vehiculoIdStr, placa: vehiculoIdStr });
                            }
                        }
                    }
                }
                
                console.log('Vehículos únicos procesados:', Array.from(vehiculosMap.values()));
                
                // Agregar todos los vehículos al select
                let vehiculosAgregados = 0;
                for (const vehiculo of vehiculosMap.values()) {
                    if (vehiculoField) {
                        const option = document.createElement('option');
                        option.value = vehiculo.id;
                        const texto = `${vehiculo.placa || vehiculo.id} - ${vehiculo.marca || ''} ${vehiculo.modelo || ''}`.trim();
                        option.textContent = texto || vehiculo.id;
                        vehiculoField.appendChild(option);
                        vehiculosAgregados++;
                        console.log('Vehículo agregado al select:', texto);
                    }
                }
                
                console.log('Total de vehículos agregados al select:', vehiculosAgregados);
                
                // Habilitar el campo para que el usuario pueda seleccionar
                if (vehiculoField) {
                    vehiculoField.disabled = false;
                    console.log('Campo de vehículo habilitado. Estado disabled:', vehiculoField.disabled);
                    console.log('Número de opciones en el select:', vehiculoField.options.length);
                    
                    // Forzar visibilidad del campo y su contenedor
                    vehiculoField.style.display = 'block';
                    vehiculoField.style.visibility = 'visible';
                    vehiculoField.style.opacity = '1';
                    
                    const parentGroup = vehiculoField.closest('.form-group');
                    if (parentGroup) {
                        parentGroup.style.display = 'block';
                        parentGroup.style.visibility = 'visible';
                        parentGroup.style.opacity = '1';
                        console.log('Grupo de vehículo forzado a visible. Display:', parentGroup.style.display);
                    }
                    
                    // También verificar el contenedor padre (form-row)
                    const formRow = vehiculoField.closest('.form-row');
                    if (formRow) {
                        formRow.style.display = 'flex';
                        formRow.style.visibility = 'visible';
                        console.log('Form-row forzado a visible');
                    }
                } else {
                    console.error('No se encontró el campo routeVehiculo en el DOM');
                }
                
                // Ocultar información del vehículo hasta que se seleccione uno
                hideVehicleInfo();
                
                // NO seleccionar automáticamente - dejar que el usuario elija
            } else {
                // Si no hay asignaciones, mostrar mensaje
                if (vehiculoField) {
                    vehiculoField.innerHTML = '<option value="">El conductor no tiene vehículos asignados</option>';
                    vehiculoField.disabled = true;
                }
                hideVehicleInfo();
                showNotification('warning', 'Advertencia', 
                    'El conductor seleccionado no tiene vehículos asignados activos. Debe asignar un vehículo al conductor primero.');
            }
        } else {
            console.error('Error cargando asignaciones del conductor:', response.status, response.statusText);
            if (vehiculoField) {
                vehiculoField.innerHTML = '<option value="">Error al cargar vehículos</option>';
                vehiculoField.disabled = true;
            }
            hideVehicleInfo();
            showNotification('error', 'Error', 'No se pudieron cargar las asignaciones del conductor');
        }
    } catch (error) {
        console.error('Error cargando asignaciones:', error);
        const vehiculoField = document.getElementById('routeVehiculo');
        if (vehiculoField) {
            vehiculoField.innerHTML = '<option value="">Error al cargar vehículos</option>';
            vehiculoField.disabled = true;
        }
        hideVehicleInfo();
        showNotification('error', 'Error', 'Error de red al obtener asignaciones del conductor');
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
                        ${(currentUserRole === 'ADMIN' || currentUserRole === 'SUPERVISOR' || (currentUserRole === 'CONDUCTOR' && route.choferId === currentDriverId))
                            ? `<button class="action-btn edit" onclick="editRoute('${route.id}')" title="Editar">
                                <i class="fas fa-edit"></i>
                            </button>`
                            : ''
                        }
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
                            ? (currentUserRole === 'ADMIN' || currentUserRole === 'SUPERVISOR' || (currentUserRole === 'CONDUCTOR' && route.choferId === currentDriverId))
                                ? `<button class="action-btn cancel" onclick="cancelRoute('${route.id}')" title="Cancelar">
                                    <i class="fas fa-times"></i>
                                </button>`
                                : ''
                            : ''
                        }
                        ${(currentUserRole === 'ADMIN' || (currentUserRole === 'CONDUCTOR' && route.choferId === currentDriverId))
                            ? `<button class="action-btn delete" onclick="deleteRoute('${route.id}')" title="Eliminar">
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
                        ${(currentUserRole === 'ADMIN' || currentUserRole === 'SUPERVISOR' || (currentUserRole === 'CONDUCTOR' && route.choferId === currentDriverId))
                            ? `<button class="action-btn edit" onclick="editRoute('${route.id}')" title="Editar">
                                <i class="fas fa-edit"></i>
                            </button>`
                            : ''
                        }
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
                            ? (currentUserRole === 'ADMIN' || currentUserRole === 'SUPERVISOR' || (currentUserRole === 'CONDUCTOR' && route.choferId === currentDriverId))
                                ? `<button class="action-btn cancel" onclick="cancelRoute('${route.id}')" title="Cancelar">
                                    <i class="fas fa-times"></i>
                                </button>`
                                : ''
                            : ''
                        }
                        ${(currentUserRole === 'ADMIN' || (currentUserRole === 'CONDUCTOR' && route.choferId === currentDriverId))
                            ? `<button class="action-btn delete" onclick="deleteRoute('${route.id}')" title="Eliminar">
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

function showAddRouteModal() {
    editingRouteId = null;
    document.getElementById('routeModalTitle').textContent = 'Nueva Ruta';
    document.getElementById('routeForm').reset();
    
    // Limpiar y configurar el campo de vehículo (debe ser visible)
    const vehiculoField = document.getElementById('routeVehiculo');
    if (vehiculoField) {
        vehiculoField.value = '';
        vehiculoField.disabled = true;
        vehiculoField.innerHTML = '<option value="">Primero seleccione un conductor</option>';
        // Asegurar que el campo sea visible
        vehiculoField.style.display = 'block';
        const parentGroup = vehiculoField.closest('.form-group');
        if (parentGroup) {
            parentGroup.style.display = 'block';
            parentGroup.style.visibility = 'visible';
        }
    }
    
    // Limpiar y habilitar campos
    document.getElementById('routeTipoMaquinaria').value = '';
    document.getElementById('routeTipoMaquinaria').disabled = true;
    const choferSelect = document.getElementById('routeChofer');
    if (choferSelect) {
        choferSelect.value = '';
        choferSelect.disabled = false;
    }
    
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
        vehiculoField.innerHTML = '<option value="">Primero seleccione un conductor</option>';
    }
    document.getElementById('routeTipoMaquinaria').value = '';
    document.getElementById('routeTipoMaquinaria').disabled = true;
    
    // Asegurar que el campo de conductor esté habilitado
    const routeChoferSelect = document.getElementById('routeChofer');
    if (routeChoferSelect) {
        routeChoferSelect.disabled = false;
    }
    
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
        duracionEstimadaHoras: parseFloat(document.getElementById('routeDuracion').value) || null,
        horaInicio: document.getElementById('routeHoraInicio').value || null,
        vehiculoId: document.getElementById('routeVehiculo').value || null,
        choferId: document.getElementById('routeChofer').value || null,
        tipoMaquinaria: document.getElementById('routeTipoMaquinaria').value || null,
        observaciones: document.getElementById('routeObservaciones').value || null,
        // Coordenadas del mapa (si están disponibles)
        origenLat: document.getElementById('origenLat')?.value ? parseFloat(document.getElementById('origenLat').value) : null,
        origenLng: document.getElementById('origenLng')?.value ? parseFloat(document.getElementById('origenLng').value) : null,
        destinoLat: document.getElementById('destinoLat')?.value ? parseFloat(document.getElementById('destinoLat').value) : null,
        destinoLng: document.getElementById('destinoLng')?.value ? parseFloat(document.getElementById('destinoLng').value) : null
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

async function editRoute(id) {
    const route = routes.find(r => r.id === id);
    if (!route) return;
    
    editingRouteId = id;
    document.getElementById('routeModalTitle').textContent = 'Editar Ruta';
    document.getElementById('routeNombre').value = route.nombreRuta || '';
    document.getElementById('routeOrigen').value = route.origen || '';
    document.getElementById('routeDestino').value = route.destino || '';
    document.getElementById('routeDistancia').value = route.distanciaKm || '';
    document.getElementById('routeDuracion').value = route.duracionEstimadaHoras || '';
    document.getElementById('routeConsumoEstimado').value = route.consumoEstimadoLitros || '';
    document.getElementById('routeHoraInicio').value = route.horaInicio || '';
    
    // Cargar coordenadas si están disponibles
    if (route.origenLat) {
        document.getElementById('origenLat').value = route.origenLat;
    }
    if (route.origenLng) {
        document.getElementById('origenLng').value = route.origenLng;
    }
    if (route.destinoLat) {
        document.getElementById('destinoLat').value = route.destinoLat;
    }
    if (route.destinoLng) {
        document.getElementById('destinoLng').value = route.destinoLng;
    }
    
    // Ocultar y deshabilitar el campo de vehículo (se carga automáticamente)
    // Cargar TODOS los conductores (incluyendo el que tiene esta ruta activa) para que aparezca en el dropdown
    await loadAllDrivers();
    
    // Establecer el conductor después de cargar todos los conductores
    const choferSelect = document.getElementById('routeChofer');
    if (choferSelect) {
        choferSelect.value = route.choferId || '';
        choferSelect.disabled = false; // Permitir cambiar el conductor al editar
    }
    
    // Cargar vehículos del conductor seleccionado
    if (route.choferId) {
        await loadDriverAssignments(route.choferId);
    }
    
    // Establecer el vehículo después de cargar los vehículos
    const vehiculoField = document.getElementById('routeVehiculo');
    if (vehiculoField && route.vehiculoId) {
        // Esperar un momento para que se carguen las opciones
        setTimeout(() => {
            vehiculoField.value = route.vehiculoId || '';
            vehiculoField.disabled = false; // Permitir cambiar el vehículo al editar
            // Forzar visibilidad
            vehiculoField.style.display = 'block';
            const parentGroup = vehiculoField.closest('.form-group');
            if (parentGroup) {
                parentGroup.style.display = 'block';
            }
        }, 500);
    }
    
    document.getElementById('routeTipoMaquinaria').value = route.tipoMaquinaria || '';
    document.getElementById('routeTipoMaquinaria').disabled = true; // Bloquear tipo de maquinaria al editar
    document.getElementById('routeObservaciones').value = route.observaciones || '';
    
    // Cargar información del vehículo si está asignado
    if (route.vehiculoId) {
        await loadAndDisplayVehicleInfo(route.vehiculoId, route.tipoMaquinaria);
    }
    
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

// Función para generar el reporte en PDF
async function generateReport() {
    if (routes.length === 0) {
        showNotification('warning', 'Advertencia', 'No hay rutas para generar el reporte');
        return;
    }
    
    try {
        // Usar jsPDF desde window.jspdf
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();
        
        // Configuración de colores
        const primaryColor = [220, 53, 69]; // Rojo principal
        const darkColor = [13, 17, 23];
        const lightGray = [240, 240, 240];
        
        let yPosition = 20;
        const pageWidth = doc.internal.pageSize.width;
        const margin = 20;
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
        doc.text('Reporte de Rutas', margin, 35);
        
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
        
        const totalRoutes = routes.length;
        const activeRoutes = routes.filter(r => r.estado === 'EN_CURSO').length;
        const completedRoutes = routes.filter(r => r.estado === 'COMPLETADA').length;
        const pendingRoutes = routes.filter(r => r.estado === 'PENDIENTE').length;
        const cancelledRoutes = routes.filter(r => r.estado === 'CANCELADA').length;
        const totalDistance = routes.reduce((sum, r) => sum + (r.distanciaKm || 0), 0);
        const totalFuel = routes.reduce((sum, r) => sum + (r.consumoEstimadoLitros || 0), 0);
        
        const stats = [
            `Total de Rutas: ${totalRoutes}`,
            `Rutas Activas: ${activeRoutes}`,
            `Rutas Completadas: ${completedRoutes}`,
            `Rutas Pendientes: ${pendingRoutes}`,
            `Rutas Canceladas: ${cancelledRoutes}`,
            `Distancia Total: ${totalDistance.toFixed(2)} km`,
            `Combustible Estimado Total: ${totalFuel.toFixed(2)} L`
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
        
        // Tabla de rutas
        if (yPosition > 250) {
            doc.addPage();
            yPosition = 20;
        }
        
        doc.setFontSize(14);
        doc.setFont('helvetica', 'bold');
        doc.text('Detalle de Rutas', margin, yPosition);
        yPosition += 10;
        
        // Encabezado de tabla
        doc.setFillColor(...lightGray);
        doc.rect(margin, yPosition - 5, contentWidth, 8, 'F');
        
        doc.setFontSize(8);
        doc.setFont('helvetica', 'bold');
        doc.setTextColor(...darkColor);
        
        const colWidths = [20, 35, 50, 20, 25, 30, 25, 20, 20];
        const headers = ['Código', 'Ruta', 'Origen-Destino', 'Distancia', 'Tiempo Est.', 'Conductor', 'Vehículo', 'Combust.', 'Estado'];
        let xPos = margin + 2;
        
        headers.forEach((header, index) => {
            doc.text(header, xPos, yPosition);
            xPos += colWidths[index];
        });
        
        yPosition += 8;
        
        // Datos de rutas
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(7);
        
        routes.forEach((route, index) => {
            // Verificar si necesita nueva página
            if (yPosition > 270) {
                doc.addPage();
                yPosition = 20;
                
                // Reimprimir encabezados
                doc.setFillColor(...lightGray);
                doc.rect(margin, yPosition - 5, contentWidth, 8, 'F');
                doc.setFont('helvetica', 'bold');
                doc.setFontSize(8);
                xPos = margin + 2;
                headers.forEach((header, idx) => {
                    doc.text(header, xPos, yPosition);
                    xPos += colWidths[idx];
                });
                yPosition += 8;
                doc.setFont('helvetica', 'normal');
                doc.setFontSize(7);
            }
            
            // Fila de datos
            const codigo = route.codigo || 'N/A';
            const nombreRuta = route.nombreRuta || 'N/A';
            const origenDestino = `${route.origen || 'N/A'} - ${route.destino || 'N/A'}`;
            const distancia = route.distanciaKm ? `${route.distanciaKm.toFixed(1)} km` : 'N/A';
            const tiempoEst = route.duracionEstimadaHoras ? formatTime(route.duracionEstimadaHoras) : 'N/A';
            const conductor = route.nombreChofer && route.apellidoChofer 
                ? `${route.nombreChofer} ${route.apellidoChofer}` 
                : 'Sin asignar';
            const vehiculo = route.placaVehiculo || 'Sin asignar';
            const combustible = route.consumoEstimadoLitros 
                ? `${Math.round(route.consumoEstimadoLitros)} L` 
                : 'N/A';
            const estado = getEstadoText(route.estado);
            
            xPos = margin + 2;
            const rowData = [
                codigo.substring(0, 10),
                nombreRuta.substring(0, 18),
                origenDestino.substring(0, 25),
                distancia.substring(0, 12),
                tiempoEst.substring(0, 12),
                conductor.substring(0, 15),
                vehiculo.substring(0, 12),
                combustible.substring(0, 10),
                estado.substring(0, 12)
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
        const nombreArchivo = `Reporte_Rutas_${fechaArchivo}.pdf`;
        doc.save(nombreArchivo);
        
        showNotification('success', 'Éxito', 'Reporte generado exitosamente');
        
    } catch (error) {
        console.error('Error generando reporte:', error);
        showNotification('error', 'Error', 'Error al generar el reporte: ' + error.message);
    }
}
