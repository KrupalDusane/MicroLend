/**
 * MicroLend Auth Javascript
 * Phase F2 - Authentication Integration
 */

const AUTH_TOKEN_KEY = 'ml_jwt_token';
const AUTH_USER_KEY = 'ml_user_info';

function setToken(token) {
    localStorage.setItem(AUTH_TOKEN_KEY, token);
}

function getToken() {
    return localStorage.getItem(AUTH_TOKEN_KEY);
}

function clearToken() {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(AUTH_USER_KEY);
}

function isAuthenticated() {
    return !!getToken();
}

function setAuthUser(user) {
    localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
}

function getAuthUser() {
    const user = localStorage.getItem(AUTH_USER_KEY);
    return user ? JSON.parse(user) : null;
}

/**
 * Centralized API Fetch utility that injects the Authorization header
 * and handles global 401/403 responses.
 */
async function apiFetch(url, options = {}) {
    if (!options.headers) {
        options.headers = {};
    }
    
    if (isAuthenticated()) {
        options.headers['Authorization'] = 'Bearer ' + getToken();
    }
    
    options.headers['Content-Type'] = 'application/json';

    try {
        const response = await fetch(url, options);
        
        if (response.status === 401) {
            console.warn("401 Unauthorized - Clearing token and redirecting to login.");
            clearToken();
            window.location.href = '/login';
            throw new Error("Unauthorized");
        } else if (response.status === 403) {
            console.warn("403 Forbidden - Access Denied.");
            // We could redirect to /403, or just throw
            window.location.href = '/error/403'; // wait, Thymeleaf doesn't map /error/403 natively unless we added it
            throw new Error("Forbidden");
        }
        
        return response;
    } catch (error) {
        console.error("API Fetch Error:", error);
        throw error;
    }
}

function logout() {
    clearToken();
    window.location.href = '/login';
}

document.addEventListener('DOMContentLoaded', () => {
    
    
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            
            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });
                
                if (response.ok) {
                    const data = await response.json();
                    setToken(data.token);
                    setAuthUser(data.user);
                    
                    // Role-based redirection based strictly on the backend response
                    if (data.user.role === 'LOAN_OFFICER') {
                        window.location.href = '/officer/dashboard';
                    } else if (data.user.role === 'CUSTOMER') {
                        window.location.href = '/customer/dashboard';
                    } else {
                        // Unknown role
                        window.location.href = '/';
                    }
                } else {
                    const errData = await response.json().catch(() => ({}));
                    alert("Login Failed: " + (errData.message || "Invalid credentials"));
                }
            } catch (error) {
                alert("An error occurred during login.");
            }
        });
    }

    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const name = document.getElementById('name').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            
            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name, email, password })
                });
                
                if (response.status === 201) {
                    alert("Registration successful! Please login.");
                    window.location.href = '/login';
                } else {
                    const errData = await response.json().catch(() => ({}));
                    alert("Registration Failed: " + (errData.message || "Invalid input"));
                }
            } catch (error) {
                alert("An error occurred during registration.");
            }
        });
    }
});
