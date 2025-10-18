/* Allowed Sub-Admins Page Logic */
document.addEventListener("DOMContentLoaded", function () {
    console.log("Allowed_Sub_Admin.js loaded ✅");
    setupFilter();
    loadAllowedSubAdmins(); // Optional live reload from API
});

async function loadAllowedSubAdmins() {
    try {
        const response = await axios.get("/admin/api/subadmins/allowed");
        const approvedList = document.getElementById("approvedList");
        const rejectedList = document.getElementById("rejectedList");

        approvedList.innerHTML = "";
        rejectedList.innerHTML = "";

        response.data.approvedUsers.forEach(user => {
            approvedList.appendChild(createCard(user, "approved"));
        });

        response.data.rejectedUsers.forEach(user => {
            rejectedList.appendChild(createCard(user, "rejected"));
        });

    } catch (error) {
        console.error("Error loading allowed sub-admins:", error);
    }
}

function createCard(user, type) {
    const card = document.createElement("div");
    card.className = `institution-card ${type}`;
    card.innerHTML = `
        <h3>${user.institution}</h3>
        <p>Username: ${user.username}</p>
        <p>Registration: ${user.rnumber}</p>
        <p>Email: ${user.email}</p>
        <p>Contact: ${user.contact}</p>
        <p>Address: ${user.address}</p>
        <p>Role: ${user.role}</p>
        <span class="status-${type}">${type.charAt(0).toUpperCase() + type.slice(1)}</span>
    `;
    return card;
}

function setupFilter() {
    const input = document.getElementById("searchInput");
    input.addEventListener("input", filterInstitutions);
}

function filterInstitutions() {
    const input = document.getElementById("searchInput").value.toLowerCase();
    const cards = document.querySelectorAll(".institution-card");

    cards.forEach(card => {
        const name = card.querySelector("h3").textContent.toLowerCase();
        card.style.display = name.includes(input) ? "" : "none";
    });
}
