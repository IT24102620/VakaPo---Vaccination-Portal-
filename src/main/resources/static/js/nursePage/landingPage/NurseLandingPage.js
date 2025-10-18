// Nurse Landing Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('Nurse Landing Page loaded');
    
    // Initialize nurse dashboard functionality
    initializeNurseDashboard();
});

function initializeNurseDashboard() {
    // Add click handlers for navigation buttons
    const profileBtn = document.querySelector('a[href="/nurse/profile"]');
    const patientHistoryBtn = document.querySelector('a[href="/nurse/patient-history"]');
    const feedbackBtn = document.querySelector('a[href="/nurse/feedback"]');
    
    if (profileBtn) {
        profileBtn.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = '/nurse/profile';
        });
    }
    
    if (patientHistoryBtn) {
        patientHistoryBtn.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = '/nurse/patient-history';
        });
    }
    
    if (feedbackBtn) {
        feedbackBtn.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = '/nurse/feedback';
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
    
    // Add any other nurse-specific functionality here
    console.log('Nurse dashboard initialized');
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
