package lk.vakapo.vakapo.UserManagement.repository;

import lk.vakapo.vakapo.UserManagement.model.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, String> {
    List<Clinic> findByAdminApproval(String adminApproval); // "approved" | "not approved"
    Optional<Clinic> findByEmail(String email);
    void deleteByEmail(String email);
}
