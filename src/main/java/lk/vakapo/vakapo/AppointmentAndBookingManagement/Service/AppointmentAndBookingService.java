package lk.vakapo.vakapo.AppointmentAndBookingManagement.Service;

import lk.vakapo.vakapo.AppointmentAndBookingManagement.model.AppointmentAndBooking;
import lk.vakapo.vakapo.AppointmentAndBookingManagement.Repository.AppointmentAndBookingManagementRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AppointmentAndBookingService {

    private final AppointmentAndBookingManagementRepository repo;

    public AppointmentAndBookingService(AppointmentAndBookingManagementRepository repo) {
        this.repo = repo;
    }

    /**
     * Persist a new appointment in the database.  Rejects appointments
     * scheduled in the past by throwing an {@link IllegalArgumentException}.
     *
     * @param a the appointment to create
     * @return the saved appointment entity
     */
    public AppointmentAndBooking create(AppointmentAndBooking a) {
        // Basic guard: prevent past-date bookings (optional)
        if (a.getAppointmentDate() != null && a.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Appointment date cannot be in the past.");
        }
        a.setStatus(AppointmentAndBooking.Status.BOOKED);
        return repo.save(a);
    }

    /**
     * Retrieve all appointments for the specified user, sorted by date and time.
     *
     * @param userId the user id
     * @return a sorted list of appointments
     */
    public List<AppointmentAndBooking> getForUser(Integer userId) {
        return repo.findByUserId(
                userId,
                Sort.by(Sort.Direction.ASC, "appointmentDate", "appointmentTime")
        );
    }

    /**
     * Retrieve upcoming appointments (today or future) for the specified user.
     *
     * @param userId the user id
     * @return a list of upcoming appointments
     */
    public List<AppointmentAndBooking> upcomingForUser(Integer userId) {
        return repo.findByUserIdAndAppointmentDateGreaterThanEqual(
                userId,
                LocalDate.now(),
                Sort.by(Sort.Direction.ASC, "appointmentDate", "appointmentTime")
        );
    }

    /**
     * Cancel an appointment.  Only the appointment owner or an administrator may cancel
     * the appointment.  Cancelling will set the appointment status to CANCELED
     * and persist the change.
     *
     * @param appointmentId the id of the appointment
     * @param requestingUserId the id of the user making the request
     * @param isAdmin whether the requester has administrator privileges
     * @return the updated appointment entity
     */
    public AppointmentAndBooking cancel(Long appointmentId, Integer requestingUserId, boolean isAdmin) {
        AppointmentAndBooking appt = repo.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!isAdmin && !appt.getUserId().equals(requestingUserId)) {
            throw new SecurityException("Not allowed to cancel this appointment.");
        }

        appt.setStatus(AppointmentAndBooking.Status.CANCELED);
        return repo.save(appt);
    }

    /**
     * Mark an appointment as completed.  Does not perform any ownership checks.
     *
     * @param appointmentId the id of the appointment
     * @return the updated appointment entity
     */
    public AppointmentAndBooking complete(Long appointmentId) {
        AppointmentAndBooking appt = repo.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appt.setStatus(AppointmentAndBooking.Status.COMPLETED);
        return repo.save(appt);
    }

    /**
     * Delete an appointment from the database.  Only administrators may delete
     * appointments outright.  Patients should use {@link #cancel(Long, Integer, boolean)} instead.
     *
     * @param appointmentId the id of the appointment
     * @param isAdmin whether the requester has administrator privileges
     */
    public void delete(Long appointmentId, boolean isAdmin) {
        if (!isAdmin) throw new SecurityException("Only admins can delete appointments.");
        repo.deleteById(appointmentId);
    }
}
