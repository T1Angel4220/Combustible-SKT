// Configuración de la API
const API_BASE_URL = 'http://localhost:8085/api/auth';
const GATEWAY_URL = 'http://localhost:8090/api/v1/auth';

// Variables globales
let currentUser = null;
let authToken = null;

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    checkAuthStatus();
    setupLoginForm();
});

function checkAuthStatus() {
    const savedToken = localStorage.getItem('authToken');
    const savedUser = localStorage.getItem('currentUser');
    
    if (savedToken && savedUser) {
        try {
            authToken = savedToken;
            currentUser = JSON.parse(savedUser);
            // Redirigir al dashboard principal
            window.location.href = 'http://localhost:8085/dashboard.html';
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
        
        const response = await fetch(`${API_BASE_URL}/login`, {
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
                window.location.href = 'http://localhost:8085/dashboard.html';
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
