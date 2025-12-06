// Dashboard Script
let fuelChart, fleetChart;

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

document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    shareTokenWithServices();
    initializeCharts();
    loadDashboardData();
    loadRecentActivity();
    
    // Interceptar clics en enlaces externos para compartir token
    document.querySelectorAll('a[href^="http://localhost:8081"], a[href^="http://localhost:8082"], a[href^="http://localhost:8083"]').forEach(link => {
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
    // Fuel Consumption Chart
    const fuelCtx = document.getElementById('fuelConsumptionChart');
    if (fuelCtx) {
        fuelChart = new Chart(fuelCtx, {
            type: 'bar',
            data: {
                labels: ['Feb', 'Mar', 'Abr', 'May', 'Jun'],
                datasets: [{
                    label: 'Consumo (L)',
                    data: [8500, 9200, 8800, 9500, 9000],
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

    // Fleet Distribution Chart
    const fleetCtx = document.getElementById('fleetDistributionChart');
    if (fleetCtx) {
        fleetChart = new Chart(fleetCtx, {
            type: 'doughnut',
            data: {
                labels: ['Maquinaria Liviana', 'Maquinaria Pesada'],
                datasets: [{
                    data: [56, 68],
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
        // Load vehicles count
        const vehiclesRes = await fetch('http://localhost:8082/api/v1/vehicles', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        if (vehiclesRes.ok) {
            const vehicles = await vehiclesRes.json();
            document.getElementById('totalVehicles').textContent = vehicles.length || 124;
        }

        // Load drivers count
        const driversRes = await fetch('http://localhost:8081/api/v1/drivers', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        if (driversRes.ok) {
            const drivers = await driversRes.json();
            document.getElementById('activeDrivers').textContent = drivers.length || 87;
        }
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

function loadRecentActivity() {
    const activities = [
        {
            name: 'Juan Pérez',
            details: 'CAM-001 - Ruta Norte',
            status: 'completed',
            time: 'Hace 10 min'
        },
        {
            name: 'María González',
            details: 'EXC-024 - Ruta Sur',
            status: 'in-progress',
            time: 'Hace 25 min'
        },
        {
            name: 'Carlos Ruiz',
            details: 'CAM-008 - Ruta Este',
            status: 'completed',
            time: 'Hace 1 hora'
        },
        {
            name: 'Ana Silva',
            details: 'GRU-015 - Ruta Oeste',
            status: 'pending',
            time: 'Hace 2 horas'
        }
    ];

    const activityList = document.getElementById('activityList');
    if (!activityList) return;

    activityList.innerHTML = activities.map(activity => `
        <div class="activity-item">
            <div class="activity-avatar">${activity.name.split(' ').map(n => n[0]).join('')}</div>
            <div class="activity-content">
                <div class="activity-name">${activity.name}</div>
                <div class="activity-details">${activity.details}</div>
            </div>
            <div class="activity-status ${activity.status}">
                <i class="fas ${activity.status === 'completed' ? 'fa-check' : 'fa-clock'}"></i>
                ${activity.status === 'completed' ? 'Completado' : 
                  activity.status === 'in-progress' ? 'En progreso' : 'Pendiente'}
            </div>
            <div class="activity-time">${activity.time}</div>
        </div>
    `).join('');
}

function logout() {
    if (confirm('¿Estás seguro de que deseas cerrar sesión?')) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
        sessionStorage.removeItem('authToken');
        sessionStorage.removeItem('currentUser');
        window.location.href = 'index.html';
    }
}

