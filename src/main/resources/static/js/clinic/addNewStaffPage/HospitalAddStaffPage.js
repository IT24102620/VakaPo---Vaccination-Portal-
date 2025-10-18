// announcement

const cancelAnnouncement = document.querySelector(".announcement__cancel")
const announcement = document.querySelector(".announcement")

cancelAnnouncement.addEventListener("click" , function(){
    announcement.style.display = "none"
})

// menu bar

const menuBar = document.querySelector(".navbar__section4")
const cancelMenubar = document.querySelector(".responsive__navbar__section1 p")
const responsiveNavbar = document.querySelector(".responsive__navbar")
const navbarLink = document.querySelectorAll(".responsive__navbar__section1 a")

menuBar.addEventListener("click" , function(){
    responsiveNavbar.style.right = "0%"
})

cancelMenubar.addEventListener("click" , function(){
    responsiveNavbar.style.right = "-80%"
})

navbarLink.forEach(function(link){
    link.addEventListener("click" , function(){
        responsiveNavbar.style.right = "-80%"
    })
})

//profile 

const profile = document.querySelector(".navbar__section3")
const cancelProfile = document.querySelector(".profile__close")
const profileBox = document.querySelector(".profile")

profile.addEventListener("click" , function(){
    profileBox.style.right = "1%"
})

cancelProfile.addEventListener("click" , function(){
    profileBox.style.right = "-20%"
})

// Staff Invitation Form Handling
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('staffInvitationForm');
    const successMessage = document.getElementById('successMessage');
    const errorMessage = document.getElementById('errorMessage');
    const successText = document.getElementById('successText');
    const errorText = document.getElementById('errorText');

    // Validation patterns
    const patterns = {
        name: /^[a-zA-Z\s]{2,100}$/,
        email: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
        phone: /^0[0-9]{9}$/
    };

    // Show error message
    function showError(fieldId, message) {
        const errorDiv = document.getElementById(fieldId + '-error');
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
        }
    }

    // Clear error message
    function clearError(fieldId) {
        const errorDiv = document.getElementById(fieldId + '-error');
        if (errorDiv) {
            errorDiv.textContent = '';
            errorDiv.style.display = 'none';
        }
    }

    // Check if email already exists with different role
    function checkEmailRoleConflict(email) {
        if (!email || !patterns.email.test(email)) {
            return; // Skip if email is invalid
        }

        const role = document.getElementById('role').value;
        
        // Check if email exists in the system
        fetch(`/auth/check-email?email=${encodeURIComponent(email)}`)
        .then(response => response.json())
        .then(data => {
            if (data.exists) {
                // Email exists, check if it's a doctor trying to be invited to another clinic
                if (role === 'Doctor') {
                    // For doctors, show info message instead of error
                    showError('email', 'This doctor is already registered. An invitation will be sent to join this clinic.');
                } else if (role === 'Nurse') {
                    // For nurses, show error as they can only work in one clinic
                    showError('email', 'This nurse is already registered in another clinic. Nurses can only work in one clinic.');
                } else {
                    // For other roles or no role selected
                    showError('email', 'This email address is already registered in the system. Please use a different email address or contact the administrator.');
                }
            } else {
                clearError('email');
            }
        })
        .catch(error => {
            console.log('Email check failed:', error);
            // Don't show error for network issues, just log
        });
    }

    // Show success message
    function showSuccess(message) {
        successText.textContent = message;
        successMessage.style.display = 'block';
        errorMessage.style.display = 'none';
    }

    // Show error message
    function showErrorMsg(message) {
        errorText.textContent = message;
        errorMessage.style.display = 'block';
        successMessage.style.display = 'none';
    }

    // Hide messages
    function hideMessages() {
        successMessage.style.display = 'none';
        errorMessage.style.display = 'none';
    }

    // Validate field
    function validateField(fieldId, value, pattern, required = true) {
        if (required && (!value || value.trim() === '')) {
            showError(fieldId, 'This field is required');
            return false;
        }
        
        if (value && !pattern.test(value)) {
            showError(fieldId, 'Invalid format');
            return false;
        }
        
        clearError(fieldId);
        return true;
    }

    // Add event listeners for real-time validation
    document.getElementById('name').addEventListener('blur', function() {
        validateField('name', this.value, patterns.name, true);
    });

    document.getElementById('email').addEventListener('blur', function() {
        validateField('email', this.value, patterns.email, true);
        // Check if email already exists as different role
        checkEmailRoleConflict(this.value);
    });

    document.getElementById('phone').addEventListener('blur', function() {
        if (this.value) {
            validateField('phone', this.value, patterns.phone, false);
        } else {
            clearError('phone');
        }
    });

    document.getElementById('role').addEventListener('change', function() {
        if (this.value === '') {
            showError('role', 'Please select a role');
        } else {
            clearError('role');
            // Re-check email if role changed
            const email = document.getElementById('email').value;
            if (email && patterns.email.test(email)) {
                checkEmailRoleConflict(email);
            }
        }
    });

    // Form submission
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        hideMessages();

        // Validate all fields
        const name = document.getElementById('name').value;
        const email = document.getElementById('email').value;
        const phone = document.getElementById('phone').value;
        const role = document.getElementById('role').value;
        const qualification = document.getElementById('qualification').value;
        const specialization = document.getElementById('specialization').value;

        let isValid = true;

        // Validate required fields
        isValid &= validateField('name', name, patterns.name, true);
        isValid &= validateField('email', email, patterns.email, true);
        isValid &= validateField('role', role, /^.+$/, true);

        // Validate optional fields if provided
        if (phone) {
            isValid &= validateField('phone', phone, patterns.phone, false);
        }

        // Check if there are any blocking errors (not just info messages for doctors)
        const emailError = document.getElementById('email-error');
        const hasBlockingError = emailError && emailError.textContent && 
            !emailError.textContent.includes('This doctor is already registered. An invitation will be sent');
        
        if (!isValid || hasBlockingError) {
            showErrorMsg('Please fix all validation errors before submitting.');
            return;
        }

        // Prepare data for API call
        const formData = {
            name: name,
            email: email,
            contact: phone || '',
            role: role,
            qualifications: qualification || '',
            specialization: specialization || ''
        };

        // Send invitation
        fetch('/clinic/send-invitation', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showSuccess(data.message);
                form.reset();
            } else {
                showErrorMsg(data.error || 'Failed to send invitation');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showErrorMsg('An unexpected error occurred. Please try again.');
        });
    });
});