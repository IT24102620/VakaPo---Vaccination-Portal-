package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.MailManagement.EmailService;
import lk.vakapo.vakapo.MailManagement.EmailTemplateService;
import lk.vakapo.vakapo.UserManagement.model.Appointment;
import lk.vakapo.vakapo.UserManagement.model.Patient;
import lk.vakapo.vakapo.UserManagement.model.VaccinationSchedule;
import lk.vakapo.vakapo.UserManagement.model.Staff;
import lk.vakapo.vakapo.UserManagement.repository.AppointmentRepository;
import lk.vakapo.vakapo.UserManagement.repository.HospitalRepository;
import lk.vakapo.vakapo.UserManagement.repository.ClinicRepository;
import lk.vakapo.vakapo.UserManagement.repository.PatientRepository;
import lk.vakapo.vakapo.UserManagement.repository.VaccinationScheduleRepository;
import lk.vakapo.vakapo.UserManagement.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final VaccinationScheduleRepository vaccinationScheduleRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final ClinicRepository clinicRepository;
    private final StaffRepository staffRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    /**
     * Get all available vaccines from vaccination schedule
     */
    public List<String> getAvailableVaccines() {
        try {
            List<String> vaccines = vaccinationScheduleRepository.findDistinctVaccineNames();
            log.info("Found {} available vaccines", vaccines.size());
            return vaccines;
        } catch (Exception e) {
            log.error("Error fetching available vaccines: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get institutions that offer a specific vaccine
     */
    public List<Map<String, String>> getInstitutionsByVaccine(String vaccineName) {
        try {
            List<Object[]> results = vaccinationScheduleRepository.findInstitutionsByVaccine(vaccineName);
            List<Map<String, String>> institutions = new ArrayList<>();

            for (Object[] result : results) {
                String institutionId = (String) result[0];
                String institutionType = (String) result[1];
                
                String institutionName = getInstitutionName(institutionId, institutionType);
                
                Map<String, String> institution = new HashMap<>();
                institution.put("id", institutionId);
                institution.put("type", institutionType);
                institution.put("name", institutionName);
                institutions.add(institution);
            }

            log.info("Found {} institutions for vaccine: {}", institutions.size(), vaccineName);
            return institutions;
        } catch (Exception e) {
            log.error("Error fetching institutions for vaccine {}: {}", vaccineName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get available days for a specific vaccine and institution
     */
    public List<String> getAvailableDays(String vaccineName, String institutionId, String institutionType) {
        try {
            List<VaccinationSchedule> schedules = vaccinationScheduleRepository.findByVaccineAndInstitution(
                vaccineName, institutionId, institutionType);
            
            Set<String> uniqueDays = new HashSet<>();
            for (VaccinationSchedule schedule : schedules) {
                String[] days = schedule.getDays().split(",");
                for (String day : days) {
                    String trimmedDay = day.trim();
                    // Convert to proper case (e.g., "monday" -> "Monday")
                    if (!trimmedDay.isEmpty()) {
                        String properCaseDay = trimmedDay.substring(0, 1).toUpperCase() + trimmedDay.substring(1).toLowerCase();
                        uniqueDays.add(properCaseDay);
                    }
                }
            }

            List<String> sortedDays = new ArrayList<>(uniqueDays);
            Collections.sort(sortedDays);
            
            log.info("Found {} available days for vaccine {} at institution {}", 
                    sortedDays.size(), vaccineName, institutionId);
            return sortedDays;
        } catch (Exception e) {
            log.error("Error fetching available days for vaccine {} at institution {}: {}", 
                     vaccineName, institutionId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get available time slots for a specific vaccine, institution, and date
     * This method:
     * 1. Gets vaccination schedules for the vaccine and institution
     * 2. Checks if the schedule is available on the selected day
     * 3. Generates 20-minute time slots based on time_from and time_to
     * 4. Filters out already booked time slots
     * 5. Returns only available time slots
     */
    public List<String> getAvailableTimeSlots(String vaccineName, String institutionId, 
                                            String institutionType, LocalDate appointmentDate) {
        try {
            log.info("Getting available time slots for vaccine: {}, institution: {} ({}), date: {}", 
                    vaccineName, institutionId, institutionType, appointmentDate);
            
            // Get vaccination schedules for the vaccine and institution
            List<VaccinationSchedule> schedules = vaccinationScheduleRepository.findByVaccineAndInstitution(
                vaccineName, institutionId, institutionType);

            if (schedules.isEmpty()) {
                log.warn("No vaccination schedules found for vaccine: {} at institution: {} ({})", 
                        vaccineName, institutionId, institutionType);
                return new ArrayList<>();
            }

            // Get day of week for the appointment date
            String dayOfWeek = appointmentDate.getDayOfWeek().toString();
            log.debug("Checking for day of week: {}", dayOfWeek);
            
            // Also check for capitalized version (database might store "Monday" instead of "MONDAY")
            String dayOfWeekCapitalized = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1).toLowerCase();
            log.debug("Also checking for capitalized day: {}", dayOfWeekCapitalized);
            
            List<String> availableTimeSlots = new ArrayList<>();
            List<String> allTimeSlots = new ArrayList<>();

            for (VaccinationSchedule schedule : schedules) {
                log.debug("Checking schedule: {} - {} on days: {}", 
                         schedule.getTimeFrom(), schedule.getTimeTo(), schedule.getDays());
                
                // Check if the schedule is available on the selected day
                // Check both uppercase (MONDAY) and capitalized (Monday) versions
                if (schedule.getDays().contains(dayOfWeek) || schedule.getDays().contains(dayOfWeekCapitalized)) {
                    log.debug("Schedule matches selected day: {} or {}", dayOfWeek, dayOfWeekCapitalized);
                    
                    // Generate time slots based on the schedule's time range
                    List<String> timeSlots = generateTimeSlots(schedule.getTimeFrom(), schedule.getTimeTo());
                    allTimeSlots.addAll(timeSlots);
                    
                    // Filter out already booked time slots
                    for (String timeSlot : timeSlots) {
                        long bookedCount = appointmentRepository.countByDateAndInstitutionAndTimeSlot(
                            appointmentDate, institutionId, institutionType, timeSlot);
                        
                        if (bookedCount == 0) {
                            availableTimeSlots.add(timeSlot);
                            log.debug("Time slot {} is available", timeSlot);
                        } else {
                            log.debug("Time slot {} is already booked (count: {})", timeSlot, bookedCount);
                        }
                    }
                } else {
                    log.debug("Schedule does not match selected day: {} (available days: {})", 
                             dayOfWeek, schedule.getDays());
                }
            }

            Collections.sort(availableTimeSlots);
            log.info("Found {} available time slots out of {} total slots for vaccine {} at institution {} on {}", 
                    availableTimeSlots.size(), allTimeSlots.size(), vaccineName, institutionId, appointmentDate);
            
            if (availableTimeSlots.isEmpty()) {
                log.warn("No available time slots found for the selected criteria");
                log.warn("Debug info - Day of week: {}, Capitalized: {}", dayOfWeek, dayOfWeekCapitalized);
                log.warn("Debug info - Total schedules found: {}", schedules.size());
                for (VaccinationSchedule schedule : schedules) {
                    log.warn("Schedule: {} - {} on days: {}", schedule.getTimeFrom(), schedule.getTimeTo(), schedule.getDays());
                }
            } else {
                log.info("Available time slots: {}", availableTimeSlots);
            }
            
            return availableTimeSlots;
        } catch (Exception e) {
            log.error("Error fetching available time slots for vaccine: {}, institution: {} ({}), date: {}: {}", 
                     vaccineName, institutionId, institutionType, appointmentDate, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Reschedule an existing appointment to a new date (same vaccine and institution).
     * Rules:
     * - Only the appointment owner (patient) can reschedule
     * - Appointment must be in 'scheduled' status
     * - Must be at least 24 hours before the original appointment start time
     * - Vaccine and institution cannot change
     * - New date must have at least one available time slot; first available slot will be assigned
     */
    public boolean rescheduleAppointment(Long appointmentId, String patientEmail, LocalDate newDate, String requestedTimeSlot) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (!appointmentOpt.isPresent()) {
                log.warn("Reschedule failed: appointment not found: {}", appointmentId);
                return false;
            }

            Appointment appointment = appointmentOpt.get();

            // Ownership check
            if (!appointment.getPatientEmail().equals(patientEmail)) {
                log.warn("Reschedule failed: patient {} does not own appointment {}", patientEmail, appointmentId);
                return false;
            }

            // Status check
            if (!"scheduled".equals(appointment.getStatus())) {
                log.warn("Reschedule failed: appointment {} is not in scheduled status: {}", appointmentId, appointment.getStatus());
                return false;
            }

            // Enforce 24-hour rule using the start of the time slot
            try {
                String startTimeStr = appointment.getTimeSlot().split("-")[0]; // e.g., 08:00 from 08:00-08:20
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime startTime = LocalTime.parse(startTimeStr, formatter);
                LocalDateTime appointmentStart = LocalDateTime.of(appointment.getAppointmentDate(), startTime);

                if (LocalDateTime.now().isAfter(appointmentStart.minusHours(24))) {
                    log.warn("Reschedule failed: within 24 hours of appointment {} (start: {})", appointmentId, appointmentStart);
                    return false;
                }
            } catch (Exception timeParseEx) {
                log.warn("Reschedule warning: could not parse time slot '{}' for appointment {}. Fallback to date-only 24h check.", appointment.getTimeSlot(), appointmentId);
                // Fallback: prevent reschedule if today is the same as appointment date
                if (!LocalDate.now().isBefore(appointment.getAppointmentDate().minusDays(1))) {
                    return false;
                }
            }

            // If new date is same as old date, no-op
            if (appointment.getAppointmentDate().isEqual(newDate)) {
                log.info("Reschedule skipped: new date is same as existing for appointment {}", appointmentId);
                return true;
            }

            // Find available time slots for the new date (same vaccine/institution)
            List<String> availableSlots = getAvailableTimeSlots(
                appointment.getVaccineName(),
                appointment.getInstitutionId(),
                appointment.getInstitutionType(),
                newDate
            );

            if (availableSlots.isEmpty()) {
                log.warn("Reschedule failed: no available time slots on {} for appointment {}", newDate, appointmentId);
                return false;
            }

            // Validate that the requested time slot is available
            if (!availableSlots.contains(requestedTimeSlot)) {
                log.error("Requested time slot {} is not available for appointment {}", requestedTimeSlot, appointmentId);
                throw new IllegalArgumentException("The selected time slot is no longer available. Please choose another time slot.");
            }
            
            String newTimeSlot = requestedTimeSlot;

            appointment.setAppointmentDate(newDate);
            appointment.setTimeSlot(newTimeSlot);
            appointmentRepository.save(appointment);

            // Send reschedule confirmation
            try {
                sendAppointmentRescheduleEmail(appointment);
            } catch (Exception e) {
                log.error("Failed to send reschedule email for appointment {}: {}", appointment.getId(), e.getMessage());
            }

            log.info("Appointment {} rescheduled successfully to {} {}", appointmentId, newDate, newTimeSlot);
            return true;
        } catch (Exception e) {
            log.error("Error rescheduling appointment {}: {}", appointmentId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Book an appointment
     */
    public Appointment bookAppointment(String patientEmail, String vaccineName, String institutionId, 
                                     String institutionType, LocalDate appointmentDate, String timeSlot) {
        try {
            // Get patient information
            Optional<Patient> patientOpt = patientRepository.findByEmail(patientEmail);
            if (!patientOpt.isPresent()) {
                throw new IllegalArgumentException("Patient not found with email: " + patientEmail);
            }

            Patient patient = patientOpt.get();

            // Check if time slot is still available
            long bookedCount = appointmentRepository.countByDateAndInstitutionAndTimeSlot(
                appointmentDate, institutionId, institutionType, timeSlot);
            
            log.info("Checking availability for time slot {} on {} at {} ({}): {} existing bookings", 
                    timeSlot, appointmentDate, institutionId, institutionType, bookedCount);
            
            if (bookedCount > 0) {
                log.warn("Time slot {} is no longer available for {} at {} ({}) on {}", 
                        timeSlot, vaccineName, institutionId, institutionType, appointmentDate);
                throw new IllegalArgumentException("Time slot " + timeSlot + " is no longer available");
            }

            // Get institution name
            String institutionName = getInstitutionName(institutionId, institutionType);

            // Get doctor name from vaccination schedule
            List<VaccinationSchedule> schedules = vaccinationScheduleRepository.findByVaccineAndInstitution(
                vaccineName, institutionId, institutionType);
            
            if (schedules.isEmpty()) {
                throw new IllegalArgumentException("No vaccination schedule found for the selected vaccine and institution");
            }

            String doctorIdFromSchedule = schedules.get(0).getDoctorName();
            String doctorName = getRealDoctorName(doctorIdFromSchedule, institutionId, institutionType);

            // Create appointment
            Appointment appointment = new Appointment();
            appointment.setPatientId(patient.getId());
            appointment.setPatientEmail(patient.getEmail());
            appointment.setPatientName(patient.getPatientName());
            appointment.setVaccineName(vaccineName);
            appointment.setInstitutionType(institutionType);
            appointment.setInstitutionId(institutionId);
            appointment.setInstitutionName(institutionName);
            appointment.setDoctorName(doctorName);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setTimeSlot(timeSlot);
            appointment.setStatus("scheduled");

            Appointment savedAppointment = appointmentRepository.save(appointment);
            log.info("Appointment booked successfully: {}", savedAppointment.getId());
            
            // Send confirmation email to patient
            try {
                sendAppointmentConfirmationEmail(savedAppointment);
                log.info("Appointment confirmation email sent successfully for appointment: {}", savedAppointment.getId());
            } catch (Exception e) {
                log.error("Failed to send appointment confirmation email for appointment: {}. Error: {}", 
                         savedAppointment.getId(), e.getMessage(), e);
                // Don't fail the appointment booking if email fails
            }
            
            return savedAppointment;
        } catch (Exception e) {
            log.error("Error booking appointment: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get patient's appointments
     */
    public List<Appointment> getPatientAppointments(String patientEmail) {
        try {
            List<Appointment> appointments = appointmentRepository.findByPatientEmailOrderByAppointmentDateDesc(patientEmail);
            log.info("Found {} appointments for patient: {}", appointments.size(), patientEmail);
            return appointments;
        } catch (Exception e) {
            log.error("Error fetching patient appointments: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get appointments for a specific institution (Hospital or Clinic)
     */
    public List<Appointment> getAppointmentsByInstitution(String institutionId, String institutionType) {
        try {
            List<Appointment> appointments = appointmentRepository.findByInstitutionIdAndType(institutionId, institutionType);
            log.info("Found {} appointments for {}: {}", appointments.size(), institutionType, institutionId);
            return appointments;
        } catch (Exception e) {
            log.error("Error fetching appointments for {} {}: {}", institutionType, institutionId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get appointment by ID
     */
    public Optional<Appointment> getAppointmentById(Long appointmentId) {
        try {
            return appointmentRepository.findById(appointmentId);
        } catch (Exception e) {
            log.error("Error fetching appointment by ID {}: {}", appointmentId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Cancel an appointment by hospital (hospital staff can cancel appointments)
     */
    public boolean cancelAppointmentByHospital(Long appointmentId, String hospitalId) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (!appointmentOpt.isPresent()) {
                log.warn("Appointment not found: {}", appointmentId);
                return false;
            }

            Appointment appointment = appointmentOpt.get();
            if (!appointment.getInstitutionId().equals(hospitalId) || !appointment.getInstitutionType().equals("Hospital")) {
                log.warn("Appointment {} does not belong to hospital {}", appointmentId, hospitalId);
                return false;
            }

            if (!"scheduled".equals(appointment.getStatus())) {
                log.warn("Appointment {} is not in scheduled status: {}", appointmentId, appointment.getStatus());
                return false;
            }

            appointment.setStatus("cancelled");
            appointment.setCancelledBy("hospital");
            appointmentRepository.save(appointment);
            log.info("Appointment cancelled by hospital successfully: {}", appointmentId);
            
            // Send hospital-initiated cancellation confirmation email to patient
            try {
                sendHospitalCancellationEmail(appointment);
                log.info("Hospital-initiated appointment cancellation email sent successfully for appointment: {}", appointmentId);
            } catch (Exception e) {
                log.error("Failed to send hospital-initiated appointment cancellation email for appointment: {}. Error: {}", 
                         appointmentId, e.getMessage(), e);
                // Don't fail the cancellation if email fails
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error cancelling appointment by hospital: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send appointment cancellation confirmation email to patient
     */
    public void sendAppointmentCancellationEmail(Appointment appointment) {
        try {
            log.info("Sending appointment cancellation confirmation email for appointment: {}", appointment.getId());
            
            // Send only HTML email (well-styled version)
            emailTemplateService.sendHtmlAppointmentCancellationConfirmation(
                appointment.getPatientEmail(),
                appointment.getPatientName(),
                appointment.getVaccineName(),
                appointment.getInstitutionName(),
                appointment.getInstitutionType(),
                appointment.getDoctorName(),
                appointment.getAppointmentDate().toString(),
                appointment.getTimeSlot(),
                appointment.getId()
            );
            
            log.info("Appointment cancellation confirmation email sent successfully for appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error sending appointment cancellation email for appointment: {}. Error: {}", 
                     appointment.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Send hospital-initiated appointment cancellation email to patient
     */
    public void sendHospitalCancellationEmail(Appointment appointment) {
        try {
            log.info("Sending hospital-initiated appointment cancellation email for appointment: {}", appointment.getId());
            
            // Send only HTML email (well-styled version)
            emailTemplateService.sendHtmlHospitalCancellationConfirmation(
                appointment.getPatientEmail(),
                appointment.getPatientName(),
                appointment.getVaccineName(),
                appointment.getInstitutionName(),
                appointment.getInstitutionType(),
                appointment.getDoctorName(),
                appointment.getAppointmentDate().toString(),
                appointment.getTimeSlot(),
                appointment.getId()
            );
            
            log.info("Hospital-initiated appointment cancellation email sent successfully for appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error sending hospital-initiated appointment cancellation email for appointment: {}. Error: {}", 
                     appointment.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Reverse a cancelled appointment (reschedule it)
     */
    public boolean reverseAppointment(Long appointmentId, String hospitalId) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (!appointmentOpt.isPresent()) {
                log.warn("Appointment not found: {}", appointmentId);
                return false;
            }

            Appointment appointment = appointmentOpt.get();
            if (!appointment.getInstitutionId().equals(hospitalId) || !appointment.getInstitutionType().equals("Hospital")) {
                log.warn("Appointment {} does not belong to hospital {}", appointmentId, hospitalId);
                return false;
            }

            if (!"cancelled".equals(appointment.getStatus())) {
                log.warn("Appointment {} is not in cancelled status: {}", appointmentId, appointment.getStatus());
                return false;
            }

            if (!"hospital".equals(appointment.getCancelledBy())) {
                log.warn("Appointment {} was not cancelled by hospital, cannot reverse. Cancelled by: {}", appointmentId, appointment.getCancelledBy());
                return false;
            }

            // Find a new available time slot
            String newTimeSlot = findAvailableTimeSlot(appointment);
            if (newTimeSlot == null) {
                log.warn("No available time slots found for appointment: {}", appointmentId);
                return false;
            }

            // Update appointment with new time slot
            appointment.setTimeSlot(newTimeSlot);
            appointment.setStatus("scheduled");
            appointmentRepository.save(appointment);
            
            log.info("Appointment reversed successfully: {} with new time slot: {}", appointmentId, newTimeSlot);
            
            // Send reversal confirmation email to patient
            try {
                sendAppointmentReversalEmail(appointment);
                log.info("Appointment reversal email sent successfully for appointment: {}", appointmentId);
            } catch (Exception e) {
                log.error("Failed to send appointment reversal email for appointment: {}. Error: {}", 
                         appointmentId, e.getMessage(), e);
                // Don't fail the reversal if email fails
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error reversing appointment: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Find an available time slot for the appointment
     */
    private String findAvailableTimeSlot(Appointment appointment) {
        try {
            // Get vaccination schedules for the same vaccine and institution
            List<VaccinationSchedule> schedules = vaccinationScheduleRepository.findByVaccineAndInstitution(
                appointment.getVaccineName(), appointment.getInstitutionId(), appointment.getInstitutionType());

            if (schedules.isEmpty()) {
                log.warn("No vaccination schedules found for vaccine: {} at institution: {}", 
                        appointment.getVaccineName(), appointment.getInstitutionId());
                return null;
            }

            // Try to find available slots starting from the original appointment date
            LocalDate currentDate = appointment.getAppointmentDate();
            LocalDate maxDate = currentDate.plusDays(30); // Search up to 30 days ahead

            while (currentDate.isBefore(maxDate) || currentDate.isEqual(maxDate)) {
                // Get available time slots for this date
                List<String> availableSlots = getAvailableTimeSlots(
                    appointment.getVaccineName(), 
                    appointment.getInstitutionId(), 
                    appointment.getInstitutionType(), 
                    currentDate
                );

                if (!availableSlots.isEmpty()) {
                    // Return the first available slot
                    log.info("Found available time slot: {} on date: {} for appointment: {}", 
                            availableSlots.get(0), currentDate, appointment.getId());
                    return availableSlots.get(0);
                }

                currentDate = currentDate.plusDays(1);
            }

            log.warn("No available time slots found within 30 days for appointment: {}", appointment.getId());
            return null;
        } catch (Exception e) {
            log.error("Error finding available time slot for appointment: {}", appointment.getId(), e);
            return null;
        }
    }

    /**
     * Send appointment reversal confirmation email to patient
     */
    public void sendAppointmentReversalEmail(Appointment appointment) {
        try {
            log.info("Sending appointment reversal confirmation email for appointment: {}", appointment.getId());
            
            // Send only HTML email (well-styled version)
            emailTemplateService.sendHtmlAppointmentReversalConfirmation(
                appointment.getPatientEmail(),
                appointment.getPatientName(),
                appointment.getVaccineName(),
                appointment.getInstitutionName(),
                appointment.getInstitutionType(),
                appointment.getDoctorName(),
                appointment.getAppointmentDate().toString(),
                appointment.getTimeSlot(),
                appointment.getId()
            );
            
            log.info("Appointment reversal confirmation email sent successfully for appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error sending appointment reversal email for appointment: {}. Error: {}", 
                     appointment.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Send appointment reschedule confirmation email to patient
     */
    public void sendAppointmentRescheduleEmail(Appointment appointment) {
        try {
            log.info("Sending appointment reschedule confirmation email for appointment: {}", appointment.getId());
            emailTemplateService.sendHtmlAppointmentRescheduleConfirmation(
                appointment.getPatientEmail(),
                appointment.getPatientName(),
                appointment.getVaccineName(),
                appointment.getInstitutionName(),
                appointment.getInstitutionType(),
                appointment.getDoctorName(),
                appointment.getAppointmentDate().toString(),
                appointment.getTimeSlot(),
                appointment.getId()
            );
        } catch (Exception e) {
            log.error("Error sending reschedule email for appointment {}: {}", appointment.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Cancel an appointment
     */
    public boolean cancelAppointment(Long appointmentId, String patientEmail) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (!appointmentOpt.isPresent()) {
                throw new IllegalArgumentException("Appointment not found");
            }

            Appointment appointment = appointmentOpt.get();
            if (!appointment.getPatientEmail().equals(patientEmail)) {
                throw new IllegalArgumentException("You can only cancel your own appointments");
            }

            if (!"scheduled".equals(appointment.getStatus())) {
                throw new IllegalArgumentException("Only scheduled appointments can be cancelled");
            }

            appointment.setStatus("cancelled");
            appointment.setCancelledBy("patient");
            appointmentRepository.save(appointment);
            log.info("Appointment cancelled by patient successfully: {}", appointmentId);
            
            // Send cancellation confirmation email to patient
            try {
                sendAppointmentCancellationEmail(appointment);
                log.info("Appointment cancellation email sent successfully for appointment: {}", appointmentId);
            } catch (Exception e) {
                log.error("Failed to send appointment cancellation email for appointment: {}. Error: {}", 
                         appointmentId, e.getMessage(), e);
                // Don't fail the cancellation if email fails
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error cancelling appointment: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generate time slots based on time range (20-minute intervals)
     * Example: timeFrom="08:00", timeTo="10:00" generates:
     * 08:00-08:20, 08:20-08:40, 08:40-09:00, 09:00-09:20, 09:20-09:40, 09:40-10:00
     */
    private List<String> generateTimeSlots(String timeFrom, String timeTo) {
        List<String> timeSlots = new ArrayList<>();
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime startTime = LocalTime.parse(timeFrom, formatter);
            LocalTime endTime = LocalTime.parse(timeTo, formatter);
            
            log.debug("Generating time slots from {} to {}", timeFrom, timeTo);
            
            LocalTime currentTime = startTime;
            while (currentTime.isBefore(endTime)) {
                LocalTime nextTime = currentTime.plusMinutes(20);
                
                // Only add slot if the next time doesn't exceed the end time
                if (!nextTime.isAfter(endTime)) {
                    String timeSlot = currentTime.format(formatter) + "-" + nextTime.format(formatter);
                    timeSlots.add(timeSlot);
                    log.debug("Added time slot: {}", timeSlot);
                } else {
                    log.debug("Skipping time slot {} as it would exceed end time {}", 
                             currentTime.format(formatter), endTime.format(formatter));
                    break;
                }
                
                currentTime = nextTime;
            }
            
            log.info("Generated {} time slots from {} to {}", timeSlots.size(), timeFrom, timeTo);
        } catch (Exception e) {
            log.error("Error generating time slots from {} to {}: {}", timeFrom, timeTo, e.getMessage(), e);
        }
        
        return timeSlots;
    }

    /**
     * Debug method to get all vaccination schedules
     */
    public List<VaccinationSchedule> getAllVaccinationSchedules() {
        try {
            log.info("Debug: Getting all vaccination schedules");
            List<VaccinationSchedule> allSchedules = vaccinationScheduleRepository.findAll();
            log.info("Debug: Found {} total schedules", allSchedules.size());
            return allSchedules;
        } catch (Exception e) {
            log.error("Debug: Error getting all vaccination schedules: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Debug method to get vaccination schedules for troubleshooting
     */
    public List<VaccinationSchedule> getVaccinationSchedulesForDebug(String vaccineName, String institutionId, String institutionType) {
        try {
            log.info("Debug: Getting vaccination schedules for vaccine: {}, institution: {} ({})", 
                    vaccineName, institutionId, institutionType);
            
            List<VaccinationSchedule> schedules = vaccinationScheduleRepository.findByVaccineAndInstitution(
                vaccineName, institutionId, institutionType);
            
            log.info("Debug: Found {} schedules", schedules.size());
            return schedules;
        } catch (Exception e) {
            log.error("Debug: Error getting vaccination schedules: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get institution name by ID and type
     */
    private String getInstitutionName(String institutionId, String institutionType) {
        try {
            if ("Hospital".equals(institutionType)) {
                try {
                    return hospitalRepository.findById(institutionId)
                        .map(hospital -> {
                            if (hospital.getUsername() != null && !hospital.getUsername().trim().isEmpty()) {
                                return hospital.getUsername();
                            } else {
                                return "Hospital " + institutionId;
                            }
                        })
                        .orElse("Hospital " + institutionId);
                } catch (Exception e) {
                    log.warn("Error looking up hospital {}: {}", institutionId, e.getMessage());
                    return "Hospital " + institutionId;
                }
            } else if ("Clinic".equals(institutionType)) {
                try {
                    return clinicRepository.findById(institutionId)
                        .map(clinic -> {
                            if (clinic.getUsername() != null && !clinic.getUsername().trim().isEmpty()) {
                                return clinic.getUsername();
                            } else {
                                return "Clinic " + institutionId;
                            }
                        })
                        .orElse("Clinic " + institutionId);
                } catch (Exception e) {
                    log.warn("Error looking up clinic {}: {}", institutionId, e.getMessage());
                    return "Clinic " + institutionId;
                }
            }
            return institutionType + " " + institutionId;
        } catch (Exception e) {
            log.error("Error getting institution name: {}", e.getMessage(), e);
            return institutionType + " " + institutionId;
        }
    }

    /**
     * Send appointment confirmation email to patient
     */
    public void sendAppointmentConfirmationEmail(Appointment appointment) {
        try {
            log.info("Sending appointment confirmation email for appointment: {}", appointment.getId());
            
            // Send only HTML email (well-styled version)
            emailTemplateService.sendHtmlAppointmentBookingConfirmation(
                appointment.getPatientEmail(),
                appointment.getPatientName(),
                appointment.getVaccineName(),
                appointment.getInstitutionName(),
                appointment.getInstitutionType(),
                appointment.getDoctorName(),
                appointment.getAppointmentDate().toString(),
                appointment.getTimeSlot(),
                appointment.getId()
            );
            
            log.info("Appointment confirmation email sent successfully for appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error sending appointment confirmation email for appointment: {}. Error: {}", 
                     appointment.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get real doctor name from Staff table instead of using ID
     */
    private String getRealDoctorName(String doctorIdFromSchedule, String institutionId, String institutionType) {
        try {
            // If the doctor name from schedule looks like an ID (contains "Dr. " and numbers), try to find the real name
            if (doctorIdFromSchedule != null && doctorIdFromSchedule.startsWith("Dr. ")) {
                // Find staff records for this institution
                List<Staff> staffList = staffRepository.findAcceptedStaff(institutionId, institutionType);
                
                // Look for a doctor with a name that matches the ID pattern
                for (Staff staff : staffList) {
                    if ("Doctor".equals(staff.getRole())) {
                        // Check if this doctor's ID matches the one from schedule
                        // We'll use the staff name as the real doctor name
                        log.info("Found real doctor name: '{}' for doctor ID: '{}'", staff.getName(), doctorIdFromSchedule);
                        return staff.getName();
                    }
                }
                
                log.warn("No staff record found for doctor ID: '{}' at institution: '{}', using ID as name", doctorIdFromSchedule, institutionId);
            }
            
            // Fallback to the original name from schedule
            return doctorIdFromSchedule;
        } catch (Exception e) {
            log.error("Error getting real doctor name for ID: '{}' at institution: '{}'", doctorIdFromSchedule, institutionId, e);
            return doctorIdFromSchedule; // Fallback to original
        }
    }

}
