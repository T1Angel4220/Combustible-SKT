// Dashboard Script
let fuelChart, fleetChart;
let confirmationCallback = null;

// Función para compartir el token con otros servicios
function shareTokenWithServices() {
    const token = localStorage.getItem('authToken');
    const user = localStorage.getItem('currentUser');
    
    if (token) {
        // Guardar en sessionStorage para que esté disponible en otros puertos
        sessionStorage.setItem('authToken', token);
        if (user) {
            sessionStorage.setItem('currentUser', user);
        }
    }
}

document.addEventListener('DOMContentLoaded', async function() {
    checkAuth();
    shareTokenWithServices();
    initializeCharts();
    await loadDashboardData();
    await loadRecentActivity();
    
    // Interceptar clics en enlaces externos para compartir token
    document.querySelectorAll('a[href^="http://localhost:8081"], a[href^="http://localhost:8082"], a[href^="http://localhost:8083"], a[href^="http://localhost:8084"]').forEach(link => {
        link.addEventListener('click', function(e) {
            shareTokenWithServices();
            // Agregar token a la URL como parámetro
            const url = new URL(this.href);
            const token = localStorage.getItem('authToken');
            if (token) {
                url.searchParams.set('token', token);
                const user = localStorage.getItem('currentUser');
                if (user) {
                    url.searchParams.set('user', user);
                }
                this.href = url.toString();
            }
        });
    });
});

function checkAuth() {
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = 'index.html';
    }
}

function initializeCharts() {
    // Fuel Consumption Chart - Se inicializa con datos vacíos, se actualizará con datos reales
    const fuelCtx = document.getElementById('fuelConsumptionChart');
    if (fuelCtx) {
        fuelChart = new Chart(fuelCtx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [{
                    label: 'Consumo (L)',
                    data: [],
                    backgroundColor: 'rgba(248, 81, 73, 0.8)',
                    borderColor: 'rgba(248, 81, 73, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            color: '#8b949e'
                        },
                        grid: {
                            color: '#30363d'
                        }
                    },
                    x: {
                        ticks: {
                            color: '#8b949e'
                        },
                        grid: {
                            color: '#30363d'
                        }
                    }
                }
            }
        });
    }

    // Fleet Distribution Chart - Se inicializa con datos vacíos, se actualizará con datos reales
    const fleetCtx = document.getElementById('fleetDistributionChart');
    if (fleetCtx) {
        fleetChart = new Chart(fleetCtx, {
            type: 'doughnut',
            data: {
                labels: ['Maquinaria Liviana', 'Maquinaria Pesada'],
                datasets: [{
                    data: [0, 0],
                    backgroundColor: [
                        'rgba(248, 81, 73, 0.8)',
                        'rgba(139, 148, 158, 0.8)'
                    ],
                    borderColor: [
                        'rgba(248, 81, 73, 1)',
                        'rgba(139, 148, 158, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });
    }
}

async function loadFuelConsumptionChartData() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        if (!token || !fuelChart) return;

        const headers = {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };

        // Cargar todos los registros de combustible
        const fuelRes = await fetch('http://localhost:8084/api/v1/fuel', { headers });
        if (!fuelRes.ok) {
            console.error('Error cargando datos de combustible para gráfico');
            return;
        }

        const fuelConsumptions = await fuelRes.json();
        if (!Array.isArray(fuelConsumptions) || fuelConsumptions.length === 0) {
            // Si no hay datos, mostrar mensaje o datos vacíos
            fuelChart.data.labels = ['Sin datos'];
            fuelChart.data.datasets[0].data = [0];
            fuelChart.update();
            return;
        }

        // Agrupar por mes (últimos 6 meses)
        const now = new Date();
        const months = [];
        const monthData = {};

        // Generar etiquetas de los últimos 6 meses
        for (let i = 5; i >= 0; i--) {
            const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
            const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
            const monthLabel = date.toLocaleDateString('es-EC', { month: 'short' });
            months.push(monthLabel);
            monthData[monthKey] = 0;
        }

        // Agrupar consumos por mes
        fuelConsumptions.forEach(consumption => {
            if (consumption.fechaHora) {
                const fecha = new Date(consumption.fechaHora);
                const monthKey = `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}`;
                if (monthData.hasOwnProperty(monthKey)) {
                    monthData[monthKey] += consumption.cantidadLitros || 0;
                }
            }
        });

        // Convertir a array de datos
        const data = months.map((_, index) => {
            const monthKey = Object.keys(monthData)[index];
            return monthData[monthKey] || 0;
        });

        // Actualizar gráfico
        fuelChart.data.labels = months;
        fuelChart.data.datasets[0].data = data;
        fuelChart.update();
    } catch (error) {
        console.error('Error cargando datos de consumo para gráfico:', error);
    }
}

function switchChartView(type) {
    if (!fuelChart) return;
    
    fuelChart.config.type = type;
    fuelChart.update();
    
    // Update tab buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
}

async function loadDashboardData() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        if (!token) {
            console.error('No hay token disponible');
            return;
        }

        const headers = {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };

        // Cargar vehículos
        try {
            const vehiclesRes = await fetch('http://localhost:8082/api/v1/vehicles', { headers });
            if (vehiclesRes.ok) {
                const vehicles = await vehiclesRes.json();
                const totalVehicles = Array.isArray(vehicles) ? vehicles.length : 0;
                document.getElementById('totalVehicles').textContent = totalVehicles;
                
                // Calcular distribución por tipo de maquinaria
                const liviana = vehicles.filter(v => {
                    const tipo = v.tipoMaquinaria || '';
                    return tipo === 'CAMION' || tipo === 'VOLQUETE';
                }).length;
                const pesada = vehicles.filter(v => {
                    const tipo = v.tipoMaquinaria || '';
                    return tipo === 'EXCAVADORA' || tipo === 'CARGADOR' || tipo === 'GRUA' || tipo === 'MOTONIVELADORA';
                }).length;
                
                // Actualizar gráfico de distribución de flota
                if (fleetChart) {
                    fleetChart.data.datasets[0].data = [liviana, pesada];
                    fleetChart.update();
                    
                    // Actualizar leyenda
                    const legendItems = document.querySelectorAll('.legend-item');
                    if (legendItems.length >= 2) {
                        legendItems[0].querySelector('strong').textContent = `${liviana} unidades`;
                        legendItems[1].querySelector('strong').textContent = `${pesada} unidades`;
                    }
                }
            }
        } catch (error) {
            console.error('Error cargando vehículos:', error);
        }

        // Cargar conductores activos
        try {
            const driversRes = await fetch('http://localhost:8081/api/v1/drivers', { headers });
            if (driversRes.ok) {
                const drivers = await driversRes.json();
                const activeDrivers = Array.isArray(drivers) ? drivers.filter(d => d.activo !== false).length : 0;
                document.getElementById('activeDrivers').textContent = activeDrivers;
            }
        } catch (error) {
            console.error('Error cargando conductores:', error);
        }

        // Cargar rutas
        try {
            const routesRes = await fetch('http://localhost:8083/api/v1/routes?all=true', { headers });
            if (routesRes.ok) {
                const routes = await routesRes.json();
                const allRoutes = Array.isArray(routes) ? routes : [];
                const activeRoutes = allRoutes.filter(r => r.estado === 'EN_PROGRESO' || r.estado === 'PENDIENTE').length;
                const completedRoutes = allRoutes.filter(r => r.estado === 'COMPLETADA').length;
                const inProgressRoutes = allRoutes.filter(r => r.estado === 'EN_PROGRESO').length;
                
                document.getElementById('activeRoutes').textContent = activeRoutes;
                const routeFooter = document.querySelector('#activeRoutes').closest('.metric-card').querySelector('.metric-footer');
                if (routeFooter) {
                    routeFooter.innerHTML = `
                        <span class="metric-info">${inProgressRoutes} en progreso</span>
                        <span class="metric-label">Rutas completadas: ${completedRoutes}</span>
                    `;
                }
            }
        } catch (error) {
            console.error('Error cargando rutas:', error);
        }

        // Cargar estadísticas de combustible
        try {
            const fuelStatsRes = await fetch('http://localhost:8084/api/v1/fuel/stats', { headers });
            if (fuelStatsRes.ok) {
                const fuelStats = await fuelStatsRes.json();
                const totalLitros = fuelStats.totalLitros || 0;
                const totalCosto = fuelStats.totalCosto || 0;
                
                // Formatear litros con separador de miles
                const formattedLitros = new Intl.NumberFormat('es-EC').format(Math.round(totalLitros));
                document.getElementById('monthlyConsumption').textContent = `${formattedLitros} L`;
                
                // Formatear ahorro (por ahora mostrar costo total)
                const formattedCosto = new Intl.NumberFormat('es-EC', { 
                    style: 'currency', 
                    currency: 'USD',
                    minimumFractionDigits: 0
                }).format(totalCosto);
                const fuelFooter = document.querySelector('#monthlyConsumption').closest('.metric-card').querySelector('.metric-footer');
                if (fuelFooter) {
                    fuelFooter.innerHTML = `
                        <span class="metric-change negative">Costo total</span>
                        <span class="metric-label">Total: ${formattedCosto}</span>
                    `;
                }
            }
        } catch (error) {
            console.error('Error cargando estadísticas de combustible:', error);
        }

        // Cargar datos para gráfico de consumo mensual
        await loadFuelConsumptionChartData();
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

async function loadRecentActivity() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        if (!token) {
            console.error('No hay token disponible para actividad reciente');
            const activityList = document.getElementById('activityList');
            if (activityList) {
                activityList.innerHTML = '<div style="text-align: center; padding: 20px; color: #8b949e;">No hay token disponible</div>';
            }
            return;
        }

        const headers = {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };

        const activityList = document.getElementById('activityList');
        if (!activityList) {
            console.error('No se encontró el elemento activityList');
            return;
        }

        // Cargar rutas recientes
        let routes = [];
        try {
            const routesRes = await fetch('http://localhost:8083/api/v1/routes?all=true', { headers });
            if (routesRes.ok) {
                const routesData = await routesRes.json();
                routes = Array.isArray(routesData) ? routesData : [];
                console.log('Rutas cargadas para actividad:', routes.length);
            } else {
                console.error('Error cargando rutas. Status:', routesRes.status);
                activityList.innerHTML = '<div style="text-align: center; padding: 20px; color: #8b949e;">Error al cargar rutas</div>';
                return;
            }
        } catch (error) {
            console.error('Error en fetch de rutas:', error);
            activityList.innerHTML = '<div style="text-align: center; padding: 20px; color: #8b949e;">Error de conexión al cargar rutas</div>';
            return;
        }

        if (routes.length === 0) {
            activityList.innerHTML = '<div style="text-align: center; padding: 20px; color: #8b949e;">No hay rutas disponibles</div>';
            return;
        }

        // Ordenar rutas por fecha de actualización (más recientes primero)
        const sortedRoutes = routes
            .map(route => {
                // Normalizar fechas - pueden venir en diferentes formatos
                let dateToUse = null;
                if (route.updatedAt) {
                    dateToUse = new Date(route.updatedAt);
                } else if (route.createdAt) {
                    dateToUse = new Date(route.createdAt);
                } else if (route.fechaInicio) {
                    dateToUse = new Date(route.fechaInicio);
                }
                
                return {
                    ...route,
                    sortDate: dateToUse || new Date(0) // Si no hay fecha, usar fecha antigua
                };
            })
            .filter(r => r.sortDate.getTime() > 0) // Filtrar rutas sin fecha válida
            .sort((a, b) => b.sortDate - a.sortDate) // Ordenar descendente (más recientes primero)
            .slice(0, 4); // Solo las 4 más recientes

        if (sortedRoutes.length === 0) {
            activityList.innerHTML = '<div style="text-align: center; padding: 20px; color: #8b949e;">No hay actividad reciente</div>';
            return;
        }

        activityList.innerHTML = sortedRoutes.map(route => {
            // Usar datos que ya vienen en RouteResponse
            const driverName = route.nombreChofer && route.apellidoChofer 
                ? `${route.nombreChofer} ${route.apellidoChofer}`.trim()
                : (route.nombreChofer || route.apellidoChofer || 'Conductor desconocido');
            
            const initials = driverName.split(' ')
                .filter(n => n.length > 0)
                .map(n => n[0])
                .join('')
                .substring(0, 2)
                .toUpperCase() || '??';

            // Usar placaVehiculo que ya viene en RouteResponse, o buscar por ID si no está
            const vehicleCode = route.placaVehiculo || route.vehiculoId || 'N/A';

            // Determinar estado - puede venir como string o como objeto
            let estadoStr = route.estado;
            if (typeof route.estado === 'object' && route.estado !== null) {
                estadoStr = route.estado.name || route.estado.toString();
            }
            if (typeof estadoStr !== 'string') {
                estadoStr = String(estadoStr || 'PENDIENTE');
            }

            let status = 'pending';
            let statusText = 'Pendiente';
            let statusIcon = 'fa-clock';
            
            const estadoUpper = estadoStr.toUpperCase();
            if (estadoUpper === 'COMPLETADA' || estadoUpper === 'COMPLETADO') {
                status = 'completed';
                statusText = 'Completado';
                statusIcon = 'fa-check';
            } else if (estadoUpper === 'EN_PROGRESO' || estadoUpper === 'EN PROGRESO' || estadoUpper === 'IN_PROGRESS') {
                status = 'in-progress';
                statusText = 'En progreso';
                statusIcon = 'fa-clock';
            }

            // Calcular tiempo relativo
            const routeDate = route.sortDate || new Date(route.updatedAt || route.createdAt || route.fechaInicio);
            const now = new Date();
            const diffMs = now - routeDate;
            const diffMins = Math.floor(diffMs / 60000);
            const diffHours = Math.floor(diffMs / 3600000);
            const diffDays = Math.floor(diffMs / 86400000);

            let timeText = 'Hace un momento';
            if (diffMs < 0) {
                timeText = 'En el futuro';
            } else if (diffMins < 1) {
                timeText = 'Hace un momento';
            } else if (diffMins < 60) {
                timeText = `Hace ${diffMins} min`;
            } else if (diffHours < 24) {
                timeText = `Hace ${diffHours} ${diffHours === 1 ? 'hora' : 'horas'}`;
            } else if (diffDays < 7) {
                timeText = `Hace ${diffDays} ${diffDays === 1 ? 'día' : 'días'}`;
            } else {
                const diffWeeks = Math.floor(diffDays / 7);
                timeText = `Hace ${diffWeeks} ${diffWeeks === 1 ? 'semana' : 'semanas'}`;
            }

            const routeName = route.nombreRuta || route.codigo || 'Sin nombre';
            const details = `${vehicleCode} - ${routeName}`;

            return `
                <div class="activity-item">
                    <div class="activity-avatar">${initials}</div>
                    <div class="activity-content">
                        <div class="activity-name">${driverName}</div>
                        <div class="activity-details">${details}</div>
                    </div>
                    <div class="activity-status ${status}">
                        <i class="fas ${statusIcon}"></i>
                        ${statusText}
                    </div>
                    <div class="activity-time">${timeText}</div>
                </div>
            `;
        }).join('');
    } catch (error) {
        console.error('Error cargando actividad reciente:', error);
        const activityList = document.getElementById('activityList');
        if (activityList) {
            activityList.innerHTML = '<div style="text-align: center; padding: 20px; color: #8b949e;">Error al cargar actividad reciente: ' + error.message + '</div>';
        }
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
            window.location.href = 'index.html?logout=true';
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

