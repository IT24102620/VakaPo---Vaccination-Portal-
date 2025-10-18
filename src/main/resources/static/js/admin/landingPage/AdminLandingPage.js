document.addEventListener('DOMContentLoaded', function() {
    setupSidebar();
    setupCSRF();
    setupCardHoverEffects();
});

/* Initialization Functions */
function setupCSRF() {
    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    // Add CSRF token to all AJAX requests
    axios.interceptors.request.use(function (config) {
        config.headers[csrfHeader] = csrfToken;
        return config;
    }, function (error) {
        console.error('Error setting up CSRF:', error);
        return Promise.reject(error);
    });
}

function setupSidebar() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebarClose = document.getElementById('sidebarClose');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');

    // Show/hide menu on mobile
    menuToggle?.addEventListener('click', toggleSidebar);
    sidebarClose?.addEventListener('click', toggleSidebar);
    overlay?.addEventListener('click', toggleSidebar);

    // Handle window resize
    window.addEventListener('resize', function() {
        if (window.innerWidth > 768) {
            sidebar.classList.remove('active');
            overlay.style.display = 'none';
        }
    });
}

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');
    
    sidebar.classList.toggle('active');
    overlay.style.display = sidebar.classList.contains('active') ? 'block' : 'none';
}


/* Card Actions */
document.querySelectorAll('.btn-view').forEach(button => {
    button.addEventListener('click', function(event) {
        event.preventDefault();
        const action = this.getAttribute('data-action');
        
        // Add loading state
        const originalText = this.textContent;
        this.textContent = 'Loading...';
        this.disabled = true;
        
        switch(action) {
            case 'manage-hospitals':
                window.location.href = '/admin/user-management';
                break;
            case 'pending-requests':
                window.location.href = '/admin/sub-admin-request';
                break;
            case 'vaccine-manager':
                window.location.href = '/admin/vaccines/manage';
                break;
            case 'vaccination-monitoring':
                window.location.href = '/admin/vaccination-monitoring';
                break;
            case 'view-feedback':
                window.location.href = '/admin/feedback-manager';
                break;
            case 'messages-alerts':
                window.location.href = '/admin/notifications';
                break;
            default:
                console.warn('Unknown action:', action);
                // Re-enable button if action is unknown
                this.textContent = originalText;
                this.disabled = false;
        }
    });
});


/* Card Hover Effects */
function setupCardHoverEffects() {
    const cards = document.querySelectorAll('.card');
    
    cards.forEach(card => {
        const button = card.querySelector('.btn-view');
        
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 8px 25px rgba(0,0,0,0.15)';
            button.style.transform = 'scale(1.05)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 4px 15px rgba(0,0,0,0.1)';
            button.style.transform = 'scale(1)';
        });
    });
}

/* Enhanced Button Click Effects */
document.querySelectorAll('.btn-view').forEach(button => {
    button.addEventListener('click', function() {
        // Add click animation
        this.style.transform = 'scale(0.95)';
        setTimeout(() => {
            this.style.transform = 'scale(1)';
        }, 150);
    });
});