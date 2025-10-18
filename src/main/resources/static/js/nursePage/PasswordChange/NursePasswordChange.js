// Nurse Password Change JavaScript
document.addEventListener('DOMContentLoaded', function() {
    const passwordForm = document.getElementById('passwordChangeForm');
    const alertContainer = document.getElementById('alertContainer');

    // Password change form submission
    passwordForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // Additional validation - check if fields are empty
        if (!newPassword || !confirmPassword) {
            showAlert('Please fill in all password fields!', 'danger');
            return;
        }

        // Validate passwords match
        if (newPassword !== confirmPassword) {
            showAlert('New passwords do not match!', 'danger');
            return;
        }

        // Debug logging
        console.log('Current Password:', currentPassword);
        console.log('New Password:', newPassword);
        console.log('Confirm Password:', confirmPassword);
        console.log('Passwords Match:', newPassword === confirmPassword);

        // Validate password length
        if (newPassword.length < 8) {
            showAlert('New password must be at least 8 characters long!', 'danger');
            return;
        }

        // Validate password strength
        if (!isStrongPassword(newPassword)) {
            showAlert('Password must contain at least one letter and one number!', 'danger');
            return;
        }

        // Send password change request
        fetch('/nurse/change-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                currentPassword: currentPassword,
                newPassword: newPassword,
                confirmPassword: confirmPassword
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert('Password changed successfully! Redirecting to dashboard...', 'success');
                setTimeout(() => {
                    window.location.href = '/nurse/landing';
                }, 2000);
            } else {
                showAlert(data.error || 'Failed to change password', 'danger');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('An error occurred. Please try again.', 'danger');
        });
    });

    // Password strength validation
    function isStrongPassword(password) {
        const hasLetter = /[a-zA-Z]/.test(password);
        const hasNumber = /\d/.test(password);
        return hasLetter && hasNumber;
    }

    // Show alert function
    function showAlert(message, type) {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        alertContainer.appendChild(alertDiv);
        
        // Auto-remove after 5 seconds
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.parentNode.removeChild(alertDiv);
            }
        }, 5000);
    }

    // Real-time password validation
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    newPasswordInput.addEventListener('input', function() {
        const password = this.value;
        const isValid = password.length >= 8 && isStrongPassword(password);
        
        if (password.length > 0) {
            if (isValid) {
                this.classList.remove('is-invalid');
                this.classList.add('is-valid');
            } else {
                this.classList.remove('is-valid');
                this.classList.add('is-invalid');
            }
        } else {
            this.classList.remove('is-valid', 'is-invalid');
        }
    });

    confirmPasswordInput.addEventListener('input', function() {
        const password = this.value;
        const newPassword = newPasswordInput.value;
        
        if (password.length > 0) {
            if (password === newPassword) {
                this.classList.remove('is-invalid');
                this.classList.add('is-valid');
                // Clear any existing error message
                const existingError = this.parentNode.querySelector('.password-error');
                if (existingError) {
                    existingError.remove();
                }
            } else {
                this.classList.remove('is-valid');
                this.classList.add('is-invalid');
                // Show error message
                const existingError = this.parentNode.querySelector('.password-error');
                if (!existingError) {
                    const errorDiv = document.createElement('div');
                    errorDiv.className = 'password-error';
                    errorDiv.style.color = '#dc3545';
                    errorDiv.style.fontSize = '14px';
                    errorDiv.style.marginTop = '5px';
                    errorDiv.textContent = 'Passwords do not match';
                    this.parentNode.appendChild(errorDiv);
                }
            }
        } else {
            this.classList.remove('is-valid', 'is-invalid');
            const existingError = this.parentNode.querySelector('.password-error');
            if (existingError) {
                existingError.remove();
            }
        }
    });
});
