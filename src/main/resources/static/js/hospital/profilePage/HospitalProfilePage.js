// Hospital Profile Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('Hospital Profile Page loaded');
    
    // Load doctors and nurses dynamically
    loadDoctors();
    loadNurses();
    
    // Initialize edit button functionality
    initializeEditButton();
});

// Load doctors from the database
function loadDoctors() {
    console.log('Loading doctors...');
    
    fetch('/hospital/doctors')
    .then(response => {
        console.log('Doctors response status:', response.status);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('Doctors response data:', data);
        
        const container = document.getElementById('doctorsContainer');
        
        if (data.success && data.doctors && data.doctors.length > 0) {
            displayStaff(container, data.doctors, 'Doctor', '/hospital/add-new-staff');
        } else {
            container.innerHTML = `
                <div class="no-staff-message">
                    No doctors found. 
                    <a href="/hospital/add-new-staff">Add a doctor</a>
                </div>
                <div class="staff__details__doctor__card">
                    <a href="/hospital/add-new-staff">
                        <img class="staff__details__doctor__card__plus" src="/images/plus.png" alt="plus">
                    </a>
                </div>
            `;
        }
    })
    .catch(error => {
        console.error('Error loading doctors:', error);
        const container = document.getElementById('doctorsContainer');
        container.innerHTML = `
            <div class="error-message">
                Error loading doctors: ${error.message}
                <br><a href="/debug/test-endpoints">Test endpoints</a>
            </div>
        `;
    });
}

// Load nurses from the database
function loadNurses() {
    console.log('Loading nurses...');
    
    fetch('/hospital/nurses')
            .then(response => {
        console.log('Nurses response status:', response.status);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }
        return response.json();
            })
            .then(data => {
        console.log('Nurses response data:', data);
        
        const container = document.getElementById('nursesContainer');
        
        if (data.success && data.nurses && data.nurses.length > 0) {
            displayStaff(container, data.nurses, 'Nurse', '/hospital/add-new-staff');
        } else {
            container.innerHTML = `
                <div class="no-staff-message">
                    No nurses found. 
                    <a href="/hospital/add-new-staff">Add a nurse</a>
                </div>
                <div class="staff__details__doctor__card">
                    <a href="/hospital/add-new-staff">
                        <img class="staff__details__doctor__card__plus" src="/images/plus.png" alt="plus">
                    </a>
                </div>
            `;
        }
            })
            .catch(error => {
        console.error('Error loading nurses:', error);
        const container = document.getElementById('nursesContainer');
        container.innerHTML = `
            <div class="error-message">
                Error loading nurses: ${error.message}
                <br><a href="/debug/test-endpoints">Test endpoints</a>
            </div>
        `;
    });
}

// Display staff members in the container
function displayStaff(container, staffList, role, addStaffUrl) {
    console.log(`Displaying ${staffList.length} ${role.toLowerCase()}s`);
    
    let html = '';
    
    // Add staff cards
    staffList.forEach(staff => {
        const title = role === 'Doctor' ? `Dr. ${staff.name}` : staff.name;
        const qualifications = staff.qualifications || 'No qualifications specified';
        const specialization = staff.specialization || 'General';
        
        html += `
            <div class="staff__details__doctor__card">
                <img src="/images/profile.png" alt="${role.toLowerCase()}">
                <h3>${title}</h3>
                <p>${qualifications}</p>
                <p>${specialization}</p>
                <div class="staff-actions">
                    <button onclick="deleteStaff(${staff.id}, '${staff.email}', '${role}')" class="btn-delete">Delete</button>
                </div>
            </div>
        `;
    });
    
    // Add the "Add New" card
        html += `
            <div class="staff__details__doctor__card">
                <a href="${addStaffUrl}">
                    <img class="staff__details__doctor__card__plus" src="/images/plus.png" alt="plus">
                </a>
            </div>
        `;
    
    container.innerHTML = html;
}

// Delete staff member
function deleteStaff(staffId, email, role) {
    console.log('Delete staff:', { staffId, email, role });
    
    if (!confirm(`Are you sure you want to remove ${role} ${email} from this hospital? They will receive an email notification.`)) {
        return;
    }
    
    // Show loading state
    const button = event.target;
    const originalText = button.textContent;
    button.textContent = 'Deleting...';
    button.disabled = true;
    
    fetch('/hospital/remove-staff', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `staffId=${staffId}`
    })
    .then(response => response.json())
    .then(data => {
        console.log('Delete response:', data);
        
        if (data.success) {
            alert(`${role} has been successfully removed from the hospital. They have been notified via email.`);
            // Reload the staff lists
            loadDoctors();
            loadNurses();
        } else {
            alert(`Error removing ${role}: ${data.error || 'Unknown error'}`);
        }
    })
    .catch(error => {
        console.error('Error deleting staff:', error);
        alert(`Error removing ${role}. Please try again.`);
    })
    .finally(() => {
        // Reset button state
        button.textContent = originalText;
        button.disabled = false;
    });
}

// Initialize edit button functionality
function initializeEditButton() {
    console.log('Initializing edit button functionality');
    
    // Get the edit button
    const editBtn = document.getElementById('editHospitalBtn');
    console.log('Edit button found:', editBtn);
    if (!editBtn) {
        console.error('Edit button not found');
        return;
    }
    
    // Get the modal
    const modal = document.getElementById('editHospitalModal');
    console.log('Modal found:', modal);
    if (!modal) {
        console.error('Edit modal not found');
        return;
    }
    
    // Get the close button
    const closeBtn = document.querySelector('#editHospitalModal .close');
    if (!closeBtn) {
        console.error('Close button not found');
        return;
    }
    
    // Get the cancel button
    const cancelBtn = document.getElementById('cancelEdit');
    if (!cancelBtn) {
        console.error('Cancel button not found');
        return;
    }
    
    // Add click event to edit button
    editBtn.addEventListener('click', function() {
        console.log('Edit button clicked');
        modal.style.display = 'block';
        console.log('Modal should be visible now');
    });
    
    // Add click event to close button
    closeBtn.addEventListener('click', function() {
        console.log('Close button clicked');
        modal.style.display = 'none';
    });
    
    // Add click event to cancel button
    cancelBtn.addEventListener('click', function() {
        console.log('Cancel button clicked');
        modal.style.display = 'none';
    });
    
    // Close modal when clicking outside of it
    window.addEventListener('click', function(event) {
        if (event.target === modal) {
            console.log('Clicked outside modal');
            modal.style.display = 'none';
        }
    });
    
    // Handle form submission
    const form = document.getElementById('editHospitalForm');
    if (form) {
        form.addEventListener('submit', function(event) {
            console.log('Form submission started');
            // Form will submit normally to the backend
        });
    }
    
    console.log('Edit button functionality initialized successfully');
}