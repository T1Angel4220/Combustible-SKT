// Fuel Management Script
const API_BASE_URL = 'http://localhost:8084/api/v1/fuel';
const VEHICLES_API_URL = 'http://localhost:8082/api/v1/vehicles';
const ASSIGNMENTS_API_URL = 'http://localhost:8082/api/v1/assignments';
const DRIVERS_API_URL = 'http://localhost:8081/api/v1/drivers';
const ROUTES_API_URL = 'http://localhost:8083/api/v1/routes';

let fuelConsumptions = [];
let vehicles = [];
let drivers = [];
let routes = [];
let editingFuelId = null;
let confirmationCallback = null;
let currentUserRole = null; // Rol del usuario actual

document.addEventListener('DOMContentLoaded', async function() {
    await checkAuth();
    loadFuelConsumptions();
    loadVehicles();
    loadDrivers();
    loadRoutes();
    setupEventListeners();
    
    // Establecer fecha actual por defecto
    const now = new Date();
    const fechaHoraInput = document.getElementById('fuelFechaHora');
    if (fechaHoraInput) {
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        fechaHoraInput.value = `${year}-${month}-${day}T${hours}:${minutes}`;
    }
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

function applyRoleBasedUI() {
    // Ocultar botones según el rol
    const addFuelBtn = document.querySelector('.add-fuel-btn');
    
    // Solo ADMIN y SUPERVISOR pueden crear registros de combustible
    if (currentUserRole !== 'ADMIN' && currentUserRole !== 'SUPERVISOR') {
        if (addFuelBtn) addFuelBtn.style.display = 'none';
    }
    
    // Re-renderizar la tabla para ocultar botones de acciones
    if (fuelConsumptions.length > 0) {
        renderFuelConsumptions();
    }
}

function setupEventListeners() {
    const searchInput = document.getElementById('searchInput');
    const fuelForm = document.getElementById('fuelForm');
    const precioPorLitroInput = document.getElementById('fuelPrecioPorLitro');
    const cantidadLitrosInput = document.getElementById('fuelCantidadLitros');
    
    // Interceptar clics en enlaces externos para compartir token
    document.querySelectorAll('a[href^="http://localhost:8081"], a[href^="http://localhost:8082"], a[href^="http://localhost:8083"], a[href^="http://localhost:8084"], a[href^="http://localhost:8085"]').forEach(link => {
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
        searchInput.addEventListener('input', filterFuelConsumptions);
    }
    
    if (fuelForm) {
        fuelForm.addEventListener('submit', handleFuelSubmit);
    }
    
    // Listener para cuando se seleccione un conductor (auto-completar vehículo y rutas)
    const fuelChoferSelect = document.getElementById('fuelChofer');
    if (fuelChoferSelect) {
        fuelChoferSelect.addEventListener('change', async function() {
            const choferId = this.value;
            const fuelVehiculoSelect = document.getElementById('fuelVehiculo');
            const fuelRutaSelect = document.getElementById('fuelRuta');
            
            if (choferId) {
                // Habilitar el campo de vehículo
                if (fuelVehiculoSelect) {
                    fuelVehiculoSelect.disabled = false;
                    fuelVehiculoSelect.innerHTML = '<option value="">Cargando vehículos...</option>';
                }
                
                // Cargar vehículos asociados al conductor
                await loadDriverAssignmentsForFuel(choferId);
                
                // Limpiar rutas (se cargarán cuando se seleccione un vehículo)
                if (fuelRutaSelect) {
                    fuelRutaSelect.innerHTML = '<option value="">Sin ruta asociada</option>';
                }
            } else {
                // Si no hay conductor seleccionado, bloquear y limpiar vehículo
                if (fuelVehiculoSelect) {
                    fuelVehiculoSelect.disabled = true;
                    fuelVehiculoSelect.innerHTML = '<option value="">Primero seleccione un conductor</option>';
                    fuelVehiculoSelect.value = '';
                }
                
                // Limpiar rutas
                if (fuelRutaSelect) {
                    fuelRutaSelect.innerHTML = '<option value="">Sin ruta asociada</option>';
                }
            }
        });
    }
    
    // Listener para cuando se seleccione un vehículo (filtrar rutas)
    const fuelVehiculoSelect = document.getElementById('fuelVehiculo');
    if (fuelVehiculoSelect) {
        fuelVehiculoSelect.addEventListener('change', async function() {
            const vehiculoId = this.value;
            const fuelRutaSelect = document.getElementById('fuelRuta');
            
            if (vehiculoId) {
                // Cargar rutas del vehículo seleccionado
                await loadRoutesByVehicle(vehiculoId);
            } else {
                // Si no hay vehículo seleccionado, limpiar rutas
                if (fuelRutaSelect) {
                    fuelRutaSelect.innerHTML = '<option value="">Sin ruta asociada</option>';
                }
            }
        });
    }
    
    // Calcular costo total automáticamente
    if (precioPorLitroInput && cantidadLitrosInput) {
        precioPorLitroInput.addEventListener('input', calculateCostoTotal);
        cantidadLitrosInput.addEventListener('input', calculateCostoTotal);
    }
    
    // Filtros
    const machineryTypeFilter = document.getElementById('machineryTypeFilter');
    const vehicleFilter = document.getElementById('vehicleFilter');
    const driverFilter = document.getElementById('driverFilter');
    
    if (machineryTypeFilter) {
        machineryTypeFilter.addEventListener('change', filterFuelConsumptions);
    }
    if (vehicleFilter) {
        vehicleFilter.addEventListener('change', filterFuelConsumptions);
    }
    if (driverFilter) {
        driverFilter.addEventListener('change', filterFuelConsumptions);
    }
}

function calculateCostoTotal() {
    const precioPorLitro = parseFloat(document.getElementById('fuelPrecioPorLitro').value) || 0;
    const cantidadLitros = parseFloat(document.getElementById('fuelCantidadLitros').value) || 0;
    const costoTotalInput = document.getElementById('fuelCostoTotal');
    
    if (precioPorLitro > 0 && cantidadLitros > 0) {
        const costoTotal = precioPorLitro * cantidadLitros;
        costoTotalInput.value = costoTotal.toFixed(2);
    } else {
        costoTotalInput.value = '';
    }
}

async function loadFuelConsumptions() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        if (!token || token === 'null' || token === 'undefined') {
            console.warn('No hay token disponible, redirigiendo al login...');
            window.location.href = 'http://localhost:8085/';
            return;
        }
        const response = await fetch(`${API_BASE_URL}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            fuelConsumptions = await response.json();
            renderFuelConsumptions();
            updateMetrics();
            populateFilters();
        } else {
            console.error('Error cargando registros de combustible:', response.statusText);
            if (response.status === 401) {
                window.location.href = 'http://localhost:8085/';
            } else {
                showNotification('error', 'Error', 'Error al cargar registros de combustible.');
            }
        }
    } catch (error) {
        console.error('Error de red al cargar registros:', error);
        showNotification('error', 'Error', 'Error de comunicación al cargar registros.');
    }
}

async function loadVehicles() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${VEHICLES_API_URL}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            vehicles = await response.json();
            populateVehicleSelects();
        } else {
            console.error('Error cargando vehículos:', response.statusText);
        }
    } catch (error) {
        console.error('Error de red al cargar vehículos:', error);
    }
}

async function loadDrivers() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${DRIVERS_API_URL}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            drivers = await response.json();
            populateDriverSelects();
        } else {
            console.error('Error cargando conductores:', response.statusText);
        }
    } catch (error) {
        console.error('Error de red al cargar conductores:', error);
    }
}

async function loadRoutes() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${ROUTES_API_URL}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            routes = await response.json();
            populateRouteSelects();
        } else {
            console.error('Error cargando rutas:', response.statusText);
        }
    } catch (error) {
        console.error('Error de red al cargar rutas:', error);
    }
}

// Cargar asignaciones del conductor para poblar vehículos disponibles
async function loadDriverAssignmentsForFuel(choferId) {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        if (!token) {
            window.location.href = 'http://localhost:8085/';
            return;
        }
        
        const fuelVehiculoSelect = document.getElementById('fuelVehiculo');
        
        if (!fuelVehiculoSelect) {
            console.error('No se encontró el select de vehículos');
            return;
        }
        
        // Obtener asignaciones del conductor desde vehicles-service
        const response = await fetch(`${ASSIGNMENTS_API_URL}/chofer/${choferId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const asignaciones = await response.json();
            console.log('Asignaciones recibidas para conductor', choferId, ':', asignaciones);
            
            // Limpiar el select
            fuelVehiculoSelect.innerHTML = '<option value="">Seleccione un vehículo</option>';
            
            if (asignaciones && asignaciones.length > 0) {
                // Extraer todos los vehículos únicos de las asignaciones
                const vehiculosIds = [...new Set(asignaciones.map(a => a.vehicleId).filter(id => id))];
                console.log('Vehículos IDs extraídos:', vehiculosIds);
                
                // Cargar información de cada vehículo y agregarlo al select
                for (const vehiculoId of vehiculosIds) {
                    try {
                        // Verificar si el vehículo ya está en la lista
                        let vehiculo = vehicles.find(v => v.id === vehiculoId || v.id === String(vehiculoId));
                        
                        if (!vehiculo) {
                            // Cargar el vehículo desde la API
                            const vehiculoResponse = await fetch(`${VEHICLES_API_URL}/${vehiculoId}`, {
                                headers: {
                                    'Authorization': `Bearer ${token}`
                                }
                            });
                            
                            if (vehiculoResponse.ok) {
                                vehiculo = await vehiculoResponse.json();
                                // Agregar a la lista de vehículos si no está
                                if (!vehicles.find(v => v.id === vehiculo.id)) {
                                    vehicles.push(vehiculo);
                                }
                            } else {
                                console.error(`Error cargando vehículo ${vehiculoId}:`, vehiculoResponse.status);
                            }
                        }
                        
                        // Agregar al select
                        if (vehiculo) {
                            const option = document.createElement('option');
                            option.value = vehiculo.id;
                            option.textContent = `${vehiculo.placa || vehiculoId} - ${vehiculo.marca || ''} ${vehiculo.modelo || ''}`.trim();
                            fuelVehiculoSelect.appendChild(option);
                        }
                    } catch (error) {
                        console.error(`Error cargando vehículo ${vehiculoId}:`, error);
                    }
                }
                
                // Habilitar el campo
                fuelVehiculoSelect.disabled = false;
                
                // NO seleccionar automáticamente - dejar que el usuario elija
                // Si solo hay un vehículo, se puede seleccionar automáticamente, pero mejor dejarlo al usuario
                // if (vehiculosIds.length === 1) {
                //     const vehiculoId = vehiculosIds[0];
                //     const vehiculo = vehicles.find(v => v.id === vehiculoId || v.id === String(vehiculoId));
                //     if (vehiculo) {
                //         fuelVehiculoSelect.value = vehiculo.id;
                //         // Disparar evento change para cargar rutas
                //         fuelVehiculoSelect.dispatchEvent(new Event('change'));
                //     }
                // }
            } else {
                // Si no hay asignaciones, mostrar mensaje
                fuelVehiculoSelect.innerHTML = '<option value="">El conductor no tiene vehículos asignados</option>';
                showNotification('warning', 'Advertencia', 'El conductor seleccionado no tiene vehículos asignados.');
            }
        } else {
            console.error('Error cargando asignaciones del conductor:', response.status, response.statusText);
            fuelVehiculoSelect.innerHTML = '<option value="">Error al cargar vehículos</option>';
            showNotification('error', 'Error', 'No se pudieron cargar los vehículos del conductor.');
        }
    } catch (error) {
        console.error('Error cargando asignaciones:', error);
        const fuelVehiculoSelect = document.getElementById('fuelVehiculo');
        if (fuelVehiculoSelect) {
            fuelVehiculoSelect.innerHTML = '<option value="">Error al cargar vehículos</option>';
        }
        showNotification('error', 'Error', 'Error de comunicación al cargar vehículos.');
    }
}

// Cargar conductores asociados a un vehículo específico
async function loadDriversByVehicle(vehiculoId) {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        if (!token) {
            window.location.href = 'http://localhost:8085/';
            return;
        }
        
        const response = await fetch(`${ASSIGNMENTS_API_URL}/vehiculo/${vehiculoId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const fuelChoferSelect = document.getElementById('fuelChofer');
        
        if (response.ok) {
            const asignaciones = await response.json();
            if (asignaciones && asignaciones.length > 0) {
                // Extraer todos los conductores únicos de las asignaciones
                const choferesIds = [...new Set(asignaciones.map(a => a.choferId).filter(id => id))];
                
                // Limpiar el select
                if (fuelChoferSelect) {
                    fuelChoferSelect.innerHTML = '<option value="">Seleccione un conductor</option>';
                }
                
                // Cargar información de cada conductor y agregarlo al select
                for (const choferId of choferesIds) {
                    try {
                        // Verificar si el conductor ya está en la lista
                        let chofer = drivers.find(d => d.id === choferId);
                        
                        if (!chofer) {
                            // Cargar el conductor desde la API
                            const choferResponse = await fetch(`${DRIVERS_API_URL}/${choferId}`, {
                                headers: {
                                    'Authorization': `Bearer ${token}`
                                }
                            });
                            
                            if (choferResponse.ok) {
                                chofer = await choferResponse.json();
                                // Agregar a la lista de conductores si no está
                                if (!drivers.find(d => d.id === chofer.id)) {
                                    drivers.push(chofer);
                                }
                            }
                        }
                        
                        // Agregar al select
                        if (chofer && fuelChoferSelect) {
                            const option = document.createElement('option');
                            option.value = chofer.id;
                            option.textContent = `${chofer.nombre || ''} ${chofer.apellido || ''}`.trim() || choferId;
                            fuelChoferSelect.appendChild(option);
                        }
                    } catch (error) {
                        console.error(`Error cargando conductor ${choferId}:`, error);
                    }
                }
                
                // Habilitar el campo para que el usuario pueda seleccionar
                if (fuelChoferSelect) {
                    fuelChoferSelect.disabled = false;
                }
                
                // Si solo hay un conductor, seleccionarlo automáticamente
                if (choferesIds.length === 1) {
                    fuelChoferSelect.value = choferesIds[0];
                    // Disparar el evento change para cargar vehículos y rutas
                    fuelChoferSelect.dispatchEvent(new Event('change'));
                }
            } else {
                // Si no hay asignaciones, mostrar todos los conductores disponibles
                if (fuelChoferSelect) {
                    fuelChoferSelect.innerHTML = '<option value="">Seleccione un conductor</option>';
                    drivers.forEach(chofer => {
                        const option = document.createElement('option');
                        option.value = chofer.id;
                        option.textContent = `${chofer.nombre || ''} ${chofer.apellido || ''}`.trim() || chofer.id;
                        fuelChoferSelect.appendChild(option);
                    });
                    fuelChoferSelect.disabled = false;
                }
            }
        } else {
            console.error('Error cargando asignaciones del vehículo:', response.status);
            // En caso de error, mostrar todos los conductores disponibles
            if (fuelChoferSelect) {
                fuelChoferSelect.innerHTML = '<option value="">Seleccione un conductor</option>';
                drivers.forEach(chofer => {
                    const option = document.createElement('option');
                    option.value = chofer.id;
                    option.textContent = `${chofer.nombre || ''} ${chofer.apellido || ''}`.trim() || chofer.id;
                    fuelChoferSelect.appendChild(option);
                });
                fuelChoferSelect.disabled = false;
            }
        }
    } catch (error) {
        console.error('Error cargando asignaciones del vehículo:', error);
        // En caso de error, mostrar todos los conductores disponibles
        const fuelChoferSelect = document.getElementById('fuelChofer');
        if (fuelChoferSelect) {
            fuelChoferSelect.innerHTML = '<option value="">Seleccione un conductor</option>';
            drivers.forEach(chofer => {
                const option = document.createElement('option');
                option.value = chofer.id;
                option.textContent = `${chofer.nombre || ''} ${chofer.apellido || ''}`.trim() || chofer.id;
                fuelChoferSelect.appendChild(option);
            });
            fuelChoferSelect.disabled = false;
        }
    }
}

// Cargar rutas de un conductor específico
async function loadRoutesByDriver(choferId) {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        if (!token) {
            window.location.href = 'http://localhost:8085/';
            return;
        }
        
        // Filtrar rutas del conductor desde las rutas ya cargadas
        const fuelRutaSelect = document.getElementById('fuelRuta');
        
        if (fuelRutaSelect) {
            fuelRutaSelect.innerHTML = '<option value="">Sin ruta asociada</option>';
            
            // Filtrar rutas del conductor seleccionado
            const rutasDelConductor = routes.filter(route => route.choferId === choferId);
            
            if (rutasDelConductor.length > 0) {
                rutasDelConductor.forEach(route => {
                    const option = document.createElement('option');
                    option.value = route.id;
                    option.textContent = `${route.codigo || route.id} - ${route.nombreRuta || 'Sin nombre'}`;
                    fuelRutaSelect.appendChild(option);
                });
            } else {
                // Si no hay rutas, mantener solo la opción "Sin ruta asociada"
                console.log('El conductor no tiene rutas asociadas');
            }
        }
    } catch (error) {
        console.error('Error filtrando rutas del conductor:', error);
        // Mantener todas las rutas como fallback
        populateRouteSelects();
    }
}

// Cargar rutas de un vehículo específico
async function loadRoutesByVehicle(vehiculoId) {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        if (!token) {
            window.location.href = 'http://localhost:8085/';
            return;
        }
        
        const fuelRutaSelect = document.getElementById('fuelRuta');
        
        if (!fuelRutaSelect) {
            console.error('No se encontró el select de rutas');
            return;
        }
        
        fuelRutaSelect.innerHTML = '<option value="">Cargando rutas...</option>';
        
        // Filtrar rutas del vehículo desde las rutas ya cargadas
        // Comparar tanto por ID directo como por String para asegurar coincidencia
        const rutasDelVehiculo = routes.filter(route => {
            const routeVehiculoId = route.vehiculoId;
            return routeVehiculoId && (
                routeVehiculoId === vehiculoId || 
                routeVehiculoId === String(vehiculoId) ||
                String(routeVehiculoId) === String(vehiculoId)
            );
        });
        
        console.log('Rutas filtradas para vehículo', vehiculoId, ':', rutasDelVehiculo);
        console.log('Total de rutas cargadas:', routes.length);
        console.log('IDs de vehículos en rutas:', routes.map(r => r.vehiculoId));
        
        fuelRutaSelect.innerHTML = '<option value="">Sin ruta asociada</option>';
        
        if (rutasDelVehiculo.length > 0) {
            rutasDelVehiculo.forEach(route => {
                const option = document.createElement('option');
                option.value = route.id;
                option.textContent = `${route.codigo || route.id} - ${route.nombreRuta || 'Sin nombre'}`;
                fuelRutaSelect.appendChild(option);
            });
        } else {
            console.log('El vehículo no tiene rutas asociadas');
        }
    } catch (error) {
        console.error('Error filtrando rutas del vehículo:', error);
        const fuelRutaSelect = document.getElementById('fuelRuta');
        if (fuelRutaSelect) {
            fuelRutaSelect.innerHTML = '<option value="">Error al cargar rutas</option>';
        }
    }
}

// Función eliminada - ya no se necesita, solo filtramos por vehículo
// async function updateRoutesByDriverAndVehicle() {

// Cargar información de un vehículo por ID
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
                populateVehicleSelects();
            }
        }
    } catch (error) {
        console.error('Error cargando vehículo:', error);
    }
}

function populateVehicleSelects() {
    const fuelVehiculoSelect = document.getElementById('fuelVehiculo');
    const vehicleFilterSelect = document.getElementById('vehicleFilter');
    
    if (fuelVehiculoSelect) {
        fuelVehiculoSelect.innerHTML = '<option value="">Seleccione un vehículo</option>';
        vehicles.forEach(vehicle => {
            const option = document.createElement('option');
            option.value = vehicle.id;
            option.textContent = `${vehicle.placa} - ${vehicle.marca} ${vehicle.modelo}`;
            fuelVehiculoSelect.appendChild(option);
        });
    }
    
    if (vehicleFilterSelect) {
        vehicleFilterSelect.innerHTML = '<option value="">Todos los vehículos</option>';
        vehicles.forEach(vehicle => {
            const option = document.createElement('option');
            option.value = vehicle.id;
            option.textContent = `${vehicle.placa} - ${vehicle.marca} ${vehicle.modelo}`;
            vehicleFilterSelect.appendChild(option);
        });
    }
}

function populateDriverSelects() {
    const fuelChoferSelect = document.getElementById('fuelChofer');
    const driverFilterSelect = document.getElementById('driverFilter');
    
    if (fuelChoferSelect) {
        fuelChoferSelect.innerHTML = '<option value="">Seleccione un conductor</option>';
        drivers.forEach(driver => {
            const option = document.createElement('option');
            option.value = driver.id;
            option.textContent = `${driver.nombre} ${driver.apellido}`;
            fuelChoferSelect.appendChild(option);
        });
    }
    
    if (driverFilterSelect) {
        driverFilterSelect.innerHTML = '<option value="">Todos los conductores</option>';
        drivers.forEach(driver => {
            const option = document.createElement('option');
            option.value = driver.id;
            option.textContent = `${driver.nombre} ${driver.apellido}`;
            driverFilterSelect.appendChild(option);
        });
    }
}

function populateRouteSelects() {
    const fuelRutaSelect = document.getElementById('fuelRuta');
    const routeComparisonSelect = document.getElementById('routeComparisonSelect');
    
    if (fuelRutaSelect) {
        fuelRutaSelect.innerHTML = '<option value="">Sin ruta asociada</option>';
        routes.forEach(route => {
            const option = document.createElement('option');
            option.value = route.id;
            option.textContent = `${route.codigo || route.id} - ${route.nombreRuta}`;
            fuelRutaSelect.appendChild(option);
        });
    }
    
    if (routeComparisonSelect) {
        routeComparisonSelect.innerHTML = '<option value="">Seleccione una ruta</option>';
        routes.forEach(route => {
            const option = document.createElement('option');
            option.value = route.id;
            option.textContent = `${route.codigo || route.id} - ${route.nombreRuta}`;
            routeComparisonSelect.appendChild(option);
        });
    }
}

function populateFilters() {
    // Los filtros ya están poblados por loadVehicles y loadDrivers
}

function updateMetrics() {
    const totalRegistros = fuelConsumptions.length;
    const totalLitros = fuelConsumptions.reduce((sum, f) => sum + (f.cantidadLitros || 0), 0);
    const costoTotal = fuelConsumptions.reduce((sum, f) => sum + (f.costoTotal || 0), 0);
    const promedioLitros = totalRegistros > 0 ? totalLitros / totalRegistros : 0;
    
    document.getElementById('totalRegistros').textContent = totalRegistros;
    document.getElementById('totalLitros').textContent = Math.round(totalLitros);
    document.getElementById('costoTotal').textContent = `$${costoTotal.toFixed(2)}`;
    document.getElementById('promedioLitros').textContent = Math.round(promedioLitros);
}

function filterFuelConsumptions() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const machineryTypeFilter = document.getElementById('machineryTypeFilter').value;
    const vehicleFilter = document.getElementById('vehicleFilter').value;
    const driverFilter = document.getElementById('driverFilter').value;
    const fechaInicio = document.getElementById('fechaInicio').value;
    const fechaFin = document.getElementById('fechaFin').value;
    
    let filtered = fuelConsumptions;
    
    // Filtro por búsqueda
    if (searchTerm) {
        filtered = filtered.filter(f => {
            const vehiculo = vehicles.find(v => v.id === f.vehiculoId);
            const conductor = drivers.find(d => d.id === f.choferId);
            const ruta = routes.find(r => r.id === f.rutaId);
            
            return (vehiculo && (vehiculo.placa?.toLowerCase().includes(searchTerm) || 
                                vehiculo.marca?.toLowerCase().includes(searchTerm))) ||
                   (conductor && (conductor.nombre?.toLowerCase().includes(searchTerm) || 
                                 conductor.apellido?.toLowerCase().includes(searchTerm))) ||
                   (ruta && ruta.nombreRuta?.toLowerCase().includes(searchTerm));
        });
    }
    
    // Filtro por tipo de maquinaria
    if (machineryTypeFilter) {
        filtered = filtered.filter(f => {
            const vehiculo = vehicles.find(v => v.id === f.vehiculoId);
            return vehiculo && vehiculo.tipoMaquinaria === machineryTypeFilter;
        });
    }
    
    // Filtro por vehículo
    if (vehicleFilter) {
        filtered = filtered.filter(f => f.vehiculoId === vehicleFilter);
    }
    
    // Filtro por conductor
    if (driverFilter) {
        filtered = filtered.filter(f => f.choferId === driverFilter);
    }
    
    // Filtro por rango de fechas
    if (fechaInicio && fechaFin) {
        const inicio = new Date(fechaInicio);
        const fin = new Date(fechaFin);
        fin.setHours(23, 59, 59, 999); // Incluir todo el día final
        
        filtered = filtered.filter(f => {
            if (!f.fechaHora) return false;
            const fecha = new Date(f.fechaHora);
            return fecha >= inicio && fecha <= fin;
        });
    }
    
    renderFuelConsumptions(filtered);
}

function applyFilters() {
    filterFuelConsumptions();
}

function clearFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('machineryTypeFilter').value = '';
    document.getElementById('vehicleFilter').value = '';
    document.getElementById('driverFilter').value = '';
    document.getElementById('fechaInicio').value = '';
    document.getElementById('fechaFin').value = '';
    filterFuelConsumptions();
}

function renderFuelConsumptions(consumptionsToRender = fuelConsumptions) {
    const tbody = document.getElementById('fuelTableBody');
    if (!tbody) return;
    
    if (consumptionsToRender.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align: center; padding: 40px; color: var(--text-secondary);">No hay registros de combustible</td></tr>';
        return;
    }
    
    tbody.innerHTML = consumptionsToRender.map(consumption => {
        const vehiculo = vehicles.find(v => v.id === consumption.vehiculoId);
        const conductor = drivers.find(d => d.id === consumption.choferId);
        const ruta = routes.find(r => r.id === consumption.rutaId);
        
        const vehiculoText = vehiculo ? `${vehiculo.placa} - ${vehiculo.marca} ${vehiculo.modelo}` : 'N/A';
        const conductorText = conductor ? `${conductor.nombre} ${conductor.apellido}` : 'N/A';
        const rutaText = ruta ? `${ruta.codigo || ruta.id} - ${ruta.nombreRuta}` : 'Sin ruta';
        
        const fechaHora = consumption.fechaHora ? new Date(consumption.fechaHora) : null;
        const fechaHoraText = fechaHora ? fechaHora.toLocaleString('es-EC') : 'N/A';
        
        const tipoCombustibleClass = getTipoCombustibleClass(consumption.tipoCombustible);
        const tipoCombustibleText = getTipoCombustibleText(consumption.tipoCombustible);
        
        const tipoMaquinariaClass = getTipoMaquinariaClass(consumption.tipoMaquinaria);
        const tipoMaquinariaText = getTipoMaquinariaText(consumption.tipoMaquinaria);
        
        return `
            <tr>
                <td>${fechaHoraText}</td>
                <td>${vehiculoText}</td>
                <td>${conductorText}</td>
                <td>${rutaText}</td>
                <td><strong>${consumption.cantidadLitros?.toFixed(2) || '0.00'}</strong></td>
                <td><span class="fuel-type-badge ${tipoCombustibleClass}">${tipoCombustibleText}</span></td>
                <td>${consumption.precioPorLitro ? `$${consumption.precioPorLitro.toFixed(2)}` : 'N/A'}</td>
                <td><strong>${consumption.costoTotal ? `$${consumption.costoTotal.toFixed(2)}` : 'N/A'}</strong></td>
                <td><span class="machinery-type-badge ${tipoMaquinariaClass}">${tipoMaquinariaText}</span></td>
                <td>
                    <div class="table-actions">
                        ${(currentUserRole === 'ADMIN' || currentUserRole === 'SUPERVISOR')
                            ? `<button class="action-btn edit" onclick="editFuelConsumption('${consumption.id}')" title="Editar">
                                <i class="fas fa-edit"></i>
                            </button>`
                            : ''
                        }
                        ${currentUserRole === 'ADMIN'
                            ? `<button class="action-btn delete" onclick="deleteFuelConsumption('${consumption.id}')" title="Eliminar">
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

function getTipoCombustibleClass(tipo) {
    if (!tipo) return 'diesel';
    return tipo.toLowerCase();
}

function getTipoCombustibleText(tipo) {
    const tipos = {
        'DIESEL': 'Diesel',
        'GASOLINA': 'Gasolina',
        'GAS': 'Gas',
        'ELECTRICO': 'Eléctrico'
    };
    return tipos[tipo] || tipo;
}

function getTipoMaquinariaClass(tipo) {
    if (!tipo) return 'liviana';
    const pesados = ['EXCAVADORA', 'CARGADOR', 'GRUA', 'MOTONIVELADORA'];
    return pesados.includes(tipo) ? 'pesada' : 'liviana';
}

function getTipoMaquinariaText(tipo) {
    if (!tipo) return 'N/A';
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

function showAddFuelModal() {
    editingFuelId = null;
    document.getElementById('fuelModalTitle').textContent = 'Registrar Combustible';
    document.getElementById('fuelForm').reset();
    
    // Establecer fecha actual
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    document.getElementById('fuelFechaHora').value = `${year}-${month}-${day}T${hours}:${minutes}`;
    
    document.getElementById('fuelModal').classList.add('active');
}

function closeFuelModal() {
    document.getElementById('fuelModal').classList.remove('active');
    editingFuelId = null;
    document.getElementById('fuelForm').reset();
    
    // Bloquear el campo de vehículo al cerrar el modal
    const fuelVehiculoSelect = document.getElementById('fuelVehiculo');
    if (fuelVehiculoSelect) {
        fuelVehiculoSelect.disabled = true;
        fuelVehiculoSelect.innerHTML = '<option value="">Primero seleccione un conductor</option>';
    }
    
    // Limpiar rutas
    const fuelRutaSelect = document.getElementById('fuelRuta');
    if (fuelRutaSelect) {
        fuelRutaSelect.innerHTML = '<option value="">Sin ruta asociada</option>';
    }
    
    // Asegurar que el campo de conductor esté habilitado (siempre debe poder cambiar)
    const fuelChoferSelect = document.getElementById('fuelChofer');
    if (fuelChoferSelect) {
        fuelChoferSelect.disabled = false;
    }
}

async function handleFuelSubmit(e) {
    e.preventDefault();
    
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    
    const fechaHoraValue = document.getElementById('fuelFechaHora').value;
    const fechaHora = fechaHoraValue ? new Date(fechaHoraValue).toISOString() : new Date().toISOString();
    
    const formData = {
        fechaHora: fechaHora,
        cantidadLitros: parseFloat(document.getElementById('fuelCantidadLitros').value),
        tipoCombustible: document.getElementById('fuelTipoCombustible').value,
        precioPorLitro: document.getElementById('fuelPrecioPorLitro').value ? 
            parseFloat(document.getElementById('fuelPrecioPorLitro').value) : null,
        costoTotal: document.getElementById('fuelCostoTotal').value ? 
            parseFloat(document.getElementById('fuelCostoTotal').value) : null,
        vehiculoId: document.getElementById('fuelVehiculo').value,
        choferId: document.getElementById('fuelChofer').value,
        rutaId: document.getElementById('fuelRuta').value || null,
        lecturaOdometroHoras: document.getElementById('fuelLecturaOdometro').value ? 
            parseFloat(document.getElementById('fuelLecturaOdometro').value) : null,
        observaciones: document.getElementById('fuelObservaciones').value || null
    };
    
    try {
        let response;
        if (editingFuelId) {
            // Actualizar
            response = await fetch(`${API_BASE_URL}/${editingFuelId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(formData)
            });
        } else {
            // Crear
            response = await fetch(`${API_BASE_URL}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(formData)
            });
        }
        
        if (response.ok) {
            showNotification('success', 'Éxito', 
                editingFuelId ? 'Registro de combustible actualizado exitosamente' : 'Registro de combustible creado exitosamente');
            closeFuelModal();
            await loadFuelConsumptions();
        } else {
            const errorData = await response.json().catch(() => ({ error: 'Error al guardar registro' }));
            showNotification('error', 'Error', errorData.error || 'Error al guardar registro de combustible');
        }
    } catch (error) {
        console.error('Error guardando registro:', error);
        showNotification('error', 'Error', 'Error de red al guardar registro');
    }
}

async function editFuelConsumption(id) {
    const consumption = fuelConsumptions.find(f => f.id === id);
    if (!consumption) return;
    
    editingFuelId = id;
    document.getElementById('fuelModalTitle').textContent = 'Editar Registro de Combustible';
    
    // Formatear fecha para input datetime-local
    const fechaHora = consumption.fechaHora ? new Date(consumption.fechaHora) : new Date();
    const year = fechaHora.getFullYear();
    const month = String(fechaHora.getMonth() + 1).padStart(2, '0');
    const day = String(fechaHora.getDate()).padStart(2, '0');
    const hours = String(fechaHora.getHours()).padStart(2, '0');
    const minutes = String(fechaHora.getMinutes()).padStart(2, '0');
    
    document.getElementById('fuelFechaHora').value = `${year}-${month}-${day}T${hours}:${minutes}`;
    document.getElementById('fuelCantidadLitros').value = consumption.cantidadLitros || '';
    document.getElementById('fuelTipoCombustible').value = consumption.tipoCombustible || '';
    document.getElementById('fuelPrecioPorLitro').value = consumption.precioPorLitro || '';
    document.getElementById('fuelCostoTotal').value = consumption.costoTotal || '';
    document.getElementById('fuelVehiculo').value = consumption.vehiculoId || '';
    document.getElementById('fuelChofer').value = consumption.choferId || '';
    document.getElementById('fuelRuta').value = consumption.rutaId || '';
    document.getElementById('fuelLecturaOdometro').value = consumption.lecturaOdometroHoras || '';
    document.getElementById('fuelObservaciones').value = consumption.observaciones || '';
    
    document.getElementById('fuelModal').classList.add('active');
}

async function deleteFuelConsumption(id) {
    showConfirmation('Confirmar Eliminación', 
        '¿Estás seguro de que deseas eliminar este registro de combustible?', 
        async (confirmed) => {
            if (confirmed) {
                try {
                    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
                    const response = await fetch(`${API_BASE_URL}/${id}`, {
                        method: 'DELETE',
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    });
                    
                    if (response.ok || response.status === 204) {
                        showNotification('success', 'Éxito', 'Registro de combustible eliminado exitosamente');
                        await loadFuelConsumptions();
                    } else {
                        const errorData = await response.json().catch(() => ({ error: 'Error al eliminar registro' }));
                        showNotification('error', 'Error', errorData.error || 'Error al eliminar registro');
                    }
                } catch (error) {
                    console.error('Error eliminando registro:', error);
                    showNotification('error', 'Error', 'Error de red al eliminar registro');
                }
            }
        });
}

async function compareConsumption() {
    const rutaId = document.getElementById('routeComparisonSelect').value;
    if (!rutaId) {
        showNotification('warning', 'Advertencia', 'Por favor, seleccione una ruta');
        return;
    }
    
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${API_BASE_URL}/compare/${rutaId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const comparison = await response.json();
            displayComparisonResult(comparison);
        } else {
            const errorData = await response.json().catch(() => ({ error: 'Error al comparar consumo' }));
            showNotification('error', 'Error', errorData.error || 'Error al comparar consumo');
        }
    } catch (error) {
        console.error('Error comparando consumo:', error);
        showNotification('error', 'Error', 'Error de red al comparar consumo');
    }
}

function displayComparisonResult(comparison) {
    const resultDiv = document.getElementById('comparisonResult');
    if (!resultDiv) return;
    
    const diferenciaClass = comparison.diferenciaLitros >= 0 ? 'positive' : 'negative';
    const diferenciaSign = comparison.diferenciaLitros >= 0 ? '+' : '';
    
    resultDiv.innerHTML = `
        <div class="comparison-result">
            <h4 style="margin-bottom: 16px; color: var(--text-primary);">${comparison.rutaNombre || 'Ruta'}</h4>
            <div class="comparison-item">
                <span class="comparison-label">Consumo Estimado:</span>
                <span class="comparison-value">${comparison.consumoEstimadoLitros?.toFixed(2) || '0.00'} L</span>
            </div>
            <div class="comparison-item">
                <span class="comparison-label">Consumo Real:</span>
                <span class="comparison-value">${comparison.consumoRealLitros?.toFixed(2) || '0.00'} L</span>
            </div>
            <div class="comparison-item">
                <span class="comparison-label">Diferencia:</span>
                <span class="comparison-value ${diferenciaClass}">${diferenciaSign}${comparison.diferenciaLitros?.toFixed(2) || '0.00'} L</span>
            </div>
            <div class="comparison-item">
                <span class="comparison-label">Diferencia Porcentual:</span>
                <span class="comparison-value ${diferenciaClass}">${diferenciaSign}${comparison.diferenciaPorcentaje?.toFixed(2) || '0.00'}%</span>
            </div>
            <div class="comparison-item">
                <span class="comparison-label">Estado:</span>
                <span class="comparison-value ${comparison.consumoRealMayor ? 'negative' : 'positive'}">
                    ${comparison.consumoRealMayor ? 'Consumo Real Mayor' : 'Consumo Real Menor o Igual'}
                </span>
            </div>
        </div>
    `;
    resultDiv.style.display = 'block';
}

async function generateMachineryReport() {
    const tipoMaquinaria = document.getElementById('reportMachineryType').value;
    if (!tipoMaquinaria) {
        showNotification('warning', 'Advertencia', 'Por favor, seleccione un tipo de maquinaria');
        return;
    }
    
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${API_BASE_URL}/report/machinery-type/${tipoMaquinaria}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const report = await response.json();
            displayMachineryReport(report);
        } else {
            const errorData = await response.json().catch(() => ({ error: 'Error al generar reporte' }));
            showNotification('error', 'Error', errorData.error || 'Error al generar reporte');
        }
    } catch (error) {
        console.error('Error generando reporte:', error);
        showNotification('error', 'Error', 'Error de red al generar reporte');
    }
}

function displayMachineryReport(report) {
    const resultDiv = document.getElementById('machineryReportResult');
    if (!resultDiv) return;
    
    const tipoText = getTipoMaquinariaText(report.tipoMaquinaria);
    
    resultDiv.innerHTML = `
        <div class="report-result">
            <h4 style="margin-bottom: 16px; color: var(--text-primary);">Reporte: ${tipoText}</h4>
            <div class="report-stat">
                <span class="report-stat-label">Total Litros:</span>
                <span class="report-stat-value">${report.totalLitros?.toFixed(2) || '0.00'} L</span>
            </div>
            <div class="report-stat">
                <span class="report-stat-label">Costo Total:</span>
                <span class="report-stat-value">$${report.totalCosto?.toFixed(2) || '0.00'}</span>
            </div>
            <div class="report-stat">
                <span class="report-stat-label">Total Registros:</span>
                <span class="report-stat-value">${report.totalRegistros || 0}</span>
            </div>
            <div class="report-stat">
                <span class="report-stat-label">Promedio por Registro:</span>
                <span class="report-stat-value">${report.promedioLitrosPorRegistro?.toFixed(2) || '0.00'} L</span>
            </div>
        </div>
    `;
    resultDiv.style.display = 'block';
}

function showNotification(type, title, message) {
    const modal = document.getElementById('notificationModal');
    const icon = document.getElementById('notificationIcon');
    const titleEl = document.getElementById('notificationTitle');
    const messageEl = document.getElementById('notificationMessage');
    
    if (!modal || !icon || !titleEl || !messageEl) return;
    
    // Remover clases anteriores
    icon.className = 'notification-icon';
    icon.classList.add(type);
    
    // Cambiar icono según tipo
    const iconMap = {
        'success': 'fa-check-circle',
        'error': 'fa-exclamation-circle',
        'warning': 'fa-exclamation-triangle',
        'info': 'fa-info-circle'
    };
    icon.innerHTML = `<i class="fas ${iconMap[type] || 'fa-info-circle'}"></i>`;
    
    titleEl.textContent = title;
    messageEl.textContent = message;
    
    modal.classList.add('active');
    
    // Auto-cerrar después de 3 segundos para success
    if (type === 'success') {
        setTimeout(() => {
            closeNotificationModal();
        }, 3000);
    }
}

function closeNotificationModal() {
    const modal = document.getElementById('notificationModal');
    if (modal) {
        modal.classList.remove('active');
    }
}

function showConfirmation(title, message, callback) {
    const modal = document.getElementById('confirmationModal');
    const titleEl = document.getElementById('confirmationTitle');
    const messageEl = document.getElementById('confirmationMessage');
    
    if (!modal || !titleEl || !messageEl) return;
    
    titleEl.textContent = title;
    messageEl.textContent = message;
    confirmationCallback = callback;
    
    modal.classList.add('active');
}

function closeConfirmationModal(confirmed) {
    const modal = document.getElementById('confirmationModal');
    if (modal) {
        modal.classList.remove('active');
    }
    
    if (confirmationCallback) {
        confirmationCallback(confirmed);
        confirmationCallback = null;
    }
}

function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    sessionStorage.removeItem('authToken');
    sessionStorage.removeItem('currentUser');
    window.location.href = 'http://localhost:8085/';
}

