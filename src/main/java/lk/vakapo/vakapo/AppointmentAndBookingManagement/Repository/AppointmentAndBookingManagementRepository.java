package lk.vakapo.vakapo.AppointmentAndBookingManagement.Repository;


import lk.vakapo.vakapo.AppointmentAndBookingManagement.model.AppointmentAndBooking;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for persisting and querying {@link AppointmentAndBooking} entities.
 *
 * The original code referenced an ``Appointment`` entity and repository which did not
 * exist in the project.  This interface corrects that mismatch by declaring the proper
 * entity type and query method signatures.  Spring will automatically generate the
 * implementation at runtime.
 */
public interface AppointmentAndBookingManagementRepository extends JpaRepository<AppointmentAndBooking, Long> {

    /**
     * Retrieve all appointments for a given user, sorted by date and time.
     *
     * @param userId the id of the user whose appointments should be returned
     * @param sort a sort specification, typically ``Sort.by("appointmentDate", "appointmentTime")``
     * @return a list of appointments belonging to the specified user
     */
    List<AppointmentAndBooking> findByUserId(Integer userId, Sort sort);

    /**
     * Retrieve all appointments on or after a given date for a given user.
     *
     * @param userId the id of the user
     * @param date the minimum appointment date (inclusive)
     * @param sort a sort specification
     * @return a list of upcoming appointments
     */
    List<AppointmentAndBooking> findByUserIdAndAppointmentDateGreaterThanEqual(
            Integer userId, LocalDate date, Sort sort);
}
