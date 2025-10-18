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

if (navbarLink && responsiveNavbar) {
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

// Vaccination Schedule Functions

/**
 * Load doctors for the current clinic
 */
function loadDoctors() {
    console.log('Loading doctors for current clinic...');
    
    fetch('/clinic/doctors', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => {
        console.log('Doctors response status:', response.status);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('Doctors response data:', data);
        if (data.success && data.doctors) {
            populateDoctorDropdown(data.doctors);
        } else {
            console.warn('No doctors found or error in response:', data);
            showNotification('⚠️ No doctors found for this clinic', 'warning');
        }
    })
    .catch(error => {
        console.error('Error loading doctors:', error);
        console.error('Error details:', {
            message: error.message,
            stack: error.stack,
            name: error.name
        });
        showNotification('❌ Error loading doctors: ' + error.message, 'error');
    });
}

/**
 * Load vaccines for the dropdown
 */
function loadVaccines() {
    console.log('Loading vaccines for dropdown...');
    
    fetch('/clinic/vaccines', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => {
        console.log('Vaccines response status:', response.status);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('Vaccines response data:', data);
        if (data.success && data.vaccines) {
            populateVaccineDropdown(data.vaccines);
        } else {
            console.warn('No vaccines found or error in response:', data);
            showNotification('⚠️ No vaccines found', 'warning');
        }
    })
    .catch(error => {
        console.error('Error loading vaccines:', error);
        console.error('Error details:', {
            message: error.message,
            stack: error.stack,
            name: error.name
        });
        showNotification('❌ Failed to load vaccines', 'error');
    });
}

/**
 * Populate the vaccine dropdown with fetched vaccines
 */
function populateVaccineDropdown(vaccines) {
    const vaccineSelect = document.getElementById('vaccine-type');
    if (!vaccineSelect) {
        console.error('Vaccine select element not found');
        return;
    }
    
    // Clear existing options except the first one
    vaccineSelect.innerHTML = '<option value="">Select a Vaccine</option>';
    
    if (vaccines && vaccines.length > 0) {
        vaccines.forEach(vaccine => {
            const option = document.createElement('option');
            option.value = vaccine.name;
            option.textContent = vaccine.name;
            option.setAttribute('data-vaccine-id', vaccine.id);
            vaccineSelect.appendChild(option);
        });
        console.log(`Populated vaccine dropdown with ${vaccines.length} vaccines`);
    } else {
        const option = document.createElement('option');
        option.value = '';
        option.textContent = 'No vaccines available';
        option.disabled = true;
        vaccineSelect.appendChild(option);
        console.log('No vaccines available');
    }
}

/**
 * Populate the doctor dropdown with fetched doctors
 */
function populateDoctorDropdown(doctors) {
    const doctorSelect = document.getElementById('doctor');
    if (!doctorSelect) {
        console.error('Doctor select element not found');
        return;
    }
    
    // Clear existing options except the first one
    doctorSelect.innerHTML = '<option value="">Select a Doctor</option>';
    
    if (doctors && doctors.length > 0) {
        doctors.forEach(doctor => {
            const option = document.createElement('option');
            option.value = doctor.name;
            option.textContent = doctor.name;
            option.setAttribute('data-doctor-id', doctor.id);
            option.setAttribute('data-doctor-email', doctor.email);
            doctorSelect.appendChild(option);
        });
        console.log(`Populated dropdown with ${doctors.length} doctors`);
    } else {
        const option = document.createElement('option');
        option.value = '';
        option.textContent = 'No doctors available';
        option.disabled = true;
        doctorSelect.appendChild(option);
        console.log('No doctors available for this clinic');
    }
}

/**
 * Add new vaccination schedule
 */
function addSchedule() {
    console.log('Add schedule function called');
    
    try {
        // Get form data
        const doctorName = document.getElementById('doctor')?.value;
        const vaccineName = document.getElementById('vaccine-type')?.value;
        const timeFrom = document.getElementById('timeFrom')?.value;
        const timeTo = document.getElementById('timeTo')?.value;
        
        console.log('Form data retrieved:', {
            doctorName,
            vaccineName,
            timeFrom,
            timeTo
        });
    
        // Get selected days
        const selectedDays = [];
        const dayCheckboxes = document.querySelectorAll('input[name="days"]:checked');
        dayCheckboxes.forEach(checkbox => {
            selectedDays.push(checkbox.value);
        });
        
        console.log('Selected days:', selectedDays);
        
        // Validate required fields
        if (!doctorName || !vaccineName || !timeFrom || !timeTo) {
            showNotification('❌ Please fill in all required fields', 'error');
            return;
        }
        
        // Validate that at least one day is selected
        if (selectedDays.length === 0) {
            showNotification('❌ Please select at least one vaccination day', 'error');
            return;
        }
        
        // Validate time range
        if (timeFrom >= timeTo) {
            showNotification('❌ End time must be after start time', 'error');
            return;
        }
        
        // Prepare request data
        const requestData = {
            doctorName: doctorName,
            vaccineName: vaccineName,
            timeFrom: timeFrom,
            timeTo: timeTo,
            days: selectedDays.join(',')
        };
        
        console.log('Sending request:', requestData);
    
        // Show loading state
        const addButton = document.getElementById('add-schedule-btn');
        if (!addButton) {
            showNotification('❌ Add button not found', 'error');
            return;
        }
        const originalText = addButton.textContent;
        addButton.textContent = 'Adding...';
        addButton.disabled = true;
    
    // Make API call
    fetch('/clinic/add-schedule', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify(requestData)
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
            showNotification('✅ Vaccination schedule added successfully!', 'success');
            // Clear form
            document.getElementById('doctor').value = '';
            document.getElementById('vaccine-type').value = '';
            document.getElementById('timeFrom').value = '';
            document.getElementById('timeTo').value = '';
            // Clear all day checkboxes
            document.querySelectorAll('input[name="days"]').forEach(checkbox => {
                checkbox.checked = false;
            });
            // Reload page to show new schedule
            setTimeout(() => {
                window.location.reload();
            }, 1500);
        } else {
            throw new Error(data.error || 'Failed to add vaccination schedule');
        }
    })
    .catch(error => {
        console.error('Add schedule error:', error);
        console.error('Error details:', {
            message: error.message,
            stack: error.stack,
            name: error.name
        });
        showNotification('❌ Error adding vaccination schedule: ' + error.message, 'error');
    })
    .finally(() => {
        // Restore button state
        if (addButton) {
            addButton.textContent = originalText;
            addButton.disabled = false;
        }
    });
    } catch (error) {
        console.error('Error in addSchedule function:', error);
        showNotification('❌ An error occurred while adding the schedule', 'error');
    }
}

/**
 * Cancel vaccination schedule
 */
function cancelSchedule(scheduleId) {
    console.log('Cancel schedule function called with ID:', scheduleId);
    
    try {
        if (!confirm('Are you sure you want to cancel this vaccination schedule? This action cannot be undone.')) {
            return;
        }
    
    // Make API call
    fetch(`/clinic/cancel-schedule/${scheduleId}`, {
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
            showNotification('✅ Vaccination schedule cancelled successfully!', 'success');
            // Remove the cancelled schedule from the UI immediately
            removeScheduleFromUI(scheduleId);
        } else {
            throw new Error(data.error || 'Failed to cancel vaccination schedule');
        }
    })
    .catch(error => {
        console.error('Cancel schedule error:', error);
        showNotification('❌ Error cancelling vaccination schedule: ' + error.message, 'error');
    });
    } catch (error) {
        console.error('Error in cancelSchedule function:', error);
        showNotification('❌ An error occurred while cancelling the schedule', 'error');
    }
}

/**
 * Remove schedule from UI after successful cancellation
 */
function removeScheduleFromUI(scheduleId) {
    console.log('Removing schedule from UI with ID:', scheduleId);
    
    try {
        // Find the table row with the matching schedule ID
        const scheduleRow = document.querySelector(`tr[data-schedule-id="${scheduleId}"]`);
        
        if (scheduleRow) {
            // Remove the row from the table
            scheduleRow.remove();
            console.log('Schedule row removed from UI');
            
            // Update the schedule count
            updateScheduleCount();
            
            // Check if there are no more schedules
            const remainingRows = document.querySelectorAll('tr[data-schedule-id]');
            if (remainingRows.length === 0) {
                showNoSchedulesMessage();
            }
        } else {
            console.warn('Schedule row not found in UI for ID:', scheduleId);
            // Fallback: reload the page
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        }
    } catch (error) {
        console.error('Error removing schedule from UI:', error);
        // Fallback: reload the page
        setTimeout(() => {
            window.location.reload();
        }, 1000);
    }
}

/**
 * Update the schedule count in the UI
 */
function updateScheduleCount() {
    const remainingRows = document.querySelectorAll('tr[data-schedule-id]');
    const scheduleCountElement = document.getElementById('schedule-count');
    
    if (scheduleCountElement) {
        scheduleCountElement.textContent = `(${remainingRows.length})`;
    }
}

/**
 * Show "no schedules" message when all schedules are cancelled
 */
function showNoSchedulesMessage() {
    const table = document.querySelector('.schedule__summary table');
    const noSchedulesDiv = document.querySelector('.no-schedules');
    
    if (table && !noSchedulesDiv) {
        // Hide the table
        table.style.display = 'none';
        
        // Create and show the "no schedules" message
        const messageDiv = document.createElement('div');
        messageDiv.className = 'no-schedules';
        messageDiv.innerHTML = '<p>No vaccination schedules found. Create your first schedule above.</p>';
        
        // Insert the message after the heading
        const heading = document.querySelector('.schedule__summary h2');
        if (heading) {
            heading.parentNode.insertBefore(messageDiv, heading.nextSibling);
        }
    }
}

/**
 * Show notification message
 */
function showNotification(message, type) {
    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    // Add styles
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 5px;
        color: white;
        font-weight: bold;
        z-index: 10000;
        max-width: 400px;
        word-wrap: break-word;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        animation: slideIn 0.3s ease-out;
    `;
    
    // Set background color based on type
    if (type === 'success') {
        notification.style.backgroundColor = '#4CAF50';
    } else if (type === 'error') {
        notification.style.backgroundColor = '#f44336';
    } else {
        notification.style.backgroundColor = '#2196F3';
    }
    
    // Add animation styles
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideIn {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
    `;
    document.head.appendChild(style);
    
    // Add to page
    document.body.appendChild(notification);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.style.animation = 'slideIn 0.3s ease-out reverse';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.remove();
                }
            }, 300);
        }
    }, 5000);
}

/**
 * Load appointments for the current clinic
 */
function loadAppointments() {
    console.log('Loading appointments for current clinic...');
    
    fetch('/clinic/appointments-data', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => {
        console.log('Appointments response status:', response.status);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('Appointments response data:', data);
        if (data.success && data.appointments) {
            updateAppointmentsTable(data.appointments);
        } else {
            console.warn('No appointments found or error in response:', data);
            showNotification('⚠️ No appointments found', 'warning');
        }
    })
    .catch(error => {
        console.error('Error loading appointments:', error);
        showNotification('❌ Error loading appointments: ' + error.message, 'error');
    });
}

/**
 * Update the appointments table with fetched data
 */
function updateAppointmentsTable(appointments) {
    const appointmentsTable = document.querySelector('.appointments__summary table');
    const noAppointmentsDiv = document.querySelector('.no-appointments');
    const appointmentCountElement = document.getElementById('appointment-count');
    
    if (!appointmentsTable) {
        console.error('Appointments table not found');
        return;
    }
    
    // Update appointment count
    if (appointmentCountElement) {
        appointmentCountElement.textContent = `(${appointments.length})`;
    }
    
    if (appointments.length === 0) {
        // Show no appointments message
        if (appointmentsTable) {
            appointmentsTable.style.display = 'none';
        }
        if (!noAppointmentsDiv) {
            const messageDiv = document.createElement('div');
            messageDiv.className = 'no-appointments';
            messageDiv.innerHTML = '<p>No appointments found. Patients can book appointments based on your vaccination schedules.</p>';
            
            const heading = document.querySelector('.appointments__summary h2');
            if (heading) {
                heading.parentNode.insertBefore(messageDiv, heading.nextSibling);
            }
        }
        return;
    }
    
    // Hide no appointments message if it exists
    if (noAppointmentsDiv) {
        noAppointmentsDiv.remove();
    }
    
    // Show table
    appointmentsTable.style.display = 'table';
    
    // Clear existing appointment rows (keep header)
    const existingRows = appointmentsTable.querySelectorAll('tr[data-appointment-id]');
    existingRows.forEach(row => row.remove());
    
    // Add new appointment rows
    appointments.forEach(appointment => {
        const row = createAppointmentRow(appointment);
        appointmentsTable.appendChild(row);
    });
    
    console.log(`Updated appointments table with ${appointments.length} appointments`);
}

/**
 * Create a table row for an appointment
 */
function createAppointmentRow(appointment) {
    const row = document.createElement('tr');
    row.setAttribute('data-appointment-id', appointment.id);
    
    // Format time slot for display (convert "08:00-08:20" to "8:00 AM - 8:20 AM")
    const formattedTime = formatTimeSlot(appointment.timeSlot);
    
    // Create status badge
    const statusBadge = createStatusBadge(appointment.status);
    
    // Create action button
    const actionButton = createActionButton(appointment);
    
    row.innerHTML = `
        <td>${appointment.patientName}</td>
        <td>${appointment.appointmentDate}</td>
        <td>${formattedTime}</td>
        <td>${appointment.vaccineName}</td>
        <td>${appointment.doctorName}</td>
        <td>${statusBadge}</td>
        <td>${actionButton}</td>
    `;
    
    return row;
}

/**
 * Format time slot for display
 */
function formatTimeSlot(timeSlot) {
    if (!timeSlot) return '';
    
    try {
        const [startTime, endTime] = timeSlot.split('-');
        const formatTime = (time) => {
            const [hours, minutes] = time.split(':');
            const hour = parseInt(hours);
            const ampm = hour >= 12 ? 'PM' : 'AM';
            const displayHour = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
            return `${displayHour}:${minutes} ${ampm}`;
        };
        
        return `${formatTime(startTime)} - ${formatTime(endTime)}`;
    } catch (error) {
        console.error('Error formatting time slot:', error);
        return timeSlot;
    }
}

/**
 * Create status badge HTML
 */
function createStatusBadge(status) {
    const statusClasses = {
        'scheduled': 'status-scheduled',
        'completed': 'status-completed',
        'cancelled': 'status-cancelled',
        'no_show': 'status-no-show'
    };
    
    const statusTexts = {
        'scheduled': 'Scheduled',
        'completed': 'Completed',
        'cancelled': 'Cancelled',
        'no_show': 'No Show'
    };
    
    const className = statusClasses[status] || 'status-scheduled';
    const text = statusTexts[status] || status;
    
    return `<span class="${className}">${text}</span>`;
}

/**
 * Create action button HTML
 */
function createActionButton(appointment) {
    if (appointment.status === 'scheduled') {
        return `<button onclick="cancelAppointment(${appointment.id})" class="cancel-appointment-btn">Cancel</button>`;
    } else if (appointment.status === 'cancelled' && appointment.cancelledBy === 'hospital') {
        return `<button onclick="reverseAppointment(${appointment.id})" class="reverse-appointment-btn">Reverse</button>`;
    } else {
        return '<span class="no-action">-</span>';
    }
}

/**
 * Cancel an appointment
 */
function cancelAppointment(appointmentId) {
    console.log('Cancel appointment function called with ID:', appointmentId);
    
    try {
        if (!confirm('Are you sure you want to cancel this appointment? This action cannot be undone and the patient will be notified.')) {
            return;
        }
    
        // Make API call
        fetch(`/clinic/cancel-appointment/${appointmentId}`, {
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
                showNotification('✅ Appointment cancelled successfully!', 'success');
                // Remove the cancelled appointment from the UI immediately
                removeAppointmentFromUI(appointmentId);
            } else {
                throw new Error(data.error || 'Failed to cancel appointment');
            }
        })
        .catch(error => {
            console.error('Cancel appointment error:', error);
            showNotification('❌ Error cancelling appointment: ' + error.message, 'error');
        });
    } catch (error) {
        console.error('Error in cancelAppointment function:', error);
        showNotification('❌ An error occurred while cancelling the appointment', 'error');
    }
}

/**
 * Remove appointment from UI after successful cancellation
 */
function removeAppointmentFromUI(appointmentId) {
    console.log('Removing appointment from UI with ID:', appointmentId);
    
    try {
        // Find the table row with the matching appointment ID
        const appointmentRow = document.querySelector(`tr[data-appointment-id="${appointmentId}"]`);
        
        if (appointmentRow) {
            // Remove the row from the table
            appointmentRow.remove();
            console.log('Appointment row removed from UI');
            
            // Update the appointment count
            updateAppointmentCount();
            
            // Check if there are no more appointments
            const remainingRows = document.querySelectorAll('tr[data-appointment-id]');
            if (remainingRows.length === 0) {
                showNoAppointmentsMessage();
            }
        } else {
            console.warn('Appointment row not found in UI for ID:', appointmentId);
            // Fallback: reload the page
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        }
    } catch (error) {
        console.error('Error removing appointment from UI:', error);
        // Fallback: reload the page
        setTimeout(() => {
            window.location.reload();
        }, 1000);
    }
}

/**
 * Update the appointment count in the UI
 */
function updateAppointmentCount() {
    const remainingRows = document.querySelectorAll('tr[data-appointment-id]');
    const appointmentCountElement = document.getElementById('appointment-count');
    
    if (appointmentCountElement) {
        appointmentCountElement.textContent = `(${remainingRows.length})`;
    }
}

/**
 * Show "no appointments" message when all appointments are cancelled
 */
function showNoAppointmentsMessage() {
    const table = document.querySelector('.appointments__summary table');
    const noAppointmentsDiv = document.querySelector('.no-appointments');
    
    if (table && !noAppointmentsDiv) {
        // Hide the table
        table.style.display = 'none';
        
        // Create and show the "no appointments" message
        const messageDiv = document.createElement('div');
        messageDiv.className = 'no-appointments';
        messageDiv.innerHTML = '<p>No appointments found. Patients can book appointments based on your vaccination schedules.</p>';
        
        // Insert the message after the heading
        const heading = document.querySelector('.appointments__summary h2');
        if (heading) {
            heading.parentNode.insertBefore(messageDiv, heading.nextSibling);
        }
    }
}

/**
 * Reverse a cancelled appointment
 */
function reverseAppointment(appointmentId) {
    console.log('Reverse appointment function called with ID:', appointmentId);
    
    try {
        if (!confirm('Are you sure you want to reverse this cancelled appointment? The system will automatically find the next available time slot and reschedule the patient.')) {
            return;
        }
    
        // Make API call
        fetch(`/clinic/reverse-appointment/${appointmentId}`, {
            method: 'POST',
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
                showNotification('✅ Appointment reversed and rescheduled successfully!', 'success');
                // Reload the appointments to show updated status
                setTimeout(() => {
                    loadAppointments();
                }, 1500);
            } else {
                throw new Error(data.error || 'Failed to reverse appointment');
            }
        })
        .catch(error => {
            console.error('Reverse appointment error:', error);
            showNotification('❌ Error reversing appointment: ' + error.message, 'error');
        });
    } catch (error) {
        console.error('Error in reverseAppointment function:', error);
        showNotification('❌ An error occurred while reversing the appointment', 'error');
    }
}

// Load doctors, vaccines and appointments when the page loads
document.addEventListener('DOMContentLoaded', function() {
    console.log('Page loaded, loading doctors, vaccines and appointments...');
    loadDoctors();
    loadVaccines();
    loadAppointments();
});