// Global variables
let currentHospitalId = null;
let currentAppointments = [];
let filteredAppointments = [];
let isFiltered = false;

// announcement
const cancelAnnouncement = document.querySelector(".announcement__cancel")
const announcement = document.querySelector(".announcement")

cancelAnnouncement.addEventListener("click", function(){
    announcement.style.display = "none"
})

// menu bar
const menuBar = document.querySelector(".navbar__section4")
const cancelMenubar = document.querySelector(".responsive__navbar__section1 p")
const responsiveNavbar = document.querySelector(".responsive__navbar")
const navbarLink = document.querySelectorAll(".responsive__navbar__section1 a")

menuBar.addEventListener("click", function(){
    responsiveNavbar.style.right = "0%"
})

cancelMenubar.addEventListener("click", function(){
    responsiveNavbar.style.right = "-80%"
})

navbarLink.forEach(function(link){
    link.addEventListener("click", function(){
        responsiveNavbar.style.right = "-80%"
    })
})

//profile 
const profile = document.querySelector(".navbar__section3")
const cancelProfile = document.querySelector(".profile__close")
const profileBox = document.querySelector(".profile")

profile.addEventListener("click", function(){
    profileBox.style.right = "1%"
})

cancelProfile.addEventListener("click", function(){
    profileBox.style.right = "-20%"
})

// Dynamic functionality for hospitals and appointments
document.addEventListener('DOMContentLoaded', function() {
    loadHospitals();
});

/**
 * Load institutions (hospitals and clinics) that have invited the doctor
 */
async function loadHospitals() {
    try {
        console.log('Loading institutions...');
        
        const response = await fetch('/api/doctor/hospitals');
        console.log('Response status:', response.status);
        console.log('Response headers:', response.headers);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('API Response:', data);
        
        if (data.success) {
            displayHospitals(data.hospitals || data.institutions);
        } else {
            console.error('Failed to load institutions:', data.error);
            displayError(`Failed to load institutions: ${data.error || 'Unknown error'}`);
        }
    } catch (error) {
        console.error('Error loading institutions:', error);
        displayError(`Error loading institutions: ${error.message}`);
    }
}

/**
 * Display institutions (hospitals and clinics) in the UI
 */
function displayHospitals(institutions) {
    const hospitalsList = document.getElementById('hospitalsList');
    
    if (institutions.length === 0) {
        hospitalsList.innerHTML = '<div class="no-hospitals">No institutions have invited you yet.</div>';
        return;
    }
    
    hospitalsList.innerHTML = '';
    
    institutions.forEach(institution => {
        const institutionElement = document.createElement('p');
        institutionElement.className = 'hospital-item';
        
        // Display institution name with type indicator
        const displayName = institution.username || institution.id;
        const typeIndicator = institution.type === 'Clinic' ? ' (Clinic)' : ' (Hospital)';
        institutionElement.textContent = displayName + typeIndicator;
        
        institutionElement.dataset.hospitalId = institution.id;
        institutionElement.dataset.institutionType = institution.type;
        institutionElement.addEventListener('click', () => selectHospital(institution.id, displayName, institution.type));
        hospitalsList.appendChild(institutionElement);
    });
}

/**
 * Select an institution (hospital or clinic) and load its appointments
 */
async function selectHospital(institutionId, institutionName, institutionType = 'Hospital') {
    try {
        console.log('Selecting institution:', institutionId, 'Type:', institutionType);
        
        // Update UI to show selected institution
        const selectedHospitalElement = document.getElementById('selectedHospital');
        const typeIndicator = institutionType === 'Clinic' ? ' (Clinic)' : ' (Hospital)';
        selectedHospitalElement.textContent = institutionName + typeIndicator;
        selectedHospitalElement.setAttribute('data-hospital-id', institutionId);
        selectedHospitalElement.setAttribute('data-institution-type', institutionType);
        
        // Remove previous selection
        document.querySelectorAll('.hospital-item').forEach(item => {
            item.classList.remove('selected');
        });
        
        // Add selection to clicked item
        document.querySelector(`[data-hospital-id="${institutionId}"]`).classList.add('selected');
        
        // Load appointments for this institution
        await loadAppointments(institutionId, institutionType);
        
        // Show the date filter section
        document.getElementById('dateFilterSection').style.display = 'block';
        
        currentHospitalId = institutionId;
        
    } catch (error) {
        console.error('Error selecting institution:', error);
        displayError('Error selecting institution');
    }
}

/**
 * Load appointments for the selected institution (hospital or clinic)
 */
async function loadAppointments(institutionId, institutionType = 'Hospital') {
    try {
        console.log('Loading appointments for institution:', institutionId, 'Type:', institutionType);
        
        const response = await fetch(`/api/doctor/appointments/${institutionId}`);
        console.log('Appointments response status:', response.status);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('Appointments API Response:', data);
        
        if (data.success) {
            currentAppointments = data.appointments;
            filteredAppointments = [...data.appointments]; // Initialize filtered appointments
            isFiltered = false; // Reset filter state
            console.log('Current appointments:', currentAppointments);
            displayAppointments(data.appointments);
            updateFilterInfo();
        } else {
            console.error('Failed to load appointments:', data.error);
            displayError(`Failed to load appointments: ${data.error || 'Unknown error'}`);
        }
    } catch (error) {
        console.error('Error loading appointments:', error);
        displayError(`Error loading appointments: ${error.message}`);
    }
}

/**
 * Display appointments in the table
 */
function displayAppointments(appointments) {
    const table = document.querySelector('#appointmentsTable table');
    const noAppointmentsRow = document.getElementById('noAppointmentsRow');
    
    // Remove existing appointment rows (keep header and no-appointments row)
    const existingRows = table.querySelectorAll('tr.appointment-row');
    existingRows.forEach(row => row.remove());
    
    if (appointments.length === 0) {
        noAppointmentsRow.style.display = 'table-row';
        return;
    }
    
    noAppointmentsRow.style.display = 'none';
    
    appointments.forEach(appointment => {
        const row = createAppointmentRow(appointment);
        table.appendChild(row);
    });
}

/**
 * Create a table row for an appointment
 */
function createAppointmentRow(appointment) {
    const row = document.createElement('tr');
    row.className = 'appointment-row';
    row.dataset.appointmentId = appointment.id;
    
    // Format date
    const appointmentDate = new Date(appointment.appointmentDate);
    const formattedDate = appointmentDate.toLocaleDateString();
    
    // Format time slot
    const timeSlot = appointment.timeSlot || 'N/A';
    
    row.innerHTML = `
        <td>${appointment.patientId || 'N/A'}</td>
        <td>${appointment.patientName || 'N/A'}</td>
        <td>${formattedDate}</td>
        <td>${timeSlot}</td>
        <td>${appointment.vaccineName || 'N/A'}</td>
    `;
    
    return row;
}



/**
 * Display error message
 */
function displayError(message) {
    const hospitalsList = document.getElementById('hospitalsList');
    hospitalsList.innerHTML = `<div class="error-message">${message}</div>`;
}

/**
 * Show temporary message
 */
function showMessage(message, type) {
    // Create message element
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = message;
    
    // Add to page
    document.body.appendChild(messageDiv);
    
    // Remove after 3 seconds
    setTimeout(() => {
        if (messageDiv.parentNode) {
            messageDiv.parentNode.removeChild(messageDiv);
        }
    }, 3000);
}

// Add some CSS for the dynamic elements
const style = document.createElement('style');
style.textContent = `
    .hospital-item {
        cursor: pointer;
        padding: 10px;
        margin: 5px 0;
        border: 1px solid #ddd;
        border-radius: 5px;
        transition: background-color 0.3s;
    }
    
    .hospital-item:hover {
        background-color: #f0f0f0;
    }
    
    .hospital-item.selected {
        background-color: #e3f2fd;
        border-color: #2196f3;
    }
    
    .loading, .no-hospitals, .error-message {
        text-align: center;
        padding: 20px;
        color: #666;
    }
    
    .error-message {
        color: #f44336;
    }
    
    
    
    .message {
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 5px;
        z-index: 1000;
        font-weight: bold;
    }
    
    .message.success {
        background-color: #d4edda;
        color: #155724;
        border: 1px solid #c3e6cb;
    }
    
    .message.error {
        background-color: #f8d7da;
        color: #721c24;
        border: 1px solid #f5c6cb;
    }
`;
document.head.appendChild(style);

/**
 * Filter appointments by selected date
 */
function filterAppointmentsByDate() {
    const dateFilter = document.getElementById('dateFilter');
    const selectedDate = dateFilter.value;
    
    if (!selectedDate) {
        clearDateFilter();
        return;
    }
    
    console.log('Filtering appointments by date:', selectedDate);
    
    // Filter appointments by the selected date
    filteredAppointments = currentAppointments.filter(appointment => {
        const appointmentDate = new Date(appointment.appointmentDate);
        const filterDate = new Date(selectedDate);
        
        // Compare dates (ignore time)
        return appointmentDate.toDateString() === filterDate.toDateString();
    });
    
    isFiltered = true;
    displayAppointments(filteredAppointments);
    updateFilterInfo();
    
    console.log(`Filtered ${filteredAppointments.length} appointments for date: ${selectedDate}`);
}

/**
 * Clear date filter and show all appointments
 */
function clearDateFilter() {
    console.log('Clearing date filter');
    
    // Clear the date input
    document.getElementById('dateFilter').value = '';
    
    // Reset filtered appointments to show all
    filteredAppointments = [...currentAppointments];
    isFiltered = false;
    
    // Display all appointments
    displayAppointments(currentAppointments);
    updateFilterInfo();
    
    console.log('Showing all appointments:', currentAppointments.length);
}

/**
 * Update filter information display
 */
function updateFilterInfo() {
    const filterInfo = document.getElementById('filterInfo');
    const totalAppointments = currentAppointments.length;
    const displayedAppointments = filteredAppointments.length;
    
    if (isFiltered) {
        filterInfo.innerHTML = `
            <span class="filter-status">
                Showing ${displayedAppointments} of ${totalAppointments} appointments
                <span class="filter-date">for selected date</span>
            </span>
        `;
    } else {
        filterInfo.innerHTML = `
            <span class="filter-status">
                Showing all ${totalAppointments} appointments
            </span>
        `;
    }
}

// Logout function
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        // Create a form and submit it to logout endpoint
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/logout';
        
        // Add CSRF token if available
        const csrfToken = document.querySelector('meta[name="_csrf"]');
        if (csrfToken) {
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = csrfToken.getAttribute('content');
            form.appendChild(csrfInput);
        }
        
        document.body.appendChild(form);
        form.submit();
    }
}
