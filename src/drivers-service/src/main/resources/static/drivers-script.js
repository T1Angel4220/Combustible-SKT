// Drivers Management Script
const API_BASE_URL = 'http://localhost:8081/api/v1/drivers';

let drivers = [];
let editingDriverId = null;

document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    loadDrivers();
    setupEventListeners();
});

function checkAuth() {
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
}

function setupEventListeners() {
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');
    
    if (searchInput) {
        searchInput.addEventListener('input', filterDrivers);
    }
    
    if (statusFilter) {
        statusFilter.addEventListener('change', filterDrivers);
    }
    
    const driverForm = document.getElementById('driverForm');
    if (driverForm) {
        driverForm.addEventListener('submit', handleDriverSubmit);
    }
}

async function loadDrivers() {
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(API_BASE_URL, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            drivers = Array.isArray(data) ? data : (data.drivers || []);
            updateMetrics();
            renderDrivers();
        } else {
            console.error('Error loading drivers');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

function updateMetrics() {
    const total = drivers.length;
    const available = drivers.filter(d => d.estado === 'DISPONIBLE').length;
    const inRoute = drivers.filter(d => d.estado === 'EN_RUTA').length;
    const outOfService = drivers.filter(d => !d.activo || d.estado === 'LICENCIA' || d.estado === 'VACACIONES').length;
    
    document.getElementById('totalDrivers').textContent = total;
    document.getElementById('availableDrivers').textContent = available;
    document.getElementById('inRouteDrivers').textContent = inRoute;
    document.getElementById('outOfServiceDrivers').textContent = outOfService;
}

function filterDrivers() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const statusFilter = document.getElementById('statusFilter').value;
    
    let filtered = drivers;
    
    if (searchTerm) {
        filtered = filtered.filter(d => 
            d.nombre?.toLowerCase().includes(searchTerm) ||
            d.apellido?.toLowerCase().includes(searchTerm) ||
            d.email?.toLowerCase().includes(searchTerm)
        );
    }
    
    if (statusFilter) {
        filtered = filtered.filter(d => d.estado === statusFilter);
    }
    
    renderDrivers(filtered);
}

function renderDrivers(driversToRender = drivers) {
    const tbody = document.getElementById('driversTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = driversToRender.map(driver => {
        const initials = `${driver.nombre?.[0] || ''}${driver.apellido?.[0] || ''}`.toUpperCase();
        const fullName = `${driver.nombre || ''} ${driver.apellido || ''}`.trim();
        
        const licenseType = getLicenseType(driver.tipoMaquinariaAsignada);
        const statusClass = getStatusClass(driver.estado);
    const statusText = getStatusText(driver.estado);
    
    return `
            <tr>
                <td>
            <div class="driver-info">
                        <div class="driver-avatar">${initials}</div>
                        <div class="driver-name">${fullName}</div>
                </div>
                </td>
                <td>
                    <div class="contact-info">
                        <div class="contact-email">${driver.email || 'N/A'}</div>
                        <div class="contact-phone">${driver.telefono || 'N/A'}</div>
                </div>
                </td>
                <td>
                    <span class="license-badge ${licenseType.class}">${licenseType.text}</span>
                </td>
                <td>5 años</td>
                <td>
                    <span class="status-badge ${statusClass}">${statusText}</span>
                </td>
                <td>
                    <span class="vehicle-assigned">CAM-001</span>
                </td>
                <td>
                    <div class="table-actions">
                        <button class="action-btn edit" onclick="editDriver('${driver.id}')">
                            <i class="fas fa-edit"></i>
                </button>
                        <button class="action-btn delete" onclick="deleteDriver('${driver.id}')">
                            <i class="fas fa-trash"></i>
                </button>
            </div>
                </td>
            </tr>
        `;
    }).join('');
}

function getLicenseType(tipo) {
    if (!tipo) return { class: 'liviana', text: 'Liviana' };
    const pesados = ['EXCAVADORA', 'CARGADOR', 'GRUA', 'MOTONIVELADORA'];
    if (pesados.includes(tipo)) return { class: 'pesada', text: 'Pesada' };
    return { class: 'liviana', text: 'Liviana' };
}

function getStatusClass(estado) {
    if (estado === 'EN_RUTA') return 'en-ruta';
    if (estado === 'DISPONIBLE') return 'disponible';
    return 'fuera-servicio';
}

function getStatusText(estado) {
    const statusMap = {
        'DISPONIBLE': 'Disponible',
        'ASIGNADO': 'Asignado',
        'EN_RUTA': 'En Ruta',
        'DESCANSANDO': 'Descansando',
        'VACACIONES': 'En Vacaciones',
        'ENFERMO': 'Enfermo',
        'LICENCIA': 'En Licencia'
    };
    return statusMap[estado] || estado;
}

function showAddDriverModal() {
    document.getElementById('addDriverModal').classList.add('active');
}

function closeAddDriverModal() {
    document.getElementById('addDriverModal').classList.remove('active');
    document.getElementById('driverForm').reset();
    editingDriverId = null;
    // Cambiar el título del modal
    document.querySelector('#addDriverModal .modal-header h2').textContent = 'Agregar Chofer';
}

async function handleDriverSubmit(e) {
    e.preventDefault();
    
    // Construir objeto de datos, solo incluyendo campos con valores
    const driverData = {
        nombre: document.getElementById('driverNombre').value.trim(),
        apellido: document.getElementById('driverApellido').value.trim(),
        dni: document.getElementById('driverDni').value.trim(),
        licencia: document.getElementById('driverLicencia').value.trim(),
        estado: document.getElementById('driverEstado').value
    };
    
    // Agregar campos opcionales solo si tienen valor
    const email = document.getElementById('driverEmail').value.trim();
    if (email) {
        driverData.email = email;
    }
    
    const telefono = document.getElementById('driverTelefono').value.trim();
    if (telefono) {
        driverData.telefono = telefono;
    }
    
    const tipoMaquinaria = document.getElementById('driverTipoMaquinaria').value.trim();
    if (tipoMaquinaria) {
        driverData.tipoMaquinariaAsignada = tipoMaquinaria;
    }
    
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const url = editingDriverId ? `${API_BASE_URL}/${editingDriverId}` : API_BASE_URL;
        const method = editingDriverId ? 'PUT' : 'POST';
        
        console.log(`${method} ${url}`, driverData);
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(driverData)
        });
        
        if (response.ok) {
            closeAddDriverModal();
            loadDrivers();
            showNotification('success', 'Éxito', editingDriverId ? 'Chofer actualizado correctamente' : 'Chofer agregado correctamente');
        } else {
            const errorText = await response.text();
            console.error('Error response:', response.status, errorText);
            let errorMessage = 'Error al procesar la solicitud';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = editingDriverId ? 'Error al actualizar chofer' : 'Error al agregar chofer';
            }
            showNotification('error', 'Error', errorMessage);
        }
    } catch (error) {
        console.error('Error:', error);
        alert(editingDriverId ? 'Error al actualizar chofer' : 'Error al agregar chofer');
    }
}

function editDriver(id) {
    const driver = drivers.find(d => d.id === id);
    if (!driver) return;
    
    // Establecer el ID del chofer que se está editando
    editingDriverId = id;
    
    // Cambiar el título del modal
    document.querySelector('#addDriverModal .modal-header h2').textContent = 'Editar Chofer';
    
    // Populate form and show modal
    document.getElementById('driverNombre').value = driver.nombre || '';
    document.getElementById('driverApellido').value = driver.apellido || '';
    document.getElementById('driverDni').value = driver.dni || '';
    document.getElementById('driverLicencia').value = driver.licencia || '';
    document.getElementById('driverEmail').value = driver.email || '';
    document.getElementById('driverTelefono').value = driver.telefono || '';
    document.getElementById('driverTipoMaquinaria').value = driver.tipoMaquinariaAsignada || '';
    document.getElementById('driverEstado').value = driver.estado || 'DISPONIBLE';
    
    showAddDriverModal();
}

async function deleteDriver(id) {
    if (!confirm('¿Estás seguro de eliminar este chofer?')) return;
    
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok || response.status === 204) {
            loadDrivers();
            showNotification('success', 'Éxito', 'Chofer eliminado correctamente');
        } else {
            const errorText = await response.text();
            console.error('Error response:', response.status, errorText);
            let errorMessage = 'Error al eliminar chofer';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = `Error al eliminar chofer: ${response.status}`;
            }
            showNotification('error', 'Error', errorMessage);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error al eliminar chofer');
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

function logout() {
    if (confirm('¿Estás seguro de cerrar sesión?')) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
        sessionStorage.removeItem('authToken');
        sessionStorage.removeItem('currentUser');
        window.location.href = 'http://localhost:8085/';
    }
}
