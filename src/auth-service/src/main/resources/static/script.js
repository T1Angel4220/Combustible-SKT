// Configuración de la API
const API_BASE_URL = 'http://localhost:8085/api/auth';

// Variables globales
let currentUser = null;
let authToken = null;

// Elementos del DOM
let loginSection, registerSection, dashboardSection;
let loginBtn, registerBtn, dashboardBtn, logoutBtn;

// Inicialización de la aplicación
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
});

function initializeApp() {
    // Obtener elementos del DOM
    loginSection = document.getElementById('loginSection');
    registerSection = document.getElementById('registerSection');
    dashboardSection = document.getElementById('dashboardSection');
    
    loginBtn = document.getElementById('loginBtn');
    registerBtn = document.getElementById('registerBtn');
    dashboardBtn = document.getElementById('dashboardBtn');
    logoutBtn = document.getElementById('logoutBtn');
    
    // Cargar datos del localStorage
    const savedToken = localStorage.getItem('authToken');
    const savedUser = localStorage.getItem('currentUser');
    
    if (savedToken && savedUser) {
        try {
            authToken = savedToken;
            currentUser = JSON.parse(savedUser);
            showDashboard();
        } catch (error) {
            console.error('Error parsing saved user data:', error);
            clearAuthData();
        }
    } else {
        showLogin();
    }
}

function setupEventListeners() {
    // Event listeners para navegación
    loginBtn.addEventListener('click', showLogin);
    registerBtn.addEventListener('click', showRegister);
    dashboardBtn.addEventListener('click', showDashboard);
    logoutBtn.addEventListener('click', logout);
    
    // Event listeners para formularios
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
}

// Funciones de navegación
function showLogin() {
    hideAllSections();
    loginSection.classList.add('active');
    updateNavButtons('login');
}

function showRegister() {
    hideAllSections();
    registerSection.classList.add('active');
    updateNavButtons('register');
}

function showDashboard() {
    hideAllSections();
    dashboardSection.classList.add('active');
    updateNavButtons('dashboard');
    loadUserInfo();
}

function hideAllSections() {
    loginSection.classList.remove('active');
    registerSection.classList.remove('active');
    dashboardSection.classList.remove('active');
}

function updateNavButtons(activeSection) {
    // Resetear todos los botones
    loginBtn.classList.remove('active');
    registerBtn.classList.remove('active');
    
    // Mostrar/ocultar botones según el estado
    if (activeSection === 'dashboard') {
        loginBtn.style.display = 'none';
        registerBtn.style.display = 'none';
        dashboardBtn.style.display = 'inline-flex';
        logoutBtn.style.display = 'inline-flex';
    } else {
        loginBtn.style.display = 'inline-flex';
        registerBtn.style.display = 'inline-flex';
        dashboardBtn.style.display = 'none';
        logoutBtn.style.display = 'none';
        
        // Activar el botón correspondiente
        if (activeSection === 'login') {
            loginBtn.classList.add('active');
        } else if (activeSection === 'register') {
            registerBtn.classList.add('active');
        }
    }
}

// Funciones de autenticación
async function handleLogin(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const loginData = {
        usernameOrEmail: formData.get('usernameOrEmail'),
        password: formData.get('password')
    };
    
    console.log('Datos de login:', loginData);
    
    try {
        showMessage('Iniciando sesión...', 'info');
        
        const response = await fetch(`${API_BASE_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });
        
        const data = await response.json();
        
        if (response.ok) {
            authToken = data.token;
            currentUser = data.user;
            
            // Guardar en localStorage
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showMessage('¡Inicio de sesión exitoso!', 'success');
            showDashboard();
        } else {
            showMessage('Error: ' + (data.message || 'Credenciales inválidas'), 'error');
        }
    } catch (error) {
        console.error('Error en login:', error);
        showMessage('Error de conexión al iniciar sesión', 'error');
    }
}

async function handleRegister(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const registerData = {
        username: formData.get('username'),
        email: formData.get('email'),
        nombre: formData.get('nombre'),
        apellido: formData.get('apellido'),
        password: formData.get('password'),
        rol: formData.get('rol')
    };
    
    console.log('Datos de registro:', registerData);
    
    try {
        showMessage('Registrando usuario...', 'info');
        
        const response = await fetch(`${API_BASE_URL}/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(registerData)
        });
        
        const data = await response.json();
        
        if (response.ok) {
            authToken = data.token;
            currentUser = data.user;
            
            // Guardar en localStorage
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showMessage('¡Registro exitoso!', 'success');
            showDashboard();
        } else {
            showMessage('Error: ' + (data.message || 'No se pudo completar el registro'), 'error');
        }
    } catch (error) {
        console.error('Error en registro:', error);
        showMessage('Error de conexión al registrar usuario', 'error');
    }
}

function logout() {
    clearAuthData();
    showMessage('Sesión cerrada correctamente', 'info');
    showLogin();
}

function clearAuthData() {
    authToken = null;
    currentUser = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
}

// Funciones del dashboard
function loadUserInfo() {
    if (!currentUser) return;
    
    // Actualizar el mensaje de bienvenida
    const welcomeMessage = document.getElementById('welcomeMessage');
    const userDetails = document.getElementById('userDetails');
    const userRole = document.getElementById('userRole');
    
    if (welcomeMessage) {
        welcomeMessage.textContent = `¡Bienvenido, ${currentUser.nombre}!`;
    }
    
    if (userDetails) {
        userDetails.textContent = `${currentUser.nombre} ${currentUser.apellido} (${currentUser.username})`;
    }
    
    if (userRole) {
        userRole.textContent = `Rol: ${currentUser.rol}`;
    }
    
    console.log('Información del usuario cargada:', currentUser);
}

async function getCurrentUser() {
    if (!authToken) {
        showMessage('No hay token para obtener información del usuario', 'error');
        return;
    }
    
    try {
        showMessage('Obteniendo información del usuario...', 'info');
        
        const response = await fetch(`${API_BASE_URL}/me`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        
        if (response.ok) {
            currentUser = data;
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            loadUserInfo();
            showMessage('Información del usuario obtenida correctamente', 'success');
        } else {
            showMessage('Error obteniendo información: ' + (data.message || 'Error desconocido'), 'error');
        }
    } catch (error) {
        console.error('Error obteniendo información del usuario:', error);
        showMessage('Error de conexión al obtener información del usuario', 'error');
    }
}

async function validateToken() {
    if (!authToken) {
        showMessage('No hay token para validar', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/validate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({ token: authToken })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            showMessage('✅ Token válido - Sesión activa', 'success');
        } else {
            showMessage('❌ Token inválido: ' + (data.message || 'Error desconocido'), 'error');
        }
    } catch (error) {
        showMessage('Error validando token: ' + error.message, 'error');
    }
}

// Función para llenar datos de prueba en el registro
function fillRegisterForm() {
    document.getElementById('registerUsername').value = 'testuser' + Math.floor(Math.random() * 1000);
    document.getElementById('registerEmail').value = 'test' + Math.floor(Math.random() * 1000) + '@skt.com';
    document.getElementById('registerNombre').value = 'Usuario';
    document.getElementById('registerApellido').value = 'Prueba';
    document.getElementById('registerPassword').value = 'password123';
    document.getElementById('registerRol').value = 'OPERADOR';
    
    showMessage('Datos de prueba cargados', 'info');
}

// Función para ir a la gestión de vehículos
function goToVehiclesManagement() {
    // Verificar que el usuario esté autenticado
    if (!authToken) {
        showMessage('Debes iniciar sesión para acceder a la gestión de vehículos', 'error');
        return;
    }
    
    showMessage('Redirigiendo a la gestión de vehículos...', 'info');
    
    // Pasar el token y datos del usuario como parámetros URL
    const tokenParam = encodeURIComponent(authToken);
    const userParam = encodeURIComponent(JSON.stringify(currentUser));
    
    setTimeout(() => {
        window.location.href = `http://localhost:8082/vehicles-simple.html?token=${tokenParam}&user=${userParam}`;
    }, 1000);
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
    
    // Auto-remover después de 5 segundos
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

// Funciones de utilidad
function formatDate(date) {
    return new Date(date).toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP'
    }).format(amount);
}

// Manejo de errores globales
window.addEventListener('error', function(event) {
    console.error('Error global:', event.error);
    showMessage('Ha ocurrido un error inesperado', 'error');
});

// Manejo de promesas rechazadas
window.addEventListener('unhandledrejection', function(event) {
    console.error('Promesa rechazada:', event.reason);
    showMessage('Error de conexión con el servidor', 'error');
});

// Inicialización de la aplicación sin verificación automática de conexión
document.addEventListener('DOMContentLoaded', function() {
    console.log('SKT Combustible - Sistema de Autenticación iniciado');
    console.log('Servidor disponible en: http://localhost:8085');
});
