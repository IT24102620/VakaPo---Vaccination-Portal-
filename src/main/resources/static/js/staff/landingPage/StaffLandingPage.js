// Staff Landing Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('Staff Landing Page loaded');
    
    // Initialize password change functionality
    initializePasswordChange();
    
    // Check if user needs to change password
    checkPasswordStatus();
});

function initializePasswordChange() {
    const passwordModal = document.getElementById('passwordChangeModal');
    const changePasswordBtn = document.getElementById('changePasswordBtn');
    const closeModal = document.getElementById('closeModal');
    const cancelBtn = document.getElementById('cancelPasswordChange');
    const passwordForm = document.getElementById('passwordChangeForm');
    const passwordAlert = document.getElementById('passwordAlert');
    const mainAlert = document.getElementById('mainAlert');

    if (!passwordModal || !changePasswordBtn) {
        console.warn('Password change elements not found');
        return;
    }

    // Show password change modal
    changePasswordBtn.addEventListener('click', function() {
        passwordModal.style.display = 'block';
    });

    // Close modal
    closeModal.addEventListener('click', function() {
        passwordModal.style.display = 'none';
    });

    cancelBtn.addEventListener('click', function() {
        passwordModal.style.display = 'none';
    });

    // Close modal when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target === passwordModal) {
            passwordModal.style.display = 'none';
        }
    });

    // Password change form submission
    passwordForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // Validate passwords match
        if (newPassword !== confirmPassword) {
            showPasswordAlert('New passwords do not match!', 'danger');
            return;
        }

        // Validate password length
        if (newPassword.length < 8) {
            showPasswordAlert('New password must be at least 8 characters long!', 'danger');
            return;
        }

        // Send password change request
        fetch('/staff/change-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                currentPassword: currentPassword,
                newPassword: newPassword
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showPasswordAlert('Password changed successfully!', 'success');
                setTimeout(() => {
                    passwordModal.style.display = 'none';
                    location.reload();
                }, 2000);
            } else {
                showPasswordAlert(data.error || 'Failed to change password', 'danger');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showPasswordAlert('An error occurred. Please try again.', 'danger');
        });
    });

    function showPasswordAlert(message, type) {
        if (passwordAlert) {
            passwordAlert.textContent = message;
            passwordAlert.className = 'alert alert-' + type;
            passwordAlert.style.display = 'block';
        }
    }
}

function checkPasswordStatus() {
    fetch('/staff/password-status')
    .then(response => response.json())
    .then(data => {
        if (data.success && data.needsPasswordChange) {
            const mainAlert = document.getElementById('mainAlert');
            if (mainAlert) {
                mainAlert.style.display = 'block';
                // Auto-show password change modal for first-time users
                setTimeout(() => {
                    const passwordModal = document.getElementById('passwordChangeModal');
                    if (passwordModal) {
                        passwordModal.style.display = 'block';
                    }
                }, 1000);
            }
        }
    })
    .catch(error => {
        console.error('Error checking password status:', error);
    });
}
