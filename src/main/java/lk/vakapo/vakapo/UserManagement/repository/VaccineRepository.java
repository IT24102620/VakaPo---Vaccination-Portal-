package lk.vakapo.vakapo.UserManagement.repository;

import lk.vakapo.vakapo.UserManagement.model.Vaccine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {

    // Find all active vaccines
    List<Vaccine> findByIsActiveTrueOrderByVaccineNameAsc();

    // Find all vaccines (active and inactive)
    List<Vaccine> findAllByOrderByVaccineNameAsc();
    
    // Find all vaccines ordered by ID
    List<Vaccine> findAllByOrderByIdAsc();

    // Find vaccine by name
    Optional<Vaccine> findByVaccineName(String vaccineName);

    // Check if vaccine name exists
    boolean existsByVaccineName(String vaccineName);

    // Find vaccines by name containing (for search)
    List<Vaccine> findByVaccineNameContainingIgnoreCaseOrderByVaccineNameAsc(String vaccineName);

    // Count active vaccines
    long countByIsActiveTrue();

    // Count all vaccines
    long countAllBy();

    // Find vaccines created after a specific date
    @Query("SELECT v FROM Vaccine v WHERE v.createdAt >= :date ORDER BY v.createdAt DESC")
    List<Vaccine> findVaccinesCreatedAfter(@Param("date") java.time.LocalDateTime date);
}
