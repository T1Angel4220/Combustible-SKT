// Configuración de la API - Detectar automáticamente si está en producción o desarrollo
function getGatewayUrl() {
    // Si estamos en Render o producción, usar el gateway
    if (window.location.hostname.includes('onrender.com')) {
        // El gateway está en combustible-gateway.onrender.com
        return 'https://combustible-gateway.onrender.com';
    }
    // Desarrollo local - usar gateway local
    return 'http://localhost:8090';
}

const GATEWAY_URL = getGatewayUrl();
// Para login, siempre usar el gateway
const API_BASE_URL = `${GATEWAY_URL}/api/v1/auth`;

// Variables globales
let currentUser = null;
let authToken = null;

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    checkAuthStatus();
    setupLoginForm();
});

function checkAuthStatus() {
    // Verificar si viene de un logout (parámetro en URL)
    const urlParams = new URLSearchParams(window.location.search);
    const isLogout = urlParams.get('logout') === 'true';
    
    // Si viene de un logout, limpiar cualquier token residual y no redirigir
    if (isLogout) {
        clearAuthData();
        // Limpiar el parámetro de la URL
        window.history.replaceState({}, document.title, window.location.pathname);
        return;
    }
    
    const savedToken = localStorage.getItem('authToken');
    const savedUser = localStorage.getItem('currentUser');
    
    if (savedToken && savedUser) {
        try {
            authToken = savedToken;
            currentUser = JSON.parse(savedUser);
            // Redirigir al dashboard principal (usar gateway)
            window.location.href = `${GATEWAY_URL}/dashboard.html`;
        } catch (error) {
            console.error('Error parsing saved user data:', error);
            clearAuthData();
        }
    }
}

function setupLoginForm() {
    const loginForm = document.getElementById('loginForm');
    
    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');
        
        const usernameOrEmail = emailInput.value.trim();
        const password = passwordInput.value.trim();
        
        // Validar que los campos no estén vacíos
        if (!usernameOrEmail) {
            showNotification('Por favor ingresa tu usuario o correo electrónico', 'error');
            emailInput.focus();
            return;
        }
        
        if (!password) {
            showNotification('Por favor ingresa tu contraseña', 'error');
            passwordInput.focus();
            return;
        }
        
        // Intentar login con username o email
        await handleLogin(usernameOrEmail, password);
    });
}

async function handleLogin(usernameOrEmail, password) {
    const loginButton = document.querySelector('.login-button');
    const originalText = loginButton.textContent;
    
    try {
        loginButton.disabled = true;
        loginButton.textContent = 'Iniciando sesión...';
        
        const loginData = {
            usernameOrEmail: usernameOrEmail,
            password: password
        };
        
        console.log('Enviando datos de login:', { usernameOrEmail: usernameOrEmail, password: '***' });
        
        const response = await fetch(`${GATEWAY_URL}/api/v1/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });
        
        const data = await response.json();
        
        if (response.ok && data.token) {
            authToken = data.token;
            currentUser = data.user || {
                username: usernameOrEmail,
                email: usernameOrEmail,
                role: data.role || 'OPERADOR'
            };
            
            // Guardar en localStorage
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showNotification('Sesión iniciada correctamente', 'success');
            
            // Redirigir después de un breve delay
            setTimeout(() => {
                window.location.href = `${GATEWAY_URL}/dashboard.html`;
            }, 1000);
        } else {
            throw new Error(data.message || 'Error al iniciar sesión');
        }
    } catch (error) {
        console.error('Login error:', error);
        showNotification(
            error.message || 'Error al iniciar sesión. Verifica tus credenciales.',
            'error'
        );
        loginButton.disabled = false;
        loginButton.textContent = originalText;
    }
}

function clearAuthData() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    authToken = null;
    currentUser = null;
}

function showNotification(message, type = 'info') {
    const container = document.getElementById('notificationContainer');
    if (!container) return;
    
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    
    const icon = type === 'success' ? 'fa-check-circle' :
                 type === 'error' ? 'fa-exclamation-circle' :
                 type === 'warning' ? 'fa-exclamation-triangle' :
                 'fa-info-circle';
    
    notification.innerHTML = `
        <i class="fas ${icon}"></i>
        <span>${message}</span>
    `;
    
    container.appendChild(notification);
    
    // Remover después de 5 segundos
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 5000);
}
