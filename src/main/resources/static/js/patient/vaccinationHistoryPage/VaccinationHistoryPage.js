// Vaccination History Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    console.log('Vaccination History Page loaded');
    
    // Load vaccination history when page loads
    loadVaccinationHistory();
    
    // Add event listeners for UI interactions
    setupEventListeners();
});

/**
 * Setup event listeners for UI interactions
 */
function setupEventListeners() {
    // Profile dropdown functionality
    const profileImg = document.querySelector('.navbar__section3 img');
    const profileSection = document.querySelector('.profile');
    const profileClose = document.querySelector('.profile__close');
    
    if (profileImg && profileSection) {
        profileImg.addEventListener('click', function() {
            profileSection.style.display = 'block';
        });
    }
    
    if (profileClose && profileSection) {
        profileClose.addEventListener('click', function() {
            profileSection.style.display = 'none';
        });
    }
    
    // Responsive navbar functionality
    const menuBar = document.querySelector('.navbar__section4 img');
    const responsiveNavbar = document.querySelector('.responsive__navbar');
    const cancelMenubar = document.querySelector('.responsive__navbar__section1 p');
    const navbarLinks = document.querySelectorAll('.responsive__navbar__section1 a');
    
    if (menuBar && responsiveNavbar) {
        menuBar.addEventListener('click', function() {
            responsiveNavbar.style.right = '0%';
        });
    }
    
    if (cancelMenubar && responsiveNavbar) {
        cancelMenubar.addEventListener('click', function() {
            responsiveNavbar.style.right = '-80%';
        });
    }
    
    navbarLinks.forEach(function(link) {
        link.addEventListener('click', function() {
            responsiveNavbar.style.right = '-80%';
        });
    });
    
    // Announcement close functionality
    const announcementCancel = document.querySelector('.announcement__cancel');
    const announcement = document.querySelector('.announcement');
    
    if (announcementCancel && announcement) {
        announcementCancel.addEventListener('click', function() {
            announcement.style.display = 'none';
        });
    }
}

/**
 * Load vaccination history from the server
 */
async function loadVaccinationHistory() {
    try {
        console.log('Loading vaccination history...');
        
        // Show loading indicator
        showLoadingIndicator();
        
        // Fetch vaccination history from API
        const response = await fetch('/patient/api/vaccination-history', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('Vaccination history response:', data);
        
        if (data.success) {
            displayVaccinationHistory(data.vaccinationHistory, data.patientId);
        } else {
            showErrorMessage(data.error || 'Failed to load vaccination history');
        }
        
    } catch (error) {
        console.error('Error loading vaccination history:', error);
        showErrorMessage('Unable to load vaccination history. Please check your connection and try again.');
    }
}

/**
 * Display vaccination history in the table
 */
function displayVaccinationHistory(vaccinationHistory, patientId) {
    const table = document.getElementById('vaccinationHistoryTable');
    const loadingIndicator = document.getElementById('loadingIndicator');
    const errorMessage = document.getElementById('errorMessage');
    const noRecordsMessage = document.getElementById('noRecordsMessage');
    
    // Store patient ID globally for delete operations
    window.currentPatientId = patientId;
    
    // Hide loading indicator
    hideLoadingIndicator();
    
    // Clear any existing error messages
    hideErrorMessage();
    
    if (!vaccinationHistory || vaccinationHistory.length === 0) {
        // Show no records message
        showNoRecordsMessage();
        return;
    }
    
    // Clear existing table rows (except header)
    const headerRow = table.querySelector('#vaccination__history__table__header');
    table.innerHTML = '';
    table.appendChild(headerRow);
    
    // Add vaccination history rows
    vaccinationHistory.forEach(function(history) {
        const row = createVaccinationHistoryRow(history);
        table.appendChild(row);
    });
    
    // Show the table
    table.style.display = 'table';
    
    console.log(`Displayed ${vaccinationHistory.length} vaccination records`);
}

/**
 * Create a table row for a vaccination history record
 */
function createVaccinationHistoryRow(history) {
    const row = document.createElement('tr');
    
    // Format the vaccination date
    const vaccinationDate = new Date(history.vaccinationDate);
    const formattedDate = vaccinationDate.toLocaleDateString('en-GB', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
    
    // Create row HTML
    row.innerHTML = `
        <td>${escapeHtml(history.vaccineName || 'N/A')}</td>
        <td>${formattedDate}</td>
        <td>${escapeHtml(history.location || 'N/A')}</td>
        <td>${escapeHtml(history.status || 'Completed')}</td>
        <td class="vaccination__history__table__row__download">
            <img src="/images/download.png" 
                 alt="Download Certificate" 
                 title="Download Vaccination Certificate"
                 onclick="downloadVaccinationCertificate(${history.id})"
                 style="cursor: pointer;">
        </td>
        <td class="vaccination__history__table__row__action">
            <button class="delete-btn" 
                    onclick="deleteVaccinationRecord(${history.id}, '${escapeHtml(history.vaccineName || 'this vaccine')}')"
                    title="Delete Vaccination Record"
                    style="background-color: #dc3545; color: white; border: none; padding: 5px 10px; border-radius: 3px; cursor: pointer; font-size: 12px;">
                Delete
            </button>
        </td>
    `;
    
    return row;
}

/**
 * Download vaccination certificate
 */
async function downloadVaccinationCertificate(historyId) {
    try {
        console.log(`Downloading certificate for history ID: ${historyId}`);
        
        // Show loading state on the download button
        const downloadImg = event.target;
        const originalSrc = downloadImg.src;
        downloadImg.src = '/images/loading.gif'; // You might want to add a loading icon
        downloadImg.style.opacity = '0.6';
        
        // Make the download request
        const response = await fetch(`/patient/download-certificate/${historyId}`, {
            method: 'GET',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        });
        
        if (!response.ok) {
            if (response.status === 403) {
                throw new Error('Access denied. This vaccination certificate is not available for download.');
            } else if (response.status === 404) {
                throw new Error('Vaccination certificate not found.');
            } else {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
        }
        
        // Get the filename from the response headers
        const contentDisposition = response.headers.get('Content-Disposition');
        let filename = 'vaccination_certificate.pdf';
        if (contentDisposition) {
            const filenameMatch = contentDisposition.match(/filename="(.+)"/);
            if (filenameMatch) {
                filename = filenameMatch[1];
            }
        }
        
        // Convert response to blob
        const blob = await response.blob();
        
        // Create download link
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        
        // Cleanup
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        
        console.log(`Certificate downloaded successfully: ${filename}`);
        
        // Show success message
        showSuccessMessage('Vaccination certificate downloaded successfully!');
        
    } catch (error) {
        console.error('Error downloading certificate:', error);
        showErrorMessage(error.message || 'Failed to download vaccination certificate. Please try again.');
    } finally {
        // Restore the download button
        const downloadImg = event.target;
        downloadImg.src = originalSrc;
        downloadImg.style.opacity = '1';
    }
}

/**
 * Show loading indicator
 */
function showLoadingIndicator() {
    const loadingIndicator = document.getElementById('loadingIndicator');
    const table = document.getElementById('vaccinationHistoryTable');
    const errorMessage = document.getElementById('errorMessage');
    const noRecordsMessage = document.getElementById('noRecordsMessage');
    
    loadingIndicator.style.display = 'block';
    table.style.display = 'none';
    errorMessage.style.display = 'none';
    noRecordsMessage.style.display = 'none';
}

/**
 * Hide loading indicator
 */
function hideLoadingIndicator() {
    const loadingIndicator = document.getElementById('loadingIndicator');
    loadingIndicator.style.display = 'none';
}

/**
 * Show error message
 */
function showErrorMessage(message) {
    const errorMessage = document.getElementById('errorMessage');
    const loadingIndicator = document.getElementById('loadingIndicator');
    const table = document.getElementById('vaccinationHistoryTable');
    const noRecordsMessage = document.getElementById('noRecordsMessage');
    
    errorMessage.querySelector('p').textContent = message;
    errorMessage.style.display = 'block';
    loadingIndicator.style.display = 'none';
    table.style.display = 'none';
    noRecordsMessage.style.display = 'none';
}

/**
 * Hide error message
 */
function hideErrorMessage() {
    const errorMessage = document.getElementById('errorMessage');
    errorMessage.style.display = 'none';
}

/**
 * Show no records message
 */
function showNoRecordsMessage() {
    const noRecordsMessage = document.getElementById('noRecordsMessage');
    const loadingIndicator = document.getElementById('loadingIndicator');
    const table = document.getElementById('vaccinationHistoryTable');
    const errorMessage = document.getElementById('errorMessage');
    
    noRecordsMessage.style.display = 'block';
    loadingIndicator.style.display = 'none';
    table.style.display = 'none';
    errorMessage.style.display = 'none';
}

/**
 * Show success message
 */
function showSuccessMessage(message) {
    // Create a temporary success message
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';
    successDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background-color: #d4edda;
        color: #155724;
        border: 1px solid #c3e6cb;
        border-radius: 5px;
        padding: 15px;
        z-index: 1000;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    `;
    successDiv.textContent = message;
    
    document.body.appendChild(successDiv);
    
    // Remove the message after 3 seconds
    setTimeout(() => {
        if (successDiv.parentNode) {
            successDiv.parentNode.removeChild(successDiv);
        }
    }, 3000);
}

/**
 * Escape HTML to prevent XSS
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Refresh vaccination history
 */
function refreshVaccinationHistory() {
    console.log('Refreshing vaccination history...');
    loadVaccinationHistory();
}

/**
 * Delete vaccination record
 */
async function deleteVaccinationRecord(historyId, vaccineName) {
    try {
        // Show confirmation dialog
        const confirmed = confirm(`Are you sure you want to delete the vaccination record for "${vaccineName}"?\n\nThis action cannot be undone.`);
        
        if (!confirmed) {
            return;
        }
        
        console.log(`Deleting vaccination record with ID: ${historyId}`);
        
        // Get patient ID from the current user context (you might need to adjust this based on your authentication setup)
        const patientId = getCurrentPatientId(); // This function needs to be implemented based on your auth setup
        
        if (!patientId) {
            showErrorMessage('Unable to identify patient. Please refresh the page and try again.');
            return;
        }
        
        // Show loading state on the delete button
        const deleteBtn = event.target;
        const originalText = deleteBtn.textContent;
        deleteBtn.textContent = 'Deleting...';
        deleteBtn.disabled = true;
        deleteBtn.style.opacity = '0.6';
        
        // Make the delete request
        const response = await fetch(`/patient/api/vaccination-history/${historyId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('Delete response:', data);
        
        if (data.success) {
            // Show success message
            showSuccessMessage('Vaccination record deleted successfully!');
            
            // Refresh the vaccination history table
            await loadVaccinationHistory();
        } else {
            showErrorMessage(data.message || 'Failed to delete vaccination record. Please try again.');
        }
        
    } catch (error) {
        console.error('Error deleting vaccination record:', error);
        showErrorMessage('Failed to delete vaccination record. Please check your connection and try again.');
    } finally {
        // Restore the delete button
        const deleteBtn = event.target;
        deleteBtn.textContent = 'Delete';
        deleteBtn.disabled = false;
        deleteBtn.style.opacity = '1';
    }
}

/**
 * Get current patient ID from authentication context
 * This function needs to be implemented based on your authentication setup
 */
function getCurrentPatientId() {
    // Option 1: If patient ID is available in a global variable
    if (typeof window.currentPatientId !== 'undefined') {
        return window.currentPatientId;
    }
    
    // Option 2: If patient ID is stored in localStorage
    const storedPatientId = localStorage.getItem('patientId');
    if (storedPatientId) {
        return storedPatientId;
    }
    
    // Option 3: If patient ID is in a meta tag
    const patientIdMeta = document.querySelector('meta[name="patient-id"]');
    if (patientIdMeta) {
        return patientIdMeta.getAttribute('content');
    }
    
    // Option 4: Extract from URL or other sources
    // You might need to adjust this based on your application's routing
    
    console.warn('Patient ID not found. Please ensure patient authentication is properly set up.');
    return null;
}

// Make functions available globally for onclick handlers
window.downloadVaccinationCertificate = downloadVaccinationCertificate;
window.refreshVaccinationHistory = refreshVaccinationHistory;
window.deleteVaccinationRecord = deleteVaccinationRecord;