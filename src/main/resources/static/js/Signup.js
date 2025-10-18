// Role-based show/hide + required toggling
const role = document.getElementById('role');
const patientBox  = document.querySelector('.signup__patient');
const hospitalBox = document.querySelector('.signup__hospital');
const clinicBox   = document.querySelector('.signup__clinic');

function setRequired(el, required) {
    if (!el) return;
    if (required) el.setAttribute('required', 'required');
    else el.removeAttribute('required');
}

function toggleSections() {
    const r = role.value;

    // Hide all
    patientBox.style.display  = 'none';
    hospitalBox.style.display = 'none';
    clinicBox.style.display   = 'none';

    // Remove required from all section fields
    ['pname','dob','gname','gender','nic',
        'hname','rnumber','institution','rcertificate'].forEach(name => {
        document.querySelectorAll(`[name="${name}"]`).forEach(el => setRequired(el, false));
    });

    if (r === 'Patient') {
        patientBox.style.display = 'block';
        setRequired(document.getElementById('pname'), true);
        setRequired(document.getElementById('dob'), true);
        // gname required dynamically based on age (see below)
        setRequired(document.getElementById('gender'), true);
        // nic optional; uncomment to enforce:
        // setRequired(document.getElementById('nic'), true);
        // Disable org fields so they are not submitted when hidden
        // Hospital fields
        document.getElementById('hnameHospital').disabled = true;
        document.getElementById('rnumberHospital').disabled = true;
        document.getElementById('institutionHospital').disabled = true;
        document.getElementById('rcertificateHospital').disabled = true;
        // Clinic fields
        document.getElementById('hnameClinic').disabled = true;
        document.getElementById('rnumberClinic').disabled = true;
        document.getElementById('institutionClinic').disabled = true;
        document.getElementById('rcertificateClinic').disabled = true;
    } else if (r === 'Hospital') {
        hospitalBox.style.display = 'block';
        setRequired(document.getElementById('hnameHospital'), true);
        setRequired(document.getElementById('rnumberHospital'), true);
        setRequired(document.getElementById('institutionHospital'), true);
        setRequired(document.getElementById('rcertificateHospital'), true);

        // Enable hospital fields and disable clinic fields
        document.getElementById('hnameHospital').disabled = false;
        document.getElementById('rnumberHospital').disabled = false;
        document.getElementById('institutionHospital').disabled = false;
        document.getElementById('rcertificateHospital').disabled = false;
        // Disable clinic fields so they are not submitted
        document.getElementById('hnameClinic').disabled = true;
        document.getElementById('rnumberClinic').disabled = true;
        document.getElementById('institutionClinic').disabled = true;
        document.getElementById('rcertificateClinic').disabled = true;
        // Patient-specific fields remain unaffected
    } else if (r === 'Clinic') {
        clinicBox.style.display = 'block';
        setRequired(document.getElementById('hnameClinic'), true);
        setRequired(document.getElementById('rnumberClinic'), true);
        setRequired(document.getElementById('institutionClinic'), true);
        setRequired(document.getElementById('rcertificateClinic'), true);

        // Enable clinic fields and disable hospital fields
        document.getElementById('hnameClinic').disabled = false;
        document.getElementById('rnumberClinic').disabled = false;
        document.getElementById('institutionClinic').disabled = false;
        document.getElementById('rcertificateClinic').disabled = false;
        // Disable hospital fields so they are not submitted
        document.getElementById('hnameHospital').disabled = true;
        document.getElementById('rnumberHospital').disabled = true;
        document.getElementById('institutionHospital').disabled = true;
        document.getElementById('rcertificateHospital').disabled = true;
    }
}
role.addEventListener('change', toggleSections);
toggleSections(); // initialize

// Patient age calculation + guardian required if age < 1
const dobInput = document.getElementById('dob');
const ageInput = document.getElementById('age');
const gnameWrap = document.querySelector('.signup__input__gname');
const gnameInput = document.getElementById('gname');

function updateAgeAndGuardian() {
    if (!dobInput || !dobInput.value) {
        if (ageInput) ageInput.value = '';
        if (gnameWrap) gnameWrap.style.display = 'none';
        if (gnameInput) setRequired(gnameInput, false);
        return;
    }
    const dob = new Date(dobInput.value);
    const today = new Date();
    let age = today.getFullYear() - dob.getFullYear();
    const m = today.getMonth() - dob.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < dob.getDate())) age--;
    if (ageInput) ageInput.value = Math.max(0, age);

    if (age < 1) {
        if (gnameWrap) gnameWrap.style.display = 'block';
        if (gnameInput) setRequired(gnameInput, true);
    } else {
        if (gnameWrap) gnameWrap.style.display = 'none';
        if (gnameInput) setRequired(gnameInput, false);
    }
}
if (dobInput) dobInput.addEventListener('change', updateAgeAndGuardian);

// Debug function to log form data
function debugFormData(formData) {
    console.log("=== FORM DATA DEBUG ===");
    for (let [key, value] of formData.entries()) {
        console.log(key + ": " + value);
    }
    console.log("========================");
}

// Submit via fetch; show alert with ID; optional redirect to /login
document.getElementById("signupForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    // Check if submit button is disabled
    const submitBtn = document.getElementById('submitBtn');
    if (submitBtn.disabled) {
        alert("Please fill all required fields correctly before submitting.");
        return;
    }

    const form = e.target;
    const formData = new FormData(form);

    // Debug: Log form data
    debugFormData(formData);

    // CSRF support (if enabled)
    const csrfInput = form.querySelector('input[name="_csrf"]');
    const headers = {};
    if (csrfInput) {
        headers['X-CSRF-TOKEN'] = csrfInput.value;
    }

    // Disable submit button to prevent double submission
    submitBtn.disabled = true;
    submitBtn.textContent = "Creating Account...";

    try {
        const response = await fetch(form.action || "/auth/signup", {
            method: "POST",
            headers,
            body: formData
        });

        const responseText = await response.text();
        
        // Try to parse as JSON first
        let result;
        try {
            result = JSON.parse(responseText);
        } catch (parseError) {
            // If not JSON, treat as plain text (backward compatibility)
            if (!response.ok) {
                throw new Error(responseText || "Signup failed");
            }
            alert("Signup successful! Your ID: " + responseText);
            window.location.href = "/login";
            return;
        }

        // Handle JSON response
        if (result.success) {
            alert("Signup successful! Your ID: " + result.userId + "\n" + result.message);
            window.location.href = "/login";
        } else {
            alert("Signup failed: " + result.error);
            // Re-enable submit button on error
            submitBtn.disabled = false;
            submitBtn.textContent = "Create Account";
        }
    } catch (err) {
        alert("Error during signup: " + err.message);
        // Re-enable submit button on error
        submitBtn.disabled = false;
        submitBtn.textContent = "Create Account";
    }
});

// Email availability check
const emailField = document.getElementById('email');
if (emailField) {
    let emailCheckTimeout;
    emailField.addEventListener('input', function() {
        clearTimeout(emailCheckTimeout);
        const email = this.value.trim();
        
        if (email && /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)) {
            emailCheckTimeout = setTimeout(async () => {
                try {
                    const response = await fetch(`/auth/check-email?email=${encodeURIComponent(email)}`);
                    const result = await response.json();
                    
                    if (result.exists) {
                        const errorDiv = document.getElementById('email-error');
                        if (errorDiv) {
                            errorDiv.textContent = 'This email is already registered. Please use a different email.';
                            errorDiv.style.display = 'block';
                        }
                    } else {
                        const errorDiv = document.getElementById('email-error');
                        if (errorDiv) {
                            errorDiv.textContent = '';
                            errorDiv.style.display = 'none';
                        }
                    }
                } catch (error) {
                    console.log('Email check failed:', error);
                }
            }, 500); // Debounce for 500ms
        }
    });
}
