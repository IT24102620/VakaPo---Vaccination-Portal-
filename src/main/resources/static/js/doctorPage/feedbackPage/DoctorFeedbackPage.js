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
});

// Load user's reviews
async function loadMyReviews() {
    try {
        const response = await fetch('/doctor/api/my-reviews');
        const result = await response.json();
        
        const reviewsList = document.getElementById('myReviewsList');
        
        if (result.success && result.data.length > 0) {
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
        const response = await fetch(`/doctor/api/my-reviews`);
        const result = await response.json();
        
        if (result.success) {
            const review = result.data.find(r => r.id === feedbackId);
            if (review) {
                // Populate edit form
                document.getElementById('editFeedbackId').value = review.id;
                document.getElementById('editName').value = review.name || '';
                document.getElementById('editEmail').value = review.email || '';
                document.getElementById('editContactno').value = review.contactNo || '';
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
            
            const response = await fetch('/doctor/api/delete-review', {
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
    
    try {
        const response = await fetch('/doctor/api/update-review', {
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