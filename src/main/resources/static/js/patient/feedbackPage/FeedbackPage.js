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

// My Reviews functionality
document.addEventListener('DOMContentLoaded', function() {
    loadMyReviews();
    setupFeedbackForm();
});

// Setup feedback form validation and submission
function setupFeedbackForm() {
    const form = document.getElementById('feedbackForm');
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Validate form
            if (validateFeedbackForm()) {
                // Show loading state
                const submitBtn = form.querySelector('input[type="submit"]');
                const originalText = submitBtn.value;
                submitBtn.value = 'Submitting...';
                submitBtn.disabled = true;
                
                // Submit form
                form.submit();
            }
        });
    }
}

// Validate feedback form
function validateFeedbackForm() {
    const name = document.getElementById('name').value.trim();
    const email = document.getElementById('email').value.trim();
    const rating = document.getElementById('rating').value;
    const message = document.getElementById('message').value.trim();
    
    // Clear previous error messages
    clearFormErrors();
    
    let isValid = true;
    
    if (!name) {
        showFieldError('name', 'Name is required');
        isValid = false;
    }
    
    if (!email) {
        showFieldError('email', 'Email is required');
        isValid = false;
    } else if (!isValidEmail(email)) {
        showFieldError('email', 'Please enter a valid email address');
        isValid = false;
    }
    
    if (!rating) {
        showFieldError('rating', 'Please select a rating');
        isValid = false;
    }
    
    if (!message) {
        showFieldError('message', 'Message is required');
        isValid = false;
    } else if (message.length < 10) {
        showFieldError('message', 'Message must be at least 10 characters long');
        isValid = false;
    }
    
    return isValid;
}

// Show field error
function showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    if (field) {
        field.style.borderColor = '#dc3545';
        
        // Remove existing error message
        const existingError = field.parentNode.querySelector('.field-error');
        if (existingError) {
            existingError.remove();
        }
        
        // Add error message
        const errorDiv = document.createElement('div');
        errorDiv.className = 'field-error';
        errorDiv.style.color = '#dc3545';
        errorDiv.style.fontSize = '12px';
        errorDiv.style.marginTop = '5px';
        errorDiv.textContent = message;
        
        field.parentNode.appendChild(errorDiv);
    }
}

// Clear form errors
function clearFormErrors() {
    const errorMessages = document.querySelectorAll('.field-error');
    errorMessages.forEach(error => error.remove());
    
    const fields = document.querySelectorAll('#feedbackForm input, #feedbackForm select, #feedbackForm textarea');
    fields.forEach(field => {
        field.style.borderColor = '';
    });
}

// Validate email format
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Load user's reviews
async function loadMyReviews() {
    try {
        console.log('Loading patient reviews...');
        const response = await fetch('/patient/api/my-reviews');
        console.log('Response status:', response.status);
        
        if (!response.ok) {
            console.error('HTTP error:', response.status, response.statusText);
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        console.log('API Response:', result);
        console.log('Response success:', result.success);
        console.log('Data length:', result.data ? result.data.length : 'no data');
        
        const reviewsList = document.getElementById('myReviewsList');
        
        if (result.success && result.data.length > 0) {
            console.log('Found', result.data.length, 'reviews');
            reviewsList.innerHTML = result.data.map(review => `
                <div class="review-item">
                    <div class="review-header">
                        <div class="review-rating">
                            ${generateStars(review.rating)}
                        </div>
                        <div class="review-status">
                            <span class="status-badge ${review.isApproved ? 'approved' : 'pending'}">
                                ${review.isApproved ? 'Approved' : 'Pending'}
                            </span>
                        </div>
                    </div>
                    <div class="review-content">
                        <p class="review-message">${escapeHtml(review.message)}</p>
                        <p class="review-date">Submitted: ${formatDate(review.createdAt)}</p>
                    </div>
                    <div class="review-actions">
                        <button class="btn btn-edit" onclick="editReview(${review.id})">Edit</button>
                        <button class="btn btn-delete" onclick="deleteReview(${review.id})">Delete</button>
                    </div>
                </div>
            `).join('');
        } else {
            console.log('No reviews found or API returned success=false');
            reviewsList.innerHTML = '<div class="no-reviews">No reviews submitted yet.</div>';
        }
    } catch (error) {
        console.error('Error loading reviews:', error);
        document.getElementById('myReviewsList').innerHTML = '<div class="error">Failed to load reviews. Please try again.</div>';
    }
}

// Generate star rating display
function generateStars(rating) {
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            stars += '⭐';
        } else {
            stars += '☆';
        }
    }
    return stars;
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Edit review
async function editReview(feedbackId) {
    try {
        // Get review details (we'll need to fetch this from the API)
        const response = await fetch(`/patient/api/my-reviews`);
        const result = await response.json();
        
        if (result.success) {
            const review = result.data.find(r => r.id === feedbackId);
            if (review) {
                // Populate edit form
                document.getElementById('editFeedbackId').value = review.id;
                document.getElementById('editName').value = review.name || '';
                document.getElementById('editEmail').value = review.email || '';
                document.getElementById('editContactNo').value = review.contactNo || '';
                document.getElementById('editRating').value = review.rating || 1;
                document.getElementById('editMessage').value = review.message || '';
                
                // Show modal
                document.getElementById('editReviewModal').style.display = 'flex';
            }
        }
    } catch (error) {
        console.error('Error loading review for edit:', error);
        alert('Failed to load review details');
    }
}

// Close edit modal
function closeEditModal() {
    document.getElementById('editReviewModal').style.display = 'none';
}

// Delete review
async function deleteReview(feedbackId) {
    if (confirm('Are you sure you want to delete this review? This action cannot be undone.')) {
        try {
            const formData = new FormData();
            formData.append('feedbackId', feedbackId);
            
            // CSRF is disabled in Spring Security config, so no token needed
            
            const response = await fetch('/patient/api/delete-review', {
                method: 'POST',
                body: formData
            });
            
            const result = await response.json();
            
            if (result.success) {
                alert('Review deleted successfully!');
                loadMyReviews(); // Reload the list
            } else {
                alert('Failed to delete review: ' + result.message);
            }
        } catch (error) {
            console.error('Error deleting review:', error);
            alert('Failed to delete review. Please try again.');
        }
    }
}

// Handle edit form submission
document.getElementById('editReviewForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    
    // CSRF is disabled in Spring Security config
    
    try {
        const response = await fetch('/patient/api/update-review', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            alert('Review updated successfully! It will be reviewed again by admin.');
            closeEditModal();
            loadMyReviews(); // Reload the list
        } else {
            alert('Failed to update review: ' + result.message);
        }
    } catch (error) {
        console.error('Error updating review:', error);
        alert('Failed to update review. Please try again.');
    }
});