// patient

const role = document.getElementById('role');
const patientSignup = document.querySelector('.signup__patient');

role.addEventListener('change', function () {
    if (role.value === 'Patient') {
        patientSignup.style.display = 'block';
    } else {
        patientSignup.style.display = 'none';
    }
})

// patient age calculation

const dobInput = document.getElementById('dob');
const ageInput = document.getElementById('age');
const gnameInput = document.querySelector('.signup__input__gname');

dobInput.addEventListener('change', function () {
    const dob = new Date(dobInput.value);
    const today = new Date();
    let age = today.getFullYear() - dob.getFullYear();
    const monthDiff = today.getMonth() - dob.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate())) {
        age--;
    }
    ageInput.value = age;

    if (age < 0) {
        ageInput.value = '0';
    }

    if (age < 1) {
        gnameInput.style.display = 'block';
    }else{
        gnameInput.style.display = 'none';
    }
});

// hospital

const hospitalSignup = document.querySelector('.signup__hospital');
role.addEventListener('change', function () {
    if (role.value === 'Hospital') {
        hospitalSignup.style.display = 'block';
    } else {
        hospitalSignup.style.display = 'none';
    }
})

// clinic

const clinicSignup = document.querySelector('.signup__clinic');

role.addEventListener('change', function () {
    if (role.value === 'Clinic') {
        clinicSignup.style.display = 'block';
    } else {
        clinicSignup.style.display = 'none';
    }
})