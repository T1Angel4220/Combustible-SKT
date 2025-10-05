// Configuration
const API_BASE_URL = 'http://localhost:8085/api/auth';

// State management
let currentUser = null;
let authToken = null;

// DOM elements
const loginSection = document.getElementById('loginSection');
const registerSection = document.getElementById('registerSection');
const dashboardSection = document.getElementById('dashboardSection');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const loginLink = document.getElementById('loginLink');
const registerLink = document.getElementById('registerLink');
const dashboardLink = document.getElementById('dashboardLink');
const logoutLink = document.getElementById('logoutLink');
const userInfo = document.getElementById('userInfo');
const messageContainer = document.getElementById('messageContainer');

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
});

function initializeApp() {
    // Check if user is already logged in
    const savedToken = localStorage.getItem('authToken');
    const savedUser = localStorage.getItem('currentUser');
    
    if (savedToken && savedUser && savedUser !== 'null') {
        try {
            authToken = savedToken;
            currentUser = JSON.parse(savedUser);
            showDashboard();
        } catch (error) {
            console.error('Error parsing saved user data:', error);
            // Limpiar datos corruptos
            localStorage.removeItem('authToken');
            localStorage.removeItem('currentUser');
            showLogin();
        }
    } else {
        showLogin();
    }
}

function setupEventListeners() {
    // Navigation
    loginLink.addEventListener('click', (e) => {
        e.preventDefault();
        showLogin();
    });
    
    registerLink.addEventListener('click', (e) => {
        e.preventDefault();
        showRegister();
    });
    
    dashboardLink.addEventListener('click', (e) => {
        e.preventDefault();
        showDashboard();
    });
    
    logoutLink.addEventListener('click', (e) => {
        e.preventDefault();
        logout();
    });
    
    // Forms
    loginForm.addEventListener('submit', handleLogin);
    registerForm.addEventListener('submit', handleRegister);
    
    // Dashboard buttons
    document.getElementById('validateTokenBtn').addEventListener('click', validateToken);
    document.getElementById('refreshTokenBtn').addEventListener('click', refreshToken);
}

// Navigation functions
function showLogin() {
    hideAllSections();
    loginSection.classList.add('active');
    updateNavigation('login');
}

function showRegister() {
    hideAllSections();
    registerSection.classList.add('active');
    updateNavigation('register');
}

function showDashboard() {
    hideAllSections();
    dashboardSection.classList.add('active');
    updateNavigation('dashboard');
    loadUserInfo();
}

function hideAllSections() {
    document.querySelectorAll('.form-section').forEach(section => {
        section.classList.remove('active');
    });
}

function updateNavigation(activeSection) {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    
    if (activeSection === 'login') {
        loginLink.classList.add('active');
        registerLink.style.display = 'block';
        dashboardLink.style.display = 'none';
        logoutLink.style.display = 'none';
    } else if (activeSection === 'register') {
        registerLink.classList.add('active');
        loginLink.style.display = 'block';
        dashboardLink.style.display = 'none';
        logoutLink.style.display = 'none';
    } else if (activeSection === 'dashboard') {
        dashboardLink.classList.add('active');
        loginLink.style.display = 'none';
        registerLink.style.display = 'none';
        dashboardLink.style.display = 'block';
        logoutLink.style.display = 'block';
    }
}

// Form handlers
async function handleLogin(e) {
    e.preventDefault();
    console.log('Iniciando proceso de login...');
    
    const formData = new FormData(loginForm);
    const loginData = {
        usernameOrEmail: formData.get('usernameOrEmail'),
        password: formData.get('password')
    };
    
    console.log('Datos de login:', loginData);
    
    try {
        showLoading(loginForm);
        console.log('Haciendo petición a:', `${API_BASE_URL}/login`);
        
        const response = await fetch(`${API_BASE_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(loginData)
        });
        
        console.log('Respuesta recibida:', response.status, response.statusText);
        
        const data = await response.json();
        console.log('Datos de respuesta:', data);
        
        if (response.ok) {
            authToken = data.token;
            currentUser = data.user;
            
            // Save to localStorage
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showMessage('Login exitoso! Bienvenido ' + currentUser.username, 'success');
            showDashboard();
        } else {
            showMessage(data.message || 'Error en el login', 'error');
        }
    } catch (error) {
        console.error('Error en login:', error);
        showMessage('Error de conexión: ' + error.message, 'error');
    } finally {
        hideLoading(loginForm);
    }
}

async function handleRegister(e) {
    e.preventDefault();
    console.log('Iniciando proceso de registro...');
    
    const formData = new FormData(registerForm);
    const password = formData.get('password');
    const confirmPassword = formData.get('confirmPassword');
    
    console.log('Datos del formulario:', {
        username: formData.get('username'),
        email: formData.get('email'),
        nombre: formData.get('nombre'),
        apellido: formData.get('apellido'),
        rol: formData.get('rol')
    });
    
    if (password !== confirmPassword) {
        showMessage('Las contraseñas no coinciden', 'error');
        return;
    }
    
    const registerData = {
        username: formData.get('username'),
        email: formData.get('email'),
        password: password,
        nombre: formData.get('nombre'),
        apellido: formData.get('apellido'),
        rol: formData.get('rol')
    };
    
    console.log('Enviando datos de registro:', registerData);
    
    try {
        showLoading(registerForm);
        console.log('Haciendo petición a:', `${API_BASE_URL}/register`);
        
        const response = await fetch(`${API_BASE_URL}/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(registerData)
        });
        
        console.log('Respuesta recibida:', response.status, response.statusText);
        
        const data = await response.json();
        console.log('Datos de respuesta:', data);
        
        if (response.ok) {
            showMessage('Registro exitoso! Ahora puedes iniciar sesión', 'success');
            registerForm.reset();
            showLogin();
        } else {
            showMessage(data.message || 'Error en el registro', 'error');
        }
    } catch (error) {
        console.error('Error en registro:', error);
        showMessage('Error de conexión: ' + error.message, 'error');
    } finally {
        hideLoading(registerForm);
    }
}

// Dashboard functions
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
        userRole.textContent = currentUser.rol;
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
            showMessage('Token válido!', 'success');
        } else {
            showMessage('Token inválido: ' + (data.message || 'Error desconocido'), 'error');
        }
    } catch (error) {
        showMessage('Error validando token: ' + error.message, 'error');
    }
}

async function refreshToken() {
    if (!authToken) {
        showMessage('No hay token para refrescar', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/me`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        const data = await response.json();
        
        if (response.ok) {
            currentUser = data;
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            loadUserInfo();
            showMessage('Información del usuario actualizada', 'success');
        } else {
            showMessage('Error refrescando información: ' + (data.message || 'Error desconocido'), 'error');
        }
    } catch (error) {
        showMessage('Error refrescando: ' + error.message, 'error');
    }
}

// Utility functions
function logout() {
    authToken = null;
    currentUser = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    showMessage('Sesión cerrada correctamente', 'info');
    showLogin();
}

function fillRegisterForm() {
    const timestamp = Date.now();
    document.getElementById('registerUsername').value = `testuser${timestamp}`;
    document.getElementById('registerEmail').value = `test${timestamp}@test.com`;
    document.getElementById('registerPassword').value = 'test123';
    document.getElementById('registerConfirmPassword').value = 'test123';
    document.getElementById('registerNombre').value = 'Test';
    document.getElementById('registerApellido').value = 'User';
    document.getElementById('registerRol').value = 'OPERADOR';
    
    console.log('Formulario de registro llenado con datos de prueba');
}

function showMessage(message, type = 'info') {
    const messageEl = document.createElement('div');
    messageEl.className = `message ${type}`;
    messageEl.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
        <span>${message}</span>
    `;
    
    messageContainer.appendChild(messageEl);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (messageEl.parentNode) {
            messageEl.parentNode.removeChild(messageEl);
        }
    }, 5000);
}

function showLoading(form) {
    form.classList.add('loading');
    const submitBtn = form.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Procesando...';
    }
}

function hideLoading(form) {
    form.classList.remove('loading');
    const submitBtn = form.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = false;
        const originalText = form.id === 'loginForm' ? 
            '<i class="fas fa-sign-in-alt"></i> Iniciar Sesión' : 
            '<i class="fas fa-user-plus"></i> Registrarse';
        submitBtn.innerHTML = originalText;
    }
}

// Health check function
async function checkServiceHealth() {
    try {
        const response = await fetch('http://localhost:8085/actuator/health');
        const data = await response.json();
        
        if (data.status === 'UP') {
            console.log('✅ Servicio funcionando correctamente');
        } else {
            console.warn('⚠️ Servicio con problemas:', data);
        }
    } catch (error) {
        console.error('❌ Error conectando con el servicio:', error);
        showMessage('Error conectando con el servicio. Verifica que esté ejecutándose en el puerto 8085.', 'error');
    }
}

// Check service health on load
checkServiceHealth();
