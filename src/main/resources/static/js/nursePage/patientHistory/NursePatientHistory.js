// announcement

const cancelAnnouncement = document.querySelector(".announcement__cancel")
const announcement = document.querySelector(".announcement")

if (cancelAnnouncement && announcement) {
    cancelAnnouncement.addEventListener("click" , function(){
        announcement.style.display = "none"
    })
}

// menu bar

const menuBar = document.querySelector(".navbar__section4")
const cancelMenubar = document.querySelector(".responsive__navbar__section1 p")
const responsiveNavbar = document.querySelector(".responsive__navbar")
const navbarLink = document.querySelectorAll(".responsive__navbar__section1 a")

if (menuBar && responsiveNavbar) {
    menuBar.addEventListener("click" , function(){
        responsiveNavbar.style.right = "0%"
    })
}

if (cancelMenubar && responsiveNavbar) {
    cancelMenubar.addEventListener("click" , function(){
        responsiveNavbar.style.right = "-80%"
    })
}

if (navbarLink && navbarLink.length > 0 && responsiveNavbar) {
    navbarLink.forEach(function(link){
        link.addEventListener("click" , function(){
            responsiveNavbar.style.right = "-80%"
        })
    })
}

//profile 

const profile = document.querySelector(".navbar__section3")
const cancelProfile = document.querySelector(".profile__close")
const profileBox = document.querySelector(".profile")

if (profile && profileBox) {
    profile.addEventListener("click" , function(){
        profileBox.style.right = "1%"
    })
}

if (cancelProfile && profileBox) {
    cancelProfile.addEventListener("click" , function(){
        profileBox.style.right = "-20%"
    })
}

// dosage level popup

const dosageEditButton = document.querySelector(".vaccination__table__sub img")   
const dosageCancelButton = document.querySelector(".dosage__level img")
const dosageLevelPopup = document.querySelector(".dosage__level")

if (dosageEditButton && dosageLevelPopup) {
    dosageEditButton.addEventListener("click", function(){
        dosageLevelPopup.style.display = "block"
    })
}

if (dosageCancelButton && dosageLevelPopup) {
    dosageCancelButton.addEventListener("click", function(){
        dosageLevelPopup.style.display = "none"
    })
}

// Patient Search Functionality
const patientSearchInput = document.getElementById('patientSearchInput');
const searchPatientBtn = document.getElementById('searchPatientBtn');
const searchError = document.getElementById('searchError');
const patientInfoSection = document.getElementById('patientInfoSection');
const vaccinationHistorySection = document.getElementById('vaccinationHistorySection');

// Debug: Check if elements are found
console.log('Nurse patient search elements found:');
console.log('patientSearchInput:', patientSearchInput);
console.log('searchPatientBtn:', searchPatientBtn);
console.log('searchError:', searchError);
console.log('patientInfoSection:', patientInfoSection);
console.log('vaccinationHistorySection:', vaccinationHistorySection);

// Add event listeners
if (patientSearchInput) {
    patientSearchInput.addEventListener('input', function(e) {
        // Only allow numbers
        e.target.value = e.target.value.replace(/[^0-9]/g, '');
        
        // Clear error message when typing
        hideError();
    });

    patientSearchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const patientNumber = e.target.value.trim();
            if (patientNumber) {
                searchPatient(patientNumber);
            }
        }
    });
}

if (searchPatientBtn) {
    searchPatientBtn.addEventListener('click', function() {
        const patientNumber = patientSearchInput ? patientSearchInput.value.trim() : '';
        if (patientNumber) {
            searchPatient(patientNumber);
        } else {
            showError('Please enter a patient number');
        }
    });
}

function searchPatient(patientNumber) {
    console.log('Nurse searching for patient number:', patientNumber);
    
    // Show loading state
    if (searchPatientBtn) {
        searchPatientBtn.disabled = true;
        searchPatientBtn.innerHTML = '<img src="../../Images/search.png" alt="search" style="opacity: 0.5;">';
    }
    
    // Hide previous results
    if (patientInfoSection) {
        patientInfoSection.style.display = 'none';
    }
    if (vaccinationHistorySection) {
        vaccinationHistorySection.style.display = 'none';
    }
    hideError();
    
    // Make API call
    fetch(`/api/nurse/patient-search/${patientNumber}`)
        .then(response => {
            console.log('Nurse API Response status:', response.status);
            console.log('Nurse API Response headers:', response.headers);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Nurse API Response data:', data);
            console.log('Data success value:', data.success);
            console.log('Data success type:', typeof data.success);
            
            if (data.success === true) {
                console.log('✅ Success response received, displaying patient info');
                displayPatientInfo(data);
            } else {
                console.log('❌ Error response received:', data.error);
                showError(data.error || 'Patient not found');
            }
        })
        .catch(error => {
            console.error('❌ Fetch error:', error);
            console.error('Error details:', error.message);
            showError('Error searching for patient. Please try again.');
        })
        .finally(() => {
            // Reset button state
            if (searchPatientBtn) {
                searchPatientBtn.disabled = false;
                searchPatientBtn.innerHTML = '<img src="../../Images/search.png" alt="search">';
            }
        });
}

function displayPatientInfo(data) {
    console.log('🎯 Nurse displayPatientInfo called with data:', data);
    console.log('Data type:', typeof data);
    console.log('Data keys:', Object.keys(data));
    
    const patient = data.patient;
    console.log('Patient data:', patient);
    console.log('Patient type:', typeof patient);
    console.log('Patient keys:', patient ? Object.keys(patient) : 'null');
    
    // Update patient information
    const patientIdElement = document.getElementById('patientId');
    const patientNicElement = document.getElementById('patientNic');
    const patientNameElement = document.getElementById('patientName');
    const patientEmailElement = document.getElementById('patientEmail');
    const patientPhoneElement = document.getElementById('patientPhone');
    
    if (patientIdElement) patientIdElement.textContent = patient.id || 'N/A';
    if (patientNicElement) patientNicElement.textContent = patient.nic || 'N/A';
    if (patientNameElement) patientNameElement.textContent = patient.name || 'N/A';
    if (patientEmailElement) patientEmailElement.textContent = patient.email || 'N/A';
    if (patientPhoneElement) patientPhoneElement.textContent = patient.contact || 'N/A';
    
    // Show patient info section
    if (patientInfoSection) {
        console.log('Showing patient info section');
        patientInfoSection.style.display = 'block';
    } else {
        console.error('patientInfoSection element not found');
    }
    
    // Update vaccination history
    displayVaccinationHistory(data.completedVaccinations, data.pendingVaccinations);
    
    // Show vaccination history section
    if (vaccinationHistorySection) {
        console.log('Showing vaccination history section');
        vaccinationHistorySection.style.display = 'block';
    } else {
        console.error('vaccinationHistorySection element not found');
    }
}

function displayVaccinationHistory(completedVaccinations, pendingVaccinations) {
    console.log('Nurse displayVaccinationHistory called with:', { completedVaccinations, pendingVaccinations });
    
    // Clear existing rows (except headers)
    const completedTable = document.getElementById('completedVaccinationsTable');
    const pendingTable = document.getElementById('pendingVaccinationsTable');
    
    console.log('Tables found:', { completedTable, pendingTable });
    
    // Remove existing data rows
    const completedRows = completedTable ? completedTable.querySelectorAll('tr:not(#vaccination__summary__table__header)') : [];
    const pendingRows = pendingTable ? pendingTable.querySelectorAll('tr:not(#pending__vaccination__table__header)') : [];
    
    completedRows.forEach(row => row.remove());
    pendingRows.forEach(row => row.remove());
    
    // Show/hide "no data" messages
    const noCompleted = document.getElementById('noCompletedVaccinations');
    const noPending = document.getElementById('noPendingVaccinations');
    
    console.log('No data elements found:', { noCompleted, noPending });
    
    if (completedVaccinations && completedVaccinations.length === 0) {
        if (noCompleted) {
            noCompleted.style.display = 'table-row';
        }
    } else {
        if (noCompleted) {
            noCompleted.style.display = 'none';
        }
        // Add completed vaccination rows
        if (completedVaccinations && completedVaccinations.length > 0) {
            completedVaccinations.forEach(history => {
                const row = createCompletedVaccinationRow(history);
                if (completedTable) {
                    completedTable.appendChild(row);
                }
            });
        }
    }
    
    if (pendingVaccinations && pendingVaccinations.length === 0) {
        if (noPending) {
            noPending.style.display = 'table-row';
        }
    } else {
        if (noPending) {
            noPending.style.display = 'none';
        }
        // Add pending vaccination rows
        if (pendingVaccinations && pendingVaccinations.length > 0) {
            pendingVaccinations.forEach(history => {
                const row = createPendingVaccinationRow(history);
                if (pendingTable) {
                    pendingTable.appendChild(row);
                }
            });
        }
    }
}

function createCompletedVaccinationRow(history) {
    const row = document.createElement('tr');
    row.innerHTML = `
        <td>${history.vaccineName || 'N/A'}</td>
        <td>${formatDate(history.vaccinationDate)}</td>
        <td>${history.location || 'N/A'}</td>
        <td>${history.status || 'Completed'}</td>
    `;
    return row;
}

function createPendingVaccinationRow(history) {
    const row = document.createElement('tr');
    row.innerHTML = `
        <td>${history.vaccineName || 'N/A'}</td>
        <td>${formatDate(history.vaccinationDate)}</td>
        <td>${history.timeSlot || 'N/A'}</td>
        <td>${history.location || 'N/A'}</td>
        <td class="vaccination__table__sub">
            <p>${history.dosageLevel || '0ml'}</p>
        </td>
        <td>
            <div style="max-width: 200px; word-wrap: break-word; font-size: 12px;">
                ${history.additionalNotes || 'No notes'}
            </div>
        </td>
        <td>
            <img src="../../Images/correct.png" alt="confirm" onclick="confirmVaccination(${history.id})" style="cursor: pointer; margin-right: 5px;" title="Confirm Vaccination">
            <img src="../../Images/wrong.png" alt="cancel" onclick="cancelVaccination(${history.id})" style="cursor: pointer;" title="Cancel Vaccination">
        </td>
    `;
    return row;
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        // Handle different date formats
        let date;
        if (dateString.includes('-')) {
            // Handle YYYY-MM-DD format
            const parts = dateString.split('-');
            if (parts.length === 3) {
                date = new Date(parts[0], parts[1] - 1, parts[2]);
            } else {
                date = new Date(dateString);
            }
        } else {
            date = new Date(dateString);
        }
        
        // Check if date is valid
        if (isNaN(date.getTime())) {
            return dateString; // Return original if invalid
        }
        
        return date.toLocaleDateString('en-GB'); // DD/MM/YYYY format
    } catch (error) {
        console.error('Date formatting error:', error, 'for date:', dateString);
        return dateString;
    }
}


function confirmVaccination(historyId) {
    console.log('Confirming vaccination for history ID:', historyId);
    
    if (!confirm('Are you sure you want to confirm this vaccination? This will send a confirmation email to the patient.')) {
        return;
    }
    
    // Show loading state
    const confirmBtn = event.target;
    const originalSrc = confirmBtn.src;
    confirmBtn.src = '../../Images/loading.gif'; // You might want to add a loading image
    confirmBtn.style.pointerEvents = 'none';
    
    fetch(`/api/nurse/confirm-vaccination/${historyId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('Vaccination confirmed successfully! Patient has been notified via email.');
            // Refresh the patient data to show updated status
            const patientNumber = document.getElementById('patientSearchInput').value.trim();
            if (patientNumber) {
                searchPatient(patientNumber);
            }
        } else {
            alert('Error confirming vaccination: ' + (data.error || 'Unknown error'));
        }
    })
    .catch(error => {
        console.error('Error confirming vaccination:', error);
        alert('Error confirming vaccination. Please try again.');
    })
    .finally(() => {
        // Reset button state
        confirmBtn.src = originalSrc;
        confirmBtn.style.pointerEvents = 'auto';
    });
}

function cancelVaccination(historyId) {
    console.log('Cancelling vaccination for history ID:', historyId);
    
    if (!confirm('Are you sure you want to cancel this vaccination? This will send a cancellation email to the patient.')) {
        return;
    }
    
    // Show loading state
    const cancelBtn = event.target;
    const originalSrc = cancelBtn.src;
    cancelBtn.src = '../../Images/loading.gif'; // You might want to add a loading image
    cancelBtn.style.pointerEvents = 'none';
    
    fetch(`/api/nurse/cancel-vaccination/${historyId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('Vaccination cancelled successfully! Patient has been notified via email.');
            // Refresh the patient data to show updated status
            const patientNumber = document.getElementById('patientSearchInput').value.trim();
            if (patientNumber) {
                searchPatient(patientNumber);
            }
        } else {
            alert('Error cancelling vaccination: ' + (data.error || 'Unknown error'));
        }
    })
    .catch(error => {
        console.error('Error cancelling vaccination:', error);
        alert('Error cancelling vaccination. Please try again.');
    })
    .finally(() => {
        // Reset button state
        cancelBtn.src = originalSrc;
        cancelBtn.style.pointerEvents = 'auto';
    });
}

function showError(message) {
    if (searchError) {
        searchError.textContent = message;
        searchError.style.display = 'block';
    }
}

function hideError() {
    if (searchError) {
        searchError.style.display = 'none';
    }
}

// Test function to verify API connectivity
function testAPI() {
    console.log('Testing nurse API connectivity...');
    fetch('/api/nurse/test-search')
        .then(response => response.json())
        .then(data => {
            console.log('Nurse API Test Response:', data);
            if (data.success) {
                console.log('✅ Nurse API is working correctly');
            } else {
                console.log('❌ Nurse API test failed');
            }
        })
        .catch(error => {
            console.error('❌ Nurse API test error:', error);
        });
}

// Load recent updates when page loads
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ Nurse JavaScript loaded successfully!');
    console.log('Nurse page loaded, running API test...');
    testAPI();
    loadRecentUpdates();
});

// Load recent updates from the server
function loadRecentUpdates() {
    console.log('Loading recent updates...');
    
    fetch('/api/nurse/recent-updates')
        .then(response => response.json())
        .then(data => {
            console.log('Recent updates response:', data);
            
            if (data.success && data.recentUpdates) {
                displayRecentUpdates(data.recentUpdates);
            } else {
                console.error('Failed to load recent updates:', data.error);
                showNoUpdates();
            }
        })
        .catch(error => {
            console.error('Error loading recent updates:', error);
            showNoUpdates();
        });
}

// Display recent updates in the UI
function displayRecentUpdates(updates) {
    const container = document.getElementById('recentUpdatesContainer');
    const loadingElement = document.getElementById('loadingUpdates');
    const noUpdatesElement = document.getElementById('noUpdates');
    
    // Hide loading and no updates messages
    if (loadingElement) loadingElement.style.display = 'none';
    if (noUpdatesElement) noUpdatesElement.style.display = 'none';
    
    if (!updates || updates.length === 0) {
        showNoUpdates();
        return;
    }
    
    // Clear existing content
    container.innerHTML = '';
    
    // Create update elements
    updates.forEach(update => {
        const updateElement = document.createElement('div');
        updateElement.className = 'patient__section2__sub__sub';
        updateElement.style.cursor = 'pointer';
        
        // Extract patient number from patient ID (e.g., "Vak P 1001" -> "1001")
        const patientNumber = update.patientId ? update.patientId.split(' ').pop() : 'Unknown';
        
        updateElement.innerHTML = `
            <p>${patientNumber}</p>
            <p>${update.timeAgo || 'Unknown'}</p>
        `;
        
        // Add click handler to search for this patient
        updateElement.addEventListener('click', function() {
            const searchInput = document.getElementById('patientSearchInput');
            if (searchInput) {
                searchInput.value = patientNumber;
                searchPatient(patientNumber);
            }
        });
        
        container.appendChild(updateElement);
        
        // Add line break after each update (except the last one)
        if (updates.indexOf(update) < updates.length - 1) {
            const br = document.createElement('br');
            container.appendChild(br);
        }
    });
    
    console.log(`Displayed ${updates.length} recent updates`);
}

// Show no updates message
function showNoUpdates() {
    const loadingElement = document.getElementById('loadingUpdates');
    const noUpdatesElement = document.getElementById('noUpdates');
    
    if (loadingElement) loadingElement.style.display = 'none';
    if (noUpdatesElement) noUpdatesElement.style.display = 'block';
}

// Simple test to verify JavaScript is working
console.log('🚀 NursePatientHistory.js loaded successfully!');