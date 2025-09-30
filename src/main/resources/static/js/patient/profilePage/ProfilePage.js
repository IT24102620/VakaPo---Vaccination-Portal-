// announcement

const cancelAnnouncement = document.querySelector(".announcement__cancel")
const announcement = document.querySelector(".announcement")

cancelAnnouncement.addEventListener("click", function () {
    announcement.style.display = "none"
})

// menu bar

const menuBar = document.querySelector(".navbar__section4")
const cancelMenubar = document.querySelector(".responsive__navbar__section1 p")
const responsiveNavbar = document.querySelector(".responsive__navbar")
const navbarLink = document.querySelectorAll(".responsive__navbar__section1 a")

menuBar.addEventListener("click", function () {
    responsiveNavbar.style.right = "0%"
})

cancelMenubar.addEventListener("click", function () {
    responsiveNavbar.style.right = "-80%"
})

navbarLink.forEach(function (link) {
    link.addEventListener("click", function () {
        responsiveNavbar.style.right = "-80%"
    })
})

//profile 

const profile = document.querySelector(".navbar__section3")
const cancelProfile = document.querySelector(".profile__close")
const profileBox = document.querySelector(".profile")

profile.addEventListener("click", function () {
    profileBox.style.right = "1%"
})

cancelProfile.addEventListener("click", function () {
    profileBox.style.right = "-20%"
})


// latest appointment history & vaccination history 

const appointment_button = document.querySelector(".appointment__button")
const vaccine_book_button = document.querySelector(".vaccination__book__button")

appointment_button.addEventListener("click", () => {
    window.location.href = "../Appointment Page/AppointmentPage.html";
});

vaccine_book_button.addEventListener("click", () => {
    window.location.href = "../Appointment Page/AppointmentPage.html";
});

// Get modal elements
const modal = document.getElementById("editModal");
const btn = document.querySelector(".personal__information__section2 button");
const span = document.querySelector(".close");

// Open modal
btn.onclick = function() {
    modal.style.display = "block";
}

// Close modal
span.onclick = function() {
    modal.style.display = "none";
}

// Close when clicking outside
window.onclick = function(event) {
    if (event.target === modal) {
        modal.style.display = "none";
    }
}
