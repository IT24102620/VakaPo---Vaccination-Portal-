function showDetails(institutionType, institutionName, regNumber, type, address, contact, email, username, approvedOn, certPath) {
    document.getElementById('detailInstitutionType').textContent = institutionType;
    document.getElementById('detailInstitutionName').textContent = institutionName;
    document.getElementById('detailRegNumber').textContent = regNumber;
    document.getElementById('detailType').textContent = type;
    document.getElementById('detailAddress').textContent = address;
    document.getElementById('detailContact').textContent = contact;
    document.getElementById('detailEmail').textContent = email;
    document.getElementById('detailUsername').textContent = username;
    document.getElementById('detailApprovedOn').textContent = approvedOn;
    document.getElementById('detailCert').href = certPath; // Placeholder; integrate backend for file access
    document.getElementById('detailsModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('detailsModal').style.display = 'none';
}

function manageInstitution(institutionName) {
    alert(`Manage actions for ${institutionName} (e.g., assign staff, monitor vaccinations). Placeholder for future functionality.`); // Replace with actual backend or navigation logic
}

// Close modal on outside click
window.onclick = function(event) {
    const modal = document.getElementById('detailsModal');
    if (event.target === modal) {
        closeModal();
    }
}

// Search filter function
function filterCards() {
    const input = document.getElementById('searchInput').value.toLowerCase();
    const cards = document.querySelectorAll('.institution-card');
    cards.forEach(card => {
        const name = card.querySelector('h3').textContent.toLowerCase();
        const regNumber = card.querySelector('p:nth-child(3)').textContent.toLowerCase(); // Registration Number
        const email = card.querySelector('p:nth-child(7)').textContent.toLowerCase(); // Email
        if (name.includes(input) || regNumber.includes(input) || email.includes(input)) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}