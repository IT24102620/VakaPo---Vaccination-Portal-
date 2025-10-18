// Admin Feedback Page JavaScript

// Global variables
let currentPage = 0;
let currentSize = 10;
let currentFilters = {
    status: '',
    search: '',
    sortBy: 'submittedAt',
    sortDir: 'desc'
};

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    initializeSidebar();
    loadFeedback();
    setupEventListeners();
    checkUrlParams();
});

// Sidebar functionality
function initializeSidebar() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    const sidebarClose = document.getElementById('sidebarClose');
    const overlay = document.getElementById('overlay');

    if (menuToggle) {
        menuToggle.addEventListener('click', function() {
            sidebar.classList.add('active');
            overlay.style.display = 'block';
        });
    }

    if (sidebarClose) {
        sidebarClose.addEventListener('click', function() {
            sidebar.classList.remove('active');
            overlay.style.display = 'none';
        });
    }

    if (overlay) {
        overlay.addEventListener('click', function() {
            sidebar.classList.remove('active');
            overlay.style.display = 'none';
        });
    }
}

// Setup event listeners
function setupEventListeners() {
    // Status update form
    const statusUpdateForm = document.getElementById('statusUpdateForm');
    if (statusUpdateForm) {
        statusUpdateForm.addEventListener('submit', handleStatusUpdate);
    }

    // Search input with debounce
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                handleSearch();
            }, 500);
        });
    }
}

// Check URL parameters for initial state
function checkUrlParams() {
    const urlParams = new URLSearchParams(window.location.search);
    
    if (urlParams.get('success')) {
        showAlert(urlParams.get('success'), 'success');
    }
    
    if (urlParams.get('error')) {
        showAlert(urlParams.get('error'), 'error');
    }
    
    // Set current status filter from URL
    const statusParam = urlParams.get('status');
    if (statusParam) {
        const statusFilter = document.getElementById('statusFilter');
        if (statusFilter) {
            statusFilter.value = statusParam.toUpperCase();
            currentFilters.status = statusParam.toUpperCase();
        }
    }
}

// Load feedback data
async function loadFeedback() {
    try {
        showLoading();
        
        const params = new URLSearchParams({
            page: currentPage,
            size: currentSize,
            sortBy: currentFilters.sortBy,
            sortDir: currentFilters.sortDir
        });
        
        if (currentFilters.status) {
            params.append('status', currentFilters.status);
        }
        
        if (currentFilters.search) {
            params.append('search', currentFilters.search);
        }
        
        const response = await fetch(`/admin/api/feedback?${params}`);
        const result = await response.json();
        
        if (result.success) {
            updateFeedbackTable(result.data);
            updatePagination();
        } else {
            showAlert(result.message || 'Failed to load feedback', 'error');
        }
        
    } catch (error) {
        console.error('Error loading feedback:', error);
        showAlert('Failed to load feedback', 'error');
    } finally {
        hideLoading();
    }
}

// Update feedback table
function updateFeedbackTable(feedbackList) {
    const tbody = document.getElementById('feedbackTableBody');
    if (!tbody) return;
    
    if (!feedbackList || feedbackList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="no-data">No feedback found</td></tr>';
        return;
    }
    
    tbody.innerHTML = feedbackList.map(feedback => `
        <tr>
            <td>${feedback.id}</td>
            <td>${escapeHtml(feedback.name)}</td>
            <td>${escapeHtml(feedback.email)}</td>
            <td>
                <div class="rating-display" data-rating="${feedback.rating}">
                    ${generateStars(feedback.rating)}
                </div>
            </td>
            <td>
                <span class="status-badge status-${feedback.status.toLowerCase()}">
                    ${feedback.status}
                </span>
            </td>
            <td>${formatDate(feedback.submittedAt)}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-sm btn-view" onclick="viewFeedback(${feedback.id})" title="View Details">
                        <img src="/Images/view.png" alt="View">
                    </button>
                    <button class="btn btn-sm btn-edit" onclick="updateFeedbackStatus(${feedback.id})" title="Update Status">
                        <img src="/Images/edit.png" alt="Update">
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Generate star rating display
function generateStars(rating) {
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        const filled = i <= rating ? 'filled' : '';
        stars += `<span class="star ${filled}">★</span>`;
    }
    return stars;
}

// Apply filters
function applyFilters() {
    const statusFilter = document.getElementById('statusFilter');
    const sortBy = document.getElementById('sortBy');
    const sortDir = document.getElementById('sortDir');
    
    currentFilters.status = statusFilter ? statusFilter.value : '';
    currentFilters.sortBy = sortBy ? sortBy.value : 'submittedAt';
    currentFilters.sortDir = sortDir ? sortDir.value : 'desc';
    
    currentPage = 0; // Reset to first page
    loadFeedback();
}

// Handle search
function handleSearch() {
    const searchInput = document.getElementById('searchInput');
    currentFilters.search = searchInput ? searchInput.value.trim() : '';
    currentPage = 0; // Reset to first page
    loadFeedback();
}

// Refresh feedback
function refreshFeedback() {
    loadFeedback();
    showAlert('Feedback list refreshed', 'info');
}

// View feedback details
async function viewFeedback(id) {
    try {
        const response = await fetch(`/admin/api/feedback/${id}`);
        const result = await response.json();
        
        if (result.success) {
            showFeedbackModal(result.data);
        } else {
            showAlert(result.message || 'Failed to load feedback details', 'error');
        }
        
    } catch (error) {
        console.error('Error loading feedback details:', error);
        showAlert('Failed to load feedback details', 'error');
    }
}

// Show feedback modal
function showFeedbackModal(feedback) {
    const modal = document.getElementById('feedbackModal');
    const modalBody = document.getElementById('feedbackModalBody');
    
    if (!modal || !modalBody) return;
    
    modalBody.innerHTML = `
        <div class="feedback-detail">
            <div class="detail-row">
                <div class="detail-label">ID:</div>
                <div class="detail-value">${feedback.id}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Name:</div>
                <div class="detail-value">${escapeHtml(feedback.name)}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Email:</div>
                <div class="detail-value">${escapeHtml(feedback.email)}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Contact Number:</div>
                <div class="detail-value">${escapeHtml(feedback.contactNumber)}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Rating:</div>
                <div class="detail-value">
                    <div class="rating-display" data-rating="${feedback.rating}">
                        ${generateStars(feedback.rating)}
                    </div>
                </div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Status:</div>
                <div class="detail-value">
                    <span class="status-badge status-${feedback.status.toLowerCase()}">
                        ${feedback.status}
                    </span>
                </div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Submitted:</div>
                <div class="detail-value">${formatDate(feedback.submittedAt)}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Message:</div>
                <div class="detail-value">
                    <div class="message-content">${escapeHtml(feedback.message)}</div>
                </div>
            </div>
            ${feedback.adminResponse ? `
            <div class="detail-row">
                <div class="detail-label">Admin Response:</div>
                <div class="detail-value">
                    <div class="message-content">${escapeHtml(feedback.adminResponse)}</div>
                </div>
            </div>
            ` : ''}
            ${feedback.reviewedByName ? `
            <div class="detail-row">
                <div class="detail-label">Reviewed By:</div>
                <div class="detail-value">${escapeHtml(feedback.reviewedByName)}</div>
            </div>
            ` : ''}
            ${feedback.reviewedAt ? `
            <div class="detail-row">
                <div class="detail-label">Reviewed At:</div>
                <div class="detail-value">${formatDate(feedback.reviewedAt)}</div>
            </div>
            ` : ''}
        </div>
    `;
    
    modal.classList.add('show');
    modal.style.display = 'flex';
}

// Close modal
function closeModal() {
    const modal = document.getElementById('feedbackModal');
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            modal.style.display = 'none';
        }, 300);
    }
}

// Update feedback status
function updateFeedbackStatus(id) {
    const modal = document.getElementById('statusModal');
    const feedbackIdInput = document.getElementById('feedbackId');
    
    if (modal && feedbackIdInput) {
        feedbackIdInput.value = id;
        modal.classList.add('show');
        modal.style.display = 'flex';
    }
}

// Close status modal
function closeStatusModal() {
    const modal = document.getElementById('statusModal');
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            modal.style.display = 'none';
        }, 300);
    }
}

// Handle status update form submission
async function handleStatusUpdate(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const feedbackId = formData.get('feedbackId');
    const status = formData.get('status');
    const adminResponse = formData.get('adminResponse');
    
    try {
        const params = new URLSearchParams({
            status: status,
            adminResponse: adminResponse || ''
        });
        
        const response = await fetch(`/admin/api/feedback/${feedbackId}/status`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: params
        });
        
        const result = await response.json();
        
        if (result.success) {
            showAlert('Feedback status updated successfully', 'success');
            closeStatusModal();
            loadFeedback(); // Refresh the list
        } else {
            showAlert(result.message || 'Failed to update feedback status', 'error');
        }
        
    } catch (error) {
        console.error('Error updating feedback status:', error);
        showAlert('Failed to update feedback status', 'error');
    }
}

// Change page
function changePage(direction) {
    const newPage = currentPage + direction;
    if (newPage >= 0) {
        currentPage = newPage;
        loadFeedback();
    }
}

// Update pagination
function updatePagination() {
    // This would typically come from the API response
    // For now, we'll just update the pagination info
    const paginationInfo = document.getElementById('paginationInfo');
    if (paginationInfo) {
        paginationInfo.textContent = `Showing ${(currentPage * currentSize) + 1}-${(currentPage + 1) * currentSize}`;
    }
    
    // Update prev/next buttons
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    
    if (prevBtn) {
        prevBtn.disabled = currentPage === 0;
    }
    
    // Note: We don't know the total count here, so we can't disable the next button
    // This would typically be handled by the API response
}

// Show alert message
function showAlert(message, type = 'info') {
    const alertContainer = document.getElementById('alertContainer');
    if (!alertContainer) return;
    
    const alertClass = type === 'error' ? 'alert-error' : 
                     type === 'success' ? 'alert-success' : 'alert-info';
    
    const alertElement = document.createElement('div');
    alertElement.className = `alert ${alertClass}`;
    alertElement.innerHTML = `
        ${escapeHtml(message)}
        <button type="button" class="alert-close" onclick="this.parentElement.remove()">&times;</button>
    `;
    
    alertContainer.appendChild(alertElement);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (alertElement.parentElement) {
            alertElement.remove();
        }
    }, 5000);
}

// Show loading state
function showLoading() {
    const tbody = document.getElementById('feedbackTableBody');
    if (tbody) {
        tbody.innerHTML = '<tr><td colspan="7" class="no-data">Loading...</td></tr>';
    }
}

// Hide loading state
function hideLoading() {
    // Loading state is handled by updateFeedbackTable
}

// Utility functions
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-GB', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (error) {
        return dateString;
    }
}

// Close modals when clicking outside
document.addEventListener('click', function(event) {
    const feedbackModal = document.getElementById('feedbackModal');
    const statusModal = document.getElementById('statusModal');
    
    if (event.target === feedbackModal) {
        closeModal();
    }
    
    if (event.target === statusModal) {
        closeStatusModal();
    }
});

// Close modals with Escape key
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeModal();
        closeStatusModal();
    }
});
