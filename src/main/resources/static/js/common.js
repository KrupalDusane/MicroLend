/**
 * MicroLend Common Javascript
 * Utility functions for UI interaction (Phase F1 - UI Foundation Only)
 */

document.addEventListener('DOMContentLoaded', () => {
    
    // Auth Check for protected routes
    const path = window.location.pathname;
    if (path.startsWith('/customer/') || path.startsWith('/officer/')) {
        if (!isAuthenticated()) {
            window.location.href = '/login';
            return;
        }

        const user = getAuthUser();
        if (!user) {
            // Need to fetch user info
            apiFetch('/api/auth/me')
                .then(res => res.json())
                .then(data => {
                    setAuthUser(data);
                    checkRoleAccess(path, data.role);
                })
                .catch(() => {
                    // apiFetch handles the redirect on 401
                });
        } else {
            // Check role access directly
            checkRoleAccess(path, user.role);
        }
        
        // Load global unread count
        loadUnreadCount();
    }
    
    const alertDismisses = document.querySelectorAll('.alert-dismissible .close');
    alertDismisses.forEach(btn => {
        btn.addEventListener('click', (e) => {
            const alert = e.target.closest('.alert');
            if (alert) {
                alert.style.display = 'none';
            }
        });
    });
});

function checkRoleAccess(path, role) {
    if (path.startsWith('/customer/') && role !== 'CUSTOMER') {
        window.location.href = '/error/403';
    } else if (path.startsWith('/officer/') && role !== 'LOAN_OFFICER') {
        window.location.href = '/error/403';
    }
}

/**
 * Format a number as currency for display only.
 * Actual authoritative values must come from the backend.
 */
function formatCurrency(amount) {
    if (amount === undefined || amount === null) return '₹0.00';
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR'
    }).format(amount);
}

function escapeHTML(str) {
    if (str === null || str === undefined) return '';
    return String(str).replace(/[&<>'"]/g, match => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        "'": '&#39;',
        '"': '&quot;'
    }[match]));
}

// ==========================================
// NOTIFICATIONS INTEGRATION (Phase F12)
// ==========================================

async function loadUnreadCount() {
    try {
        const res = await apiFetch('/api/notifications/unread-count');
        if (res.status === 200) {
            const data = await res.json();
            const count = data.unreadCount;
            
            const badgeC = document.getElementById('customerUnreadBadge');
            const badgeO = document.getElementById('officerUnreadBadge');
            
            if (badgeC) {
                badgeC.textContent = count;
                badgeC.style.display = count > 0 ? 'inline-block' : 'none';
            }
            if (badgeO) {
                badgeO.textContent = count;
                badgeO.style.display = count > 0 ? 'inline-block' : 'none';
            }
        }
    } catch (e) {
        // failed silently
    }
}

async function loadNotifications(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    
    container.innerHTML = '<div class="alert alert-info">Loading notifications...</div>';
    
    try {
        const res = await apiFetch('/api/notifications');
        if (res.status === 200) {
            const data = await res.json();
            const notifications = data.content; // It's a Page
            
            if (!notifications || notifications.length === 0) {
                container.innerHTML = '<div class="alert alert-secondary">No notifications yet.</div>';
                return;
            }
            
            container.innerHTML = '';
            notifications.forEach(notif => {
                const card = document.createElement('div');
                const isUnread = !notif.read; // isRead in JSON
                
                card.className = `alert ${isUnread ? 'alert-primary' : 'alert-secondary'}`;
                card.style.border = isUnread ? '1px solid var(--primary-color)' : '1px solid var(--border-color)';
                card.style.position = 'relative';
                
                const timeStr = new Date(notif.createdAt).toLocaleString();
                
                let html = `
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <strong>${isUnread ? '[Unread] ' : ''}${escapeHTML(notif.title)}</strong><br>
                            ${escapeHTML(notif.message)}
                            <div style="font-size: 0.8rem; margin-top: 5px; color: #666;">
                                ${timeStr} 
                                ${notif.readAt ? ' | Read: ' + new Date(notif.readAt).toLocaleString() : ''}
                            </div>
                        </div>
                `;
                
                if (isUnread) {
                    html += `
                        <button class="btn btn-sm btn-outline-primary" onclick="markNotificationRead(${notif.id}, this, '${containerId}')">
                            Mark as Read
                        </button>
                    `;
                }
                
                html += `</div>`;
                card.innerHTML = html;
                container.appendChild(card);
            });
        } else {
            container.innerHTML = '<div class="alert alert-danger">Failed to load notifications. Please try again.</div>';
        }
    } catch (e) {
        container.innerHTML = '<div class="alert alert-danger">An error occurred while loading notifications.</div>';
    }
}

async function markNotificationRead(id, btnElement, containerId) {
    if (btnElement) {
        btnElement.disabled = true;
        btnElement.textContent = 'Marking...';
    }
    
    try {
        const res = await apiFetch(`/api/notifications/${id}/read`, {
            method: 'PATCH'
        });
        
        if (res.status === 204) {
            // Success
            loadUnreadCount();
            loadNotifications(containerId);
        } else {
            alert('Failed to mark notification as read.');
            if (btnElement) {
                btnElement.disabled = false;
                btnElement.textContent = 'Mark as Read';
            }
        }
    } catch (e) {
        alert('An error occurred.');
        if (btnElement) {
            btnElement.disabled = false;
            btnElement.textContent = 'Mark as Read';
        }
    }
}

async function markAllNotificationsRead(containerId) {
    const btn = document.getElementById('markAllReadBtn');
    if (btn) {
        btn.disabled = true;
        btn.textContent = 'Processing...';
    }
    
    try {
        const res = await apiFetch(`/api/notifications/read-all`, {
            method: 'PATCH'
        });
        
        if (res.status === 204) {
            loadUnreadCount();
            loadNotifications(containerId);
        } else {
            alert('Failed to mark all as read.');
        }
    } catch (e) {
        alert('An error occurred.');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.textContent = 'Mark All as Read';
        }
    }
}
