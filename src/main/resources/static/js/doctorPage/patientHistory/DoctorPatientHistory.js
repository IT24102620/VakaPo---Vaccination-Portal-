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
console.log('Patient search elements found:');
console.log('patientSearchInput:', patientSearchInput);
console.log('searchPatientBtn:', searchPatientBtn);
console.log('searchError:', searchError);
console.log('patientInfoSection:', patientInfoSection);
console.log('vaccinationHistorySection:', vaccinationHistorySection);

// Only allow numbers in the search input
if (patientSearchInput) {
    patientSearchInput.addEventListener('input', function(e) {
        // Remove any non-numeric characters
        this.value = this.value.replace(/[^0-9]/g, '');
        
        // Clear error message when user starts typing
        if (searchError) {
            searchError.style.display = 'none';
        }
    });
}

// Search patient when button is clicked
if (searchPatientBtn) {
    searchPatientBtn.addEventListener('click', function() {
        const patientNumber = patientSearchInput ? patientSearchInput.value.trim() : '';
        
        if (!patientNumber) {
            showError('Please enter a patient number');
            return;
        }
        
        if (patientNumber.length < 3) {
            showError('Please enter at least 3 digits');
            return;
        }
        
        searchPatient(patientNumber);
    });
}

// Search patient when Enter key is pressed
if (patientSearchInput) {
    patientSearchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            if (searchPatientBtn) {
                searchPatientBtn.click();
            }
        }
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

function searchPatient(patientNumber) {
    console.log('Searching for patient number:', patientNumber);
    
    // Show loading state
    searchPatientBtn.disabled = true;
    searchPatientBtn.innerHTML = '<img src="../../Images/search.png" alt="search" style="opacity: 0.5;">';
    
    // Hide previous results
    if (patientInfoSection) {
        patientInfoSection.style.display = 'none';
    }
    if (vaccinationHistorySection) {
        vaccinationHistorySection.style.display = 'none';
    }
    hideError();
    
    // Make API call
    fetch(`/api/doctor/patient-search/${patientNumber}`)
        .then(response => {
            console.log('API Response status:', response.status);
            console.log('API Response headers:', response.headers);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('API Response data:', data);
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
            searchPatientBtn.disabled = false;
            searchPatientBtn.innerHTML = '<img src="../../Images/search.png" alt="search">';
        });
}

function displayPatientInfo(data) {
    console.log('🎯 displayPatientInfo called with data:', data);
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
    console.log('displayVaccinationHistory called with:', { completedVaccinations, pendingVaccinations });
    
    // Clear existing rows (except headers)
    const completedTable = document.getElementById('completedVaccinationsTable');
    const pendingTable = document.getElementById('pendingVaccinationsTable');
    
    console.log('Tables found:', { completedTable, pendingTable });
    
    // Remove existing data rows
    const completedRows = completedTable.querySelectorAll('tr:not(#vaccination__summary__table__header)');
    const pendingRows = pendingTable.querySelectorAll('tr:not(#pending__vaccination__table__header)');
    
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
            completedVaccinations.forEach(appointment => {
                const row = createCompletedVaccinationRow(appointment);
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
            pendingVaccinations.forEach(appointment => {
                const row = createPendingVaccinationRow(appointment);
                if (pendingTable) {
                    pendingTable.appendChild(row);
                }
            });
        }
    }
}

function createCompletedVaccinationRow(appointment) {
    const row = document.createElement('tr');
    row.innerHTML = `
        <td>${appointment.vaccineName || 'N/A'}</td>
        <td>${formatDate(appointment.vaccinationDate || appointment.appointmentDate)}</td>
        <td>${appointment.timeSlot || 'N/A'}</td>
        <td>${appointment.location || appointment.institutionName || 'N/A'}</td>
        <td>${appointment.dosageLevel || 'N/A'}</td>
        <td>${appointment.additionalNotes || 'N/A'}</td>
        <td class="vaccination__table__sub">
            <span id="nextDate-${appointment.id}">${formatDate(appointment.nextVaccineDate) || 'No next shot needed'}</span>
            <img src="../../Images/edit.png" alt="edit" onclick="editNextVaccineDate(${appointment.id})" style="cursor: pointer; margin-left: 5px;">
        </td>
        <td><span class="status-badge completed">Approved</span></td>
    `;
    return row;
}

function createPendingVaccinationRow(appointment) {
    const row = document.createElement('tr');
    row.innerHTML = `
        <td>${appointment.vaccineName || 'N/A'}</td>
        <td>${formatDate(appointment.vaccinationDate || appointment.appointmentDate)}</td>
        <td>${appointment.timeSlot || 'N/A'}</td>
        <td>${appointment.location || appointment.institutionName || 'N/A'}</td>
        <td class="vaccination__table__sub">
            <span id="dosage-${appointment.id}">${getDosageNumber(appointment.dosageLevel)}</span><span class="dosage-unit">ml</span>
            <img src="../../Images/edit.png" alt="edit" onclick="editDosage(${appointment.id})" style="cursor: pointer; margin-left: 5px;">
        </td>
        <td>
            <span id="notes-${appointment.id}">${appointment.additionalNotes || 'No notes'}</span>
            <img src="../../Images/edit.png" alt="edit" onclick="editNotes(${appointment.id})" style="cursor: pointer; margin-left: 5px;">
        </td>
        <td class="vaccination__table__sub">
            <span id="nextDate-${appointment.id}">${formatDate(appointment.nextVaccineDate) || 'No next shot needed'}</span>
            <img src="../../Images/edit.png" alt="edit" onclick="editNextVaccineDate(${appointment.id})" style="cursor: pointer; margin-left: 5px;">
        </td>
        <td><span class="status-badge pending">${getNurseApprovalStatus(appointment)}</span></td>
    `;
    return row;
}

function getDosageNumber(dosageLevel) {
    if (!dosageLevel) return '0';
    
    // Extract number from dosage level (e.g., "20ml" -> "20", "0ml" -> "0")
    const match = dosageLevel.toString().match(/^(\d+(?:\.\d+)?)/);
    return match ? match[1] : '0';
}

function getNurseApprovalStatus(appointment) {
    // Check if nurse approval is available
    if (appointment.nurseApproval) {
        switch (appointment.nurseApproval.toLowerCase()) {
            case 'approved':
                return 'Approved';
            case 'rejected':
                return 'Rejected';
            case 'pending':
            default:
                return 'Pending';
        }
    }
    
    // Fallback to status if nurse approval not available
    if (appointment.status) {
        switch (appointment.status.toLowerCase()) {
            case 'completed':
                return 'Pending'; // Show as pending until nurse approval
            case 'scheduled':
                return 'Pending';
            default:
                return 'Pending';
        }
    }
    
    return 'Pending';
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

function editDosage(historyId) {
    const currentDosageElement = document.getElementById(`dosage-${historyId}`);
    const currentDosageNumber = currentDosageElement.textContent;
    
    const newDosageNumber = prompt('Enter dosage number (ml will be added automatically):', currentDosageNumber);
    
    if (newDosageNumber !== null && newDosageNumber.trim() !== '') {
        // Validate that it's a valid number
        const number = parseFloat(newDosageNumber.trim());
        if (isNaN(number) || number < 0) {
            alert('Please enter a valid positive number');
            return;
        }
        
        const dosageWithUnit = newDosageNumber.trim() + 'ml';
        
        // Update the display
        currentDosageElement.textContent = newDosageNumber.trim();
        
        // Save to backend with "ml" unit
        saveVaccinationHistory(historyId, { dosageLevel: dosageWithUnit });
    }
}

function editNotes(historyId) {
    const currentNotes = document.getElementById(`notes-${historyId}`).textContent;
    const newNotes = prompt('Enter additional notes:', currentNotes === 'No notes' ? '' : currentNotes);
    
    if (newNotes !== null) {
        const displayNotes = newNotes.trim() === '' ? 'No notes' : newNotes.trim();
        // Update the display
        document.getElementById(`notes-${historyId}`).textContent = displayNotes;
        
        // Save to backend
        saveVaccinationHistory(historyId, { additionalNotes: newNotes.trim() });
    }
}

// Global variable to store current history ID for date picker
let currentHistoryIdForDate = null;

function editNextVaccineDate(historyId) {
    currentHistoryIdForDate = historyId;
    
    // Get current date value
    const currentDate = document.getElementById(`nextDate-${historyId}`).textContent;
    let currentDateValue = '';
    
    // Convert display date back to YYYY-MM-DD format if it's not "No next shot needed"
    if (currentDate !== 'No next shot needed' && currentDate !== 'N/A') {
        // Try to parse the display date back to YYYY-MM-DD format
        try {
            const date = new Date(currentDate.split('/').reverse().join('-'));
            if (!isNaN(date.getTime())) {
                currentDateValue = date.toISOString().split('T')[0];
            }
        } catch (e) {
            console.log('Could not parse current date:', currentDate);
        }
    }
    
    // Set the date input value
    const dateInput = document.getElementById('nextVaccineDateInput');
    dateInput.value = currentDateValue;
    
    // Show the modal
    const modal = document.getElementById('datePickerModal');
    modal.style.display = 'flex';
    
    // Focus on the date input
    setTimeout(() => {
        dateInput.focus();
    }, 100);
}

// Date picker modal functions
function initializeDatePickerModal() {
    const modal = document.getElementById('datePickerModal');
    const closeBtn = document.querySelector('.date-picker-close');
    const cancelBtn = document.getElementById('cancelDateBtn');
    const saveBtn = document.getElementById('saveDateBtn');
    const clearBtn = document.getElementById('clearDateBtn');
    const dateInput = document.getElementById('nextVaccineDateInput');
    
    // Close modal functions
    function closeModal() {
        modal.style.display = 'none';
        currentHistoryIdForDate = null;
    }
    
    // Close modal when clicking X or Cancel
    if (closeBtn) {
        closeBtn.addEventListener('click', closeModal);
    }
    
    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeModal);
    }
    
    // Close modal when clicking outside
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeModal();
            }
        });
    }
    
    // Clear date button
    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            dateInput.value = '';
            saveDateSelection();
        });
    }
    
    // Save date button
    if (saveBtn) {
        saveBtn.addEventListener('click', function() {
            saveDateSelection();
        });
    }
    
    // Save date when Enter is pressed in date input
    if (dateInput) {
        dateInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                saveDateSelection();
            }
        });
    }
    
    function saveDateSelection() {
        if (!currentHistoryIdForDate) {
            closeModal();
            return;
        }
        
        const selectedDate = dateInput.value;
        let displayDate = 'No next shot needed';
        let dateValue = null;
        
        if (selectedDate && selectedDate.trim() !== '') {
            // Validate that the date is not in the past
            const selectedDateObj = new Date(selectedDate);
            const today = new Date();
            today.setHours(0, 0, 0, 0); // Reset time to start of day
            
            if (selectedDateObj < today) {
                alert('Please select a future date for the next vaccination.');
                return;
            }
            
            displayDate = formatDate(selectedDate);
            dateValue = selectedDate;
        }
        
        // Update the display
        document.getElementById(`nextDate-${currentHistoryIdForDate}`).textContent = displayDate;
        
        // Save to backend
        saveVaccinationHistory(currentHistoryIdForDate, { nextVaccineDate: dateValue });
        
        // Close modal
        closeModal();
    }
}

function saveVaccinationHistory(historyId, updateData) {
    console.log('Saving vaccination history:', { historyId, updateData });
    
    fetch(`/api/doctor/vaccination-history/${historyId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(updateData)
    })
    .then(response => {
        console.log('Response status:', response.status);
        console.log('Response headers:', response.headers);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('Response data:', data);
        if (data.success) {
            console.log('Vaccination history updated successfully');
            showMessage('Changes saved successfully', 'success');
            
            // Refresh recent updates
            loadRecentUpdates();
        } else {
            console.error('Failed to update vaccination history:', data.error);
            showMessage('Failed to save changes: ' + (data.error || 'Unknown error'), 'error');
        }
    })
    .catch(error => {
        console.error('Error updating vaccination history:', error);
        showMessage('Error saving changes: ' + error.message, 'error');
    });
}


function loadRecentUpdates() {
    console.log('Loading recent updates...');
    
    fetch('/api/doctor/recent-updates')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Recent updates response:', data);
            if (data.success) {
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
    
    // Create update items
    updates.forEach((update, index) => {
        const updateDiv = document.createElement('div');
        updateDiv.className = 'patient__section2__sub__sub';
        updateDiv.style.cursor = 'pointer';
        updateDiv.onclick = () => searchPatientById(update.patientId);
        
        updateDiv.innerHTML = `
            <p>${update.patientId}</p>
            <p>${update.timeAgo}</p>
        `;
        
        container.appendChild(updateDiv);
        
        // Add separator line (except for last item)
        if (index < updates.length - 1) {
            const separator = document.createElement('br');
            container.appendChild(separator);
        }
    });
}

function showNoUpdates() {
    const loadingElement = document.getElementById('loadingUpdates');
    const noUpdatesElement = document.getElementById('noUpdates');
    
    if (loadingElement) loadingElement.style.display = 'none';
    if (noUpdatesElement) noUpdatesElement.style.display = 'block';
}

function searchPatientById(patientId) {
    // Extract patient number from patient ID (e.g., "Vak P 1234" -> "1234")
    const patientNumber = patientId.replace('Vak P ', '');
    
    // Set the search input and trigger search
    const searchInput = document.getElementById('patientSearchInput');
    if (searchInput) {
        searchInput.value = patientNumber;
        searchPatient(patientNumber);
    }
}

function showMessage(message, type) {
    // Create message element
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = message;
    messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 5px;
        z-index: 1000;
        font-weight: bold;
        background-color: ${type === 'success' ? '#d4edda' : '#f8d7da'};
        color: ${type === 'success' ? '#155724' : '#721c24'};
        border: 1px solid ${type === 'success' ? '#c3e6cb' : '#f5c6cb'};
    `;
    
    // Add to page
    document.body.appendChild(messageDiv);
    
    // Remove after 3 seconds
    setTimeout(() => {
        if (messageDiv.parentNode) {
            messageDiv.parentNode.removeChild(messageDiv);
        }
    }, 3000);
}

// Test function to verify API connectivity
function testAPI() {
    console.log('Testing API connectivity...');
    fetch('/api/doctor/test-search')
        .then(response => response.json())
        .then(data => {
            console.log('API Test Response:', data);
            if (data.success) {
                console.log('✅ API is working correctly');
            } else {
                console.log('❌ API test failed');
            }
        })
        .catch(error => {
            console.error('❌ API test error:', error);
        });
}

// Test function with sample data
function testSearchWithSampleData() {
    console.log('Testing with sample data...');
    
    const sampleData = {
        success: true,
        patient: {
            id: "Vak P 1001",
            name: "John Doe",
            email: "john.doe@example.com",
            contact: "0771234567",
            nic: "123456789V"
        },
        completedVaccinations: [
            {
                id: 1,
                vaccineName: "COVID-19 Vaccine",
                appointmentDate: "2024-01-15",
                institutionName: "Test Hospital",
                status: "completed"
            },
            {
                id: 2,
                vaccineName: "Influenza Vaccine",
                appointmentDate: "2024-02-15",
                institutionName: "Delmon Hospital",
                status: "completed"
            }
        ],
        pendingVaccinations: [
            {
                id: 3,
                vaccineName: "Hepatitis B Vaccine",
                appointmentDate: "2024-03-15",
                timeSlot: "11:30 a.m.",
                institutionName: "Delmon Hospital"
            }
        ]
    };
    
    displayPatientInfo(sampleData);
}

// Test function to call real API
function testRealAPI() {
    console.log('Testing real API with patient number 1001...');
    searchPatient('1001');
}

// Test function to check if API endpoint exists
function testAPIEndpoint() {
    console.log('Testing API endpoint accessibility...');
    fetch('/api/doctor/test-search')
        .then(response => {
            console.log('Test endpoint response status:', response.status);
            return response.json();
        })
        .then(data => {
            console.log('Test endpoint response data:', data);
        })
        .catch(error => {
            console.error('Test endpoint error:', error);
        });
}

// Add CSS styles for status badges and date picker modal
const style = document.createElement('style');
style.textContent = `
    .status-badge {
        padding: 4px 8px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: bold;
        display: inline-block;
    }
    
    .status-badge.pending {
        background-color: #fff3cd;
        color: #856404;
    }
    
    .status-badge.completed {
        background-color: #d4edda;
        color: #155724;
    }
    
    .vaccination__table__sub {
        display: flex;
        align-items: center;
        gap: 5px;
    }
    
    .vaccination__table__sub img {
        width: 16px;
        height: 16px;
    }
    
    .dosage-unit {
        color: #666;
        font-weight: normal;
        margin-left: 2px;
    }
    
    
    /* Date Picker Modal Styles */
    .date-picker-modal {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        z-index: 1000;
        display: flex;
        justify-content: center;
        align-items: center;
    }
    
    .date-picker-content {
        background: white;
        border-radius: 10px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
        width: 90%;
        max-width: 500px;
        max-height: 90vh;
        overflow-y: auto;
    }
    
    .date-picker-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 25px 15px;
        border-bottom: 1px solid #e0e0e0;
    }
    
    .date-picker-header h3 {
        margin: 0;
        color: #333;
        font-size: 18px;
        font-weight: 600;
    }
    
    .date-picker-close {
        font-size: 24px;
        font-weight: bold;
        color: #999;
        cursor: pointer;
        line-height: 1;
        padding: 5px;
        border-radius: 50%;
        transition: all 0.2s ease;
    }
    
    .date-picker-close:hover {
        background-color: #f0f0f0;
        color: #666;
    }
    
    .date-picker-body {
        padding: 25px;
    }
    
    .date-picker-body p {
        margin: 0 0 20px 0;
        color: #666;
        font-size: 14px;
        line-height: 1.5;
    }
    
    .date-input-container {
        margin-bottom: 25px;
    }
    
    .date-input {
        width: 100%;
        padding: 12px 15px;
        border: 2px solid #e0e0e0;
        border-radius: 8px;
        font-size: 16px;
        color: #333;
        background-color: #fff;
        transition: border-color 0.2s ease;
    }
    
    .date-input:focus {
        outline: none;
        border-color: #007bff;
        box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
    }
    
    .date-picker-actions {
        display: flex;
        gap: 10px;
        justify-content: flex-end;
        flex-wrap: wrap;
    }
    
    .date-picker-actions button {
        padding: 10px 20px;
        border: none;
        border-radius: 6px;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s ease;
        min-width: 120px;
    }
    
    .clear-date-btn {
        background-color: #6c757d;
        color: white;
    }
    
    .clear-date-btn:hover {
        background-color: #5a6268;
    }
    
    .save-date-btn {
        background-color: #28a745;
        color: white;
    }
    
    .save-date-btn:hover {
        background-color: #218838;
    }
    
    .cancel-date-btn {
        background-color: #dc3545;
        color: white;
    }
    
    .cancel-date-btn:hover {
        background-color: #c82333;
    }
    
    /* Responsive design */
    @media (max-width: 600px) {
        .date-picker-content {
            width: 95%;
            margin: 10px;
        }
        
        .date-picker-header {
            padding: 15px 20px 10px;
        }
        
        .date-picker-body {
            padding: 20px;
        }
        
        .date-picker-actions {
            flex-direction: column;
        }
        
        .date-picker-actions button {
            width: 100%;
            min-width: auto;
        }
    }
`;
document.head.appendChild(style);

// Run API test when page loads
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ JavaScript loaded successfully!');
    console.log('Page loaded, running API test...');
    testAPI();
    
    // Initialize date picker modal
    initializeDatePickerModal();
    
    // Load recent updates
    loadRecentUpdates();
});

// Simple test to verify JavaScript is working
console.log('🚀 DoctorPatientHistory.js loaded successfully!');