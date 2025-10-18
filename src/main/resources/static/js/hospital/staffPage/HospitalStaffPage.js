// Hospital Staff Page JavaScript loaded

// announcement
const cancelAnnouncement = document.querySelector(".announcement__cancel")
const announcement = document.querySelector(".announcement")

if (cancelAnnouncement) {
    cancelAnnouncement.addEventListener("click", function(){
        announcement.style.display = "none"
    })
}

// menu bar
const menuBar = document.querySelector(".navbar__section4")
const cancelMenubar = document.querySelector(".responsive__navbar__section1 p")
const responsiveNavbar = document.querySelector(".responsive__navbar")
const navbarLink = document.querySelectorAll(".responsive__navbar__section1 a")

if (menuBar) {
    menuBar.addEventListener("click", function(){
        responsiveNavbar.style.right = "0%"
    })
}

if (cancelMenubar) {
    cancelMenubar.addEventListener("click", function(){
        responsiveNavbar.style.right = "-80%"
    })
}

navbarLink.forEach(function(link){
    link.addEventListener("click", function(){
        responsiveNavbar.style.right = "-80%"
    })
})

// profile 
const profile = document.querySelector(".navbar__section3")
const cancelProfile = document.querySelector(".profile__close")
const profileBox = document.querySelector(".profile")

if (profile) {
    profile.addEventListener("click", function(){
        profileBox.style.right = "1%"
    })
}

if (cancelProfile) {
    cancelProfile.addEventListener("click", function(){
        profileBox.style.right = "-20%"
    })
}

// Staff Management Functions
let currentFilter = 'all';
let allStaffCards = [];

// Initialize staff cards when page loads
document.addEventListener('DOMContentLoaded', function() {
    allStaffCards = Array.from(document.querySelectorAll('.staff__section2__card'));
    updateStaffCount();
});

// Search functionality
function searchStaff() {
    const searchInput = document.getElementById('staffSearchInput');
    const searchTerm = searchInput.value.toLowerCase().trim();
    
    allStaffCards.forEach(card => {
        const staffName = card.querySelector('.staff-name').textContent.toLowerCase();
        const staffRole = card.querySelector('.staff-role').textContent.toLowerCase();
        const staffEmail = card.querySelector('.staff-email').textContent.toLowerCase();
        const staffSpecialization = card.querySelector('.staff-specialization')?.textContent.toLowerCase() || '';
        
        const matchesSearch = staffName.includes(searchTerm) || 
                            staffRole.includes(searchTerm) || 
                            staffEmail.includes(searchTerm) ||
                            staffSpecialization.includes(searchTerm);
        
        const matchesFilter = currentFilter === 'all' || 
                            card.getAttribute('data-role').toLowerCase() === currentFilter.toLowerCase();
        
        if (matchesSearch && matchesFilter) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
    
    updateStaffCount();
}

// Filter functionality
function filterStaff(filter) {
    currentFilter = filter;
    
    // Update active filter button
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // Apply filter
    allStaffCards.forEach(card => {
        const cardRole = card.getAttribute('data-role').toLowerCase();
        const matchesFilter = filter === 'all' || cardRole === filter.toLowerCase();
        
        if (matchesFilter) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
    
    // Re-apply search if there's a search term
    const searchInput = document.getElementById('staffSearchInput');
    if (searchInput.value.trim()) {
        searchStaff();
    }
    
    updateStaffCount();
}

// Update staff count display
function updateStaffCount() {
    const visibleCards = allStaffCards.filter(card => card.style.display !== 'none');
    const countElement = document.getElementById('totalStaffCount');
    if (countElement) {
        countElement.textContent = visibleCards.length;
    }
}

// Cancel staff connection function
function removeStaff(staffId, staffName) {
    console.log('Cancel function called with:', { staffId, staffName });
    
    // Show confirmation dialog with better styling
    const confirmed = confirm(`🚫 CANCEL STAFF CONNECTION\n\nAre you sure you want to cancel the connection with "${staffName}"?\n\nThis will remove them from your staff but keep their account intact for future connections.`);
    
    if (confirmed) {
        console.log('User confirmed cancellation');
        // Show loading state
        const cancelButton = event.target.closest('.remove-btn');
        const originalContent = cancelButton.innerHTML;
        cancelButton.innerHTML = '<div style="width: 16px; height: 16px; border: 2px solid #fff; border-top: 2px solid transparent; border-radius: 50%; animation: spin 1s linear infinite;"></div>';
        cancelButton.disabled = true;
        
        // Add CSS for loading animation
        if (!document.querySelector('#loading-styles')) {
            const style = document.createElement('style');
            style.id = 'loading-styles';
            style.textContent = '@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }';
            document.head.appendChild(style);
        }
        
        // Make API call to cancel staff connection
        const url = `/hospital/remove-staff/${staffId}`;
        console.log('Making DELETE request to:', url);
        
        fetch(url, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => {
            console.log('Response status:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Response data:', data);
            if (data.success) {
                // Find and remove the staff card using data attribute
                const cardToRemove = document.querySelector(`[data-staff-id="${staffId}"]`);
                
                if (cardToRemove) {
                    // Add fade out animation
                    cardToRemove.style.transition = 'all 0.3s ease';
                    cardToRemove.style.opacity = '0';
                    cardToRemove.style.transform = 'translateX(-100%)';
                    
                    setTimeout(() => {
                        cardToRemove.remove();
                        allStaffCards = Array.from(document.querySelectorAll('.staff__section2__card'));
                        updateStaffCount();
                        
                        // Show success message
                        showNotification('✅ Staff connection cancelled successfully!', 'success');
                    }, 300);
                } else {
                    // Fallback: reload page
                    showNotification('✅ Staff connection cancelled successfully!', 'success');
                    setTimeout(() => {
                        window.location.reload();
                    }, 1500);
                }
            } else {
                throw new Error(data.error || 'Failed to cancel staff connection');
            }
        })
        .catch(error => {
            console.error('Cancel error:', error);
            console.error('Error details:', {
                message: error.message,
                stack: error.stack,
                name: error.name
            });
            showNotification('❌ Error cancelling staff connection: ' + error.message, 'error');
        })
        .finally(() => {
            // Restore button state
            cancelButton.innerHTML = originalContent;
            cancelButton.disabled = false;
        });
    }
}

// Show notification function
function showNotification(message, type = 'info') {
    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.staff-notification');
    existingNotifications.forEach(notification => notification.remove());
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `staff-notification staff-notification-${type}`;
    notification.textContent = message;
    
    // Style the notification
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 8px;
        color: white;
        font-weight: 600;
        z-index: 10000;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transform: translateX(100%);
        transition: transform 0.3s ease;
        max-width: 400px;
        word-wrap: break-word;
    `;
    
    // Set background color based on type
    if (type === 'success') {
        notification.style.background = 'linear-gradient(135deg, #28a745 0%, #20c997 100%)';
    } else if (type === 'error') {
        notification.style.background = 'linear-gradient(135deg, #dc3545 0%, #e74c3c 100%)';
    } else {
        notification.style.background = 'linear-gradient(135deg, #1E489E 0%, #153a7a 100%)';
    }
    
    // Add to page
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // Auto remove after 4 seconds
    setTimeout(() => {
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 300);
    }, 4000);
}

// Clear search
function clearSearch() {
    const searchInput = document.getElementById('staffSearchInput');
    searchInput.value = '';
    searchStaff();
}