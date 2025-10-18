// User Management JavaScript

let userToDelete = null;

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    console.log('User Management page loaded');
    
    // Add any initialization code here
    initializePage();
    
    // Add event delegation for delete buttons
    setupDeleteButtonHandlers();
});

// Setup event delegation for delete buttons
function setupDeleteButtonHandlers() {
    // Use event delegation to handle delete button clicks
    document.addEventListener('click', function(event) {
        if (event.target.classList.contains('btn-delete')) {
            const email = event.target.getAttribute('data-email');
            const userType = event.target.getAttribute('data-user-type');
            
            if (email && userType) {
                deleteUser(email, userType);
            }
        }
    });
}

function initializePage() {
    // Add smooth scrolling for better UX
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

// Delete user function
function deleteUser(email, userType) {
    userToDelete = {
        email: email,
        type: userType
    };
    
    // Update modal content
    document.getElementById('deleteUserEmail').textContent = email;
    document.getElementById('deleteUserType').textContent = userType.charAt(0).toUpperCase() + userType.slice(1);
    
    // Show modal
    document.getElementById('deleteModal').style.display = 'block';
}

// Close delete modal
function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    userToDelete = null;
}

// Confirm delete
function confirmDelete() {
    if (!userToDelete) {
        showMessage('Error: No user selected for deletion', 'error');
        return;
    }
    
    // Show loading state
    const confirmBtn = document.querySelector('.btn-confirm-delete');
    const originalText = confirmBtn.textContent;
    confirmBtn.textContent = 'Deleting...';
    confirmBtn.disabled = true;
    
    // Make API call to delete user
    fetch('/admin/user-management/delete', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({
            email: userToDelete.email,
            userType: userToDelete.type
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            showMessage(data.message, 'success');
            
            // Remove the user row from the table
            removeUserFromTable(userToDelete.email, userToDelete.type);
            
            // Update statistics
            updateStatistics();
            
            // Close modal
            closeDeleteModal();
        } else {
            showMessage(data.error || 'Failed to delete user', 'error');
        }
    })
    .catch(error => {
        console.error('Error deleting user:', error);
        showMessage('An error occurred while deleting the user. Please try again.', 'error');
    })
    .finally(() => {
        // Reset button state
        confirmBtn.textContent = originalText;
        confirmBtn.disabled = false;
    });
}

// Remove user from table after successful deletion
function removeUserFromTable(email, userType) {
    const tables = document.querySelectorAll('.user-table tbody');
    
    tables.forEach(table => {
        const rows = table.querySelectorAll('tr');
        rows.forEach(row => {
            const emailCell = row.querySelector('td:nth-child(2)'); // Email is usually the 2nd column
            if (emailCell && emailCell.textContent.trim() === email) {
                // Add fade out animation
                row.style.transition = 'opacity 0.3s ease';
                row.style.opacity = '0';
                
                setTimeout(() => {
                    row.remove();
                }, 300);
            }
        });
    });
}

// Update statistics after user deletion
function updateStatistics() {
    // This would typically involve making an API call to get updated statistics
    // For now, we'll just update the counts in the UI
    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach(card => {
        const countElement = card.querySelector('h3');
        if (countElement) {
            const currentCount = parseInt(countElement.textContent);
            if (currentCount > 0) {
                countElement.textContent = currentCount - 1;
            }
        }
    });
}

// Show success/error messages
function showMessage(message, type) {
    const messageContainer = document.getElementById('messageContainer');
    
    // Create message element
    const messageElement = document.createElement('div');
    messageElement.className = `message message-${type}`;
    messageElement.textContent = message;
    
    // Add to container
    messageContainer.appendChild(messageElement);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        messageElement.style.animation = 'messageSlideOut 0.3s ease';
        setTimeout(() => {
            if (messageElement.parentNode) {
                messageElement.parentNode.removeChild(messageElement);
            }
        }, 300);
    }, 5000);
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('deleteModal');
    if (event.target === modal) {
        closeDeleteModal();
    }
}

// Handle escape key to close modal
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeDeleteModal();
    }
});

// Add CSS for message slide out animation
const style = document.createElement('style');
style.textContent = `
    @keyframes messageSlideOut {
        from {
            opacity: 1;
            transform: translateX(0);
        }
        to {
            opacity: 0;
            transform: translateX(100%);
        }
    }
`;
document.head.appendChild(style);

// Table search functionality (optional enhancement)
function addTableSearch() {
    const tables = document.querySelectorAll('.user-table');
    
    tables.forEach(table => {
        const header = table.closest('.table-section').querySelector('.table-header');
        const searchInput = document.createElement('input');
        searchInput.type = 'text';
        searchInput.placeholder = 'Search...';
        searchInput.className = 'table-search';
        searchInput.style.cssText = `
            padding: 0.5rem;
            border: 1px solid #ddd;
            border-radius: 4px;
            margin-left: auto;
            width: 200px;
        `;
        
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            const rows = table.querySelectorAll('tbody tr');
            
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                if (text.includes(searchTerm)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
        
        header.appendChild(searchInput);
    });
}

// Initialize table search if needed
// addTableSearch();

// Export functions for potential external use
window.UserManagement = {
    deleteUser,
    closeDeleteModal,
    confirmDelete,
    showMessage
};
