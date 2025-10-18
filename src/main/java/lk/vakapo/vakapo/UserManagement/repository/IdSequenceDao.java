package lk.vakapo.vakapo.UserManagement.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class IdSequenceDao {

    @PersistenceContext
    private EntityManager em;

    public long nextPatient() {
        Object o = em.createNativeQuery("SELECT NEXT VALUE FOR dbo.seq_patient").getSingleResult();
        return ((Number) o).longValue();
    }

    public long nextHospital() {
        Object o = em.createNativeQuery("SELECT NEXT VALUE FOR dbo.seq_hospital").getSingleResult();
        return ((Number) o).longValue();
    }

    public long nextClinic() {
        Object o = em.createNativeQuery("SELECT NEXT VALUE FOR dbo.seq_clinic").getSingleResult();
        return ((Number) o).longValue();
    }
}
