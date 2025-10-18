// Doctor Landing Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('Doctor Landing Page loaded');
    
    // Initialize doctor dashboard functionality
    initializeDoctorDashboard();
});

function initializeDoctorDashboard() {
    // Add click handlers for navigation buttons
    const profileBtn = document.querySelector('a[href="/doctor/profile"]');
    const appointmentsBtn = document.querySelector('a[href="/doctor/appointments"]');
    const patientHistoryBtn = document.querySelector('a[href="/doctor/patient-history"]');
    const feedbackBtn = document.querySelector('a[href="/doctor/feedback"]');
    
    if (profileBtn) {
        profileBtn.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = '/doctor/profile';
        });
    }
    
    if (appointmentsBtn) {
        appointmentsBtn.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = '/doctor/appointments';
        });
    }
    
    if (patientHistoryBtn) {
        patientHistoryBtn.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = '/doctor/patient-history';
        });
    }
    
    if (feedbackBtn) {
        feedbackBtn.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = '/doctor/feedback';
        });
    }
    
    // Add logout functionality
    const logoutBtn = document.querySelector('form[action="/logout"] button');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            if (!confirm('Are you sure you want to logout?')) {
                e.preventDefault();
            }
        });
    }
    
    // Add any other doctor-specific functionality here
    console.log('Doctor dashboard initialized');
}


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