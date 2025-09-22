function showDetails(institutionType, institutionName, regNumber, type, address, contact, email, username, received, certPath) {
    document.getElementById('detailInstitutionType').textContent = institutionType;
    document.getElementById('detailInstitutionName').textContent = institutionName;
    document.getElementById('detailRegNumber').textContent = regNumber;
    document.getElementById('detailType').textContent = type;
    document.getElementById('detailAddress').textContent = address;
    document.getElementById('detailContact').textContent = contact;
    document.getElementById('detailEmail').textContent = email;
    document.getElementById('detailUsername').textContent = username;
    document.getElementById('detailReceived').textContent = received;
    document.getElementById('detailCert').href = certPath; // Placeholder; integrate backend for file access
    document.getElementById('detailsModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('detailsModal').style.display = 'none';
}

function handleAccept(button) {
    const card = button.closest('.request-card');
    card.querySelector('.status').textContent = 'Approved';
    button.disabled = true;
    card.querySelector('.btn-reject').disabled = true;
    alert('Request approved! Sub-admin access granted.'); // Replace with actual backend call in future
}

function handleReject(button) {
    const card = button.closest('.request-card');
    card.querySelector('.status').textContent = 'Rejected';
    button.disabled = true;
    card.querySelector('.btn-accept').disabled = true;
    alert('Request rejected.'); // Replace with actual backend call in future
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
    const cards = document.querySelectorAll('.request-card');
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