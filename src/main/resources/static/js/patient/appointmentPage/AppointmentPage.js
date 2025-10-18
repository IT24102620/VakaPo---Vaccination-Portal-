// Sequential Appointment Booking JavaScript
// This script handles the sequential form filling and AJAX calls

document.addEventListener('DOMContentLoaded', function() {
    console.log('Appointment page loaded');
    
    // Initialize form state
    resetForm();
    
    // Add event listeners
    setupEventListeners();
});

function setupEventListeners() {
    // Vaccine selection
    const vaccineSelect = document.getElementById('vaccine');
    if (vaccineSelect) {
        vaccineSelect.addEventListener('change', loadInstitutions);
    }
    
    // Institution selection
    const institutionSelect = document.getElementById('institution');
    if (institutionSelect) {
        institutionSelect.addEventListener('change', loadDays);
    }
    
    // Day selection
    const daySelect = document.getElementById('day');
    if (daySelect) {
        daySelect.addEventListener('change', function() {
            loadDateAndTimeSlots();
            updateDateInputForSelectedDay();
        });
    }
    
    // Date selection
    const dateInput = document.getElementById('date');
    if (dateInput) {
        dateInput.addEventListener('change', loadTimeSlots);
    }
    
    // Time slot selection
    const timeSlotSelect = document.getElementById('timeSlot');
    if (timeSlotSelect) {
        timeSlotSelect.addEventListener('change', enableBookButton);
    }
}

function resetForm() {
    // Disable all dependent fields
    document.getElementById('institution').disabled = true;
    document.getElementById('day').disabled = true;
    document.getElementById('date').disabled = true;
    document.getElementById('timeSlot').disabled = true;
    document.getElementById('bookButton').disabled = true;
    
    // Clear dependent fields
    document.getElementById('institution').innerHTML = '<option value="">-- Select Hospital/Clinic --</option>';
    document.getElementById('day').innerHTML = '<option value="">-- Select Day --</option>';
    document.getElementById('timeSlot').innerHTML = '<option value="">-- Select Time Slot --</option>';
    document.getElementById('date').value = '';
    document.getElementById('institutionType').value = '';
}

function loadInstitutions() {
    const vaccineSelect = document.getElementById('vaccine');
    const institutionSelect = document.getElementById('institution');
    const institutionTypeInput = document.getElementById('institutionType');
    
    if (!vaccineSelect.value) {
        resetForm();
        return;
    }
    
    console.log('Loading institutions for vaccine:', vaccineSelect.value);
    
    // Show loading state
    institutionSelect.innerHTML = '<option value="">Loading...</option>';
    institutionSelect.disabled = true;
    
    // Make AJAX call
    fetch(`/patient/api/institutions?vaccineName=${encodeURIComponent(vaccineSelect.value)}`)
        .then(response => response.json())
        .then(data => {
            console.log('Institutions loaded:', data);
            
            // Clear and populate institution dropdown
            institutionSelect.innerHTML = '<option value="">-- Select Hospital/Clinic --</option>';
            
            data.forEach(institution => {
                const option = document.createElement('option');
                option.value = institution.id;
                option.textContent = `${institution.name} (${institution.type})`;
                option.dataset.type = institution.type;
                institutionSelect.appendChild(option);
            });
            
            // Enable institution selection
            institutionSelect.disabled = false;
            
            // Reset dependent fields
            document.getElementById('day').disabled = true;
            document.getElementById('date').disabled = true;
            document.getElementById('timeSlot').disabled = true;
            document.getElementById('bookButton').disabled = true;
            document.getElementById('day').innerHTML = '<option value="">-- Select Day --</option>';
            document.getElementById('timeSlot').innerHTML = '<option value="">-- Select Time Slot --</option>';
            document.getElementById('date').value = '';
            institutionTypeInput.value = '';
        })
        .catch(error => {
            console.error('Error loading institutions:', error);
            institutionSelect.innerHTML = '<option value="">Error loading institutions</option>';
            showError('Failed to load institutions. Please try again.');
        });
}

function loadDays() {
    const vaccineSelect = document.getElementById('vaccine');
    const institutionSelect = document.getElementById('institution');
    const daySelect = document.getElementById('day');
    const institutionTypeInput = document.getElementById('institutionType');
    
    if (!vaccineSelect.value || !institutionSelect.value) {
        return;
    }
    
    // Get institution type from selected option
    const selectedOption = institutionSelect.options[institutionSelect.selectedIndex];
    const institutionType = selectedOption.dataset.type;
    institutionTypeInput.value = institutionType;
    
    console.log('Loading days for vaccine:', vaccineSelect.value, 'institution:', institutionSelect.value, 'type:', institutionType);
    
    // Show loading state
    daySelect.innerHTML = '<option value="">Loading...</option>';
    daySelect.disabled = true;
    
    // Make AJAX call
    fetch(`/patient/api/days?vaccineName=${encodeURIComponent(vaccineSelect.value)}&institutionId=${encodeURIComponent(institutionSelect.value)}&institutionType=${encodeURIComponent(institutionType)}`)
        .then(response => response.json())
        .then(data => {
            console.log('Days loaded:', data);
            
            // Clear and populate day dropdown
            daySelect.innerHTML = '<option value="">-- Select Day --</option>';
            
            data.forEach(day => {
                const option = document.createElement('option');
                option.value = day;
                option.textContent = day;
                daySelect.appendChild(option);
            });
            
            // Enable day selection
            daySelect.disabled = false;
            
            // Show helpful message about date selection
            showDaySelectionHelp(data);
            
            // Reset dependent fields
            document.getElementById('date').disabled = true;
            document.getElementById('timeSlot').disabled = true;
            document.getElementById('bookButton').disabled = true;
            document.getElementById('timeSlot').innerHTML = '<option value="">-- Select Time Slot --</option>';
            document.getElementById('date').value = '';
        })
        .catch(error => {
            console.error('Error loading days:', error);
            daySelect.innerHTML = '<option value="">Error loading days</option>';
            showError('Failed to load available days. Please try again.');
        });
}

function loadDateAndTimeSlots() {
    const daySelect = document.getElementById('day');
    const dateInput = document.getElementById('date');
    
    if (!daySelect.value) {
        return;
    }
    
    console.log('Day selected:', daySelect.value);
    
    // Enable date input
    dateInput.disabled = false;
    
    // Set minimum date to today
    const today = new Date().toISOString().split('T')[0];
    dateInput.min = today;
    
    // Add event listener to restrict date selection to the selected day of week
    dateInput.addEventListener('change', function() {
        validateSelectedDate();
    });
    
    // Reset dependent fields
    document.getElementById('timeSlot').disabled = true;
    document.getElementById('bookButton').disabled = true;
    document.getElementById('timeSlot').innerHTML = '<option value="">-- Select Time Slot --</option>';
}

function loadTimeSlots() {
    const vaccineSelect = document.getElementById('vaccine');
    const institutionSelect = document.getElementById('institution');
    const institutionTypeInput = document.getElementById('institutionType');
    const dateInput = document.getElementById('date');
    const timeSlotSelect = document.getElementById('timeSlot');
    
    if (!vaccineSelect.value || !institutionSelect.value || !dateInput.value) {
        return;
    }
    
    console.log('Loading time slots for date:', dateInput.value);
    
    // Show loading state
    timeSlotSelect.innerHTML = '<option value="">Loading...</option>';
    timeSlotSelect.disabled = true;
    
    // Make AJAX call
    const apiUrl = `/patient/api/time-slots?vaccineName=${encodeURIComponent(vaccineSelect.value)}&institutionId=${encodeURIComponent(institutionSelect.value)}&institutionType=${encodeURIComponent(institutionTypeInput.value)}&appointmentDate=${encodeURIComponent(dateInput.value)}`;
    console.log('Making API call to:', apiUrl);
    
    fetch(apiUrl)
        .then(response => {
            console.log('API response status:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Time slots loaded:', data);
            console.log('Number of time slots:', data.length);
            
            // Clear and populate time slot dropdown
            timeSlotSelect.innerHTML = '<option value="">-- Select Time Slot --</option>';
            
            if (data.length === 0) {
                timeSlotSelect.innerHTML = '<option value="">No available time slots</option>';
                showError('No time slots available for the selected date. This could be because:\n' +
                         '1. The selected day is not available for this vaccine at this institution\n' +
                         '2. All time slots are already booked\n' +
                         '3. No vaccination schedule exists for this combination\n\n' +
                         'Please try a different date or check the debug endpoint: /patient/debug/schedules');
            } else {
                data.forEach(timeSlot => {
                    const option = document.createElement('option');
                    option.value = timeSlot;
                    option.textContent = timeSlot;
                    timeSlotSelect.appendChild(option);
                });
                
                // Enable time slot selection
                timeSlotSelect.disabled = false;
                console.log('Time slots populated successfully');
            }
        })
        .catch(error => {
            console.error('Error loading time slots:', error);
            timeSlotSelect.innerHTML = '<option value="">Error loading time slots</option>';
            showError('Failed to load time slots. Please check the console for details and try the debug endpoint: /patient/debug/schedules');
        });
}

function enableBookButton() {
    const timeSlotSelect = document.getElementById('timeSlot');
    const bookButton = document.getElementById('bookButton');
    
    if (timeSlotSelect.value) {
        bookButton.disabled = false;
        bookButton.textContent = 'Book Appointment';
    } else {
        bookButton.disabled = true;
    }
}

function showError(message) {
    // Create or update error message display
    let errorDiv = document.getElementById('error-message');
    if (!errorDiv) {
        errorDiv = document.createElement('div');
        errorDiv.id = 'error-message';
        errorDiv.style.cssText = 'background-color: #f8d7da; color: #721c24; padding: 10px; margin: 10px 0; border-radius: 5px;';
        
        const form = document.getElementById('appointmentForm');
        form.parentNode.insertBefore(errorDiv, form);
    }
    
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    
    // Hide error after 5 seconds
    setTimeout(() => {
        errorDiv.style.display = 'none';
    }, 5000);
}

function showSuccess(message) {
    // Create or update success message display
    let successDiv = document.getElementById('success-message');
    if (!successDiv) {
        successDiv = document.createElement('div');
        successDiv.id = 'success-message';
        successDiv.style.cssText = 'background-color: #d4edda; color: #155724; padding: 10px; margin: 10px 0; border-radius: 5px;';
        
        const form = document.getElementById('appointmentForm');
        form.parentNode.insertBefore(successDiv, form);
    }
    
    successDiv.textContent = message;
    successDiv.style.display = 'block';
    
    // Hide success after 5 seconds
    setTimeout(() => {
        successDiv.style.display = 'none';
    }, 5000);
}

function validateSelectedDate() {
    const daySelect = document.getElementById('day');
    const dateInput = document.getElementById('date');
    
    if (!daySelect.value || !dateInput.value) {
        return;
    }
    
    const selectedDate = new Date(dateInput.value);
    const selectedDayName = daySelect.value;
    
    // Get day of week for the selected date (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
    const dayOfWeek = selectedDate.getDay();
    
    // Map day numbers to day names
    const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const actualDayName = dayNames[dayOfWeek];
    
    console.log('Selected date:', dateInput.value, 'Expected day:', selectedDayName, 'Actual day:', actualDayName);
    
    if (actualDayName !== selectedDayName) {
        // Clear the date input and show error
        dateInput.value = '';
        showError(`Please select a ${selectedDayName}. The selected date is a ${actualDayName}.`);
        return false;
    }
    
    return true;
}

function getNextValidDate(dayName) {
    const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const targetDayIndex = dayNames.indexOf(dayName);
    
    if (targetDayIndex === -1) {
        return null;
    }
    
    const today = new Date();
    const currentDayIndex = today.getDay();
    
    // Calculate days until the next occurrence of the target day
    let daysUntilTarget = (targetDayIndex - currentDayIndex + 7) % 7;
    
    // If today is the target day, move to next week
    if (daysUntilTarget === 0) {
        daysUntilTarget = 7;
    }
    
    const nextValidDate = new Date(today);
    nextValidDate.setDate(today.getDate() + daysUntilTarget);
    
    return nextValidDate.toISOString().split('T')[0];
}

function showDaySelectionHelp(availableDays) {
    // Remove any existing help message
    const existingHelp = document.getElementById('day-selection-help');
    if (existingHelp) {
        existingHelp.remove();
    }
    
    if (availableDays.length === 0) {
        return;
    }
    
    // Create help message
    const helpDiv = document.createElement('div');
    helpDiv.id = 'day-selection-help';
    helpDiv.style.cssText = 'background-color: #e7f3ff; color: #0066cc; padding: 10px; margin: 10px 0; border-radius: 5px; border-left: 4px solid #0066cc;';
    
    const dayList = availableDays.join(', ');
    helpDiv.innerHTML = `
        <strong>Available Days:</strong> ${dayList}<br>
        <small>After selecting a day, you can only pick dates that fall on that day of the week.</small>
    `;
    
    // Insert after the day selection
    const daySelect = document.getElementById('day');
    daySelect.parentNode.insertBefore(helpDiv, daySelect.nextSibling);
}

function updateDateInputForSelectedDay() {
    const daySelect = document.getElementById('day');
    const dateInput = document.getElementById('date');
    
    if (!daySelect.value) {
        return;
    }
    
    // Get the next valid date for the selected day
    const nextValidDate = getNextValidDate(daySelect.value);
    
    if (nextValidDate) {
        // Set the date input to the next valid date
        dateInput.value = nextValidDate;
        
        // Show a helpful message
        const selectedDate = new Date(nextValidDate);
        const dateString = selectedDate.toLocaleDateString('en-US', { 
            weekday: 'long', 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
        });
        
        showSuccess(`Next available ${daySelect.value} is ${dateString}. You can change this date if needed.`);
        
        // Automatically load time slots for this date
        setTimeout(() => {
            loadTimeSlots();
        }, 1000);
    }
}

// Form validation
document.getElementById('appointmentForm').addEventListener('submit', function(e) {
    const vaccine = document.getElementById('vaccine').value;
    const institution = document.getElementById('institution').value;
    const day = document.getElementById('day').value;
    const date = document.getElementById('date').value;
    const timeSlot = document.getElementById('timeSlot').value;
    
    if (!vaccine || !institution || !day || !date || !timeSlot) {
        e.preventDefault();
        showError('Please fill in all required fields.');
        return false;
    }
    
    // Additional validation
    const selectedDate = new Date(date);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (selectedDate < today) {
        e.preventDefault();
        showError('Please select a future date.');
        return false;
    }
    
    return true;
});