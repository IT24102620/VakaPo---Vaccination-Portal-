package lk.vakapo.vakapo.Service;


import lk.vakapo.vakapo.Model.*;
import lk.vakapo.vakapo.Repository.*;
import lk.vakapo.vakapo.dto.FeedbackDTO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@Service
public class FeedbackService {

    @Autowired private DoctorFeedbackRepository doctorRepo;
    @Autowired private NurseFeedbackRepository nurseRepo;
    @Autowired private HospitalFeedbackRepository hospitalRepo;
    @Autowired private PatientFeedbackRepository patientRepo;

    // Save feedback
    public void saveFeedback(FeedbackDTO dto) {
        Feedback feedback;
        switch (dto.getCategory()) {
            case "Doctor" -> {
                feedback = new DoctorFeedback();
                mapDTOToEntity(dto, feedback);
                doctorRepo.save((DoctorFeedback) feedback);
            }
            case "Nurse" -> {
                feedback = new NurseFeedback();
                mapDTOToEntity(dto, feedback);
                nurseRepo.save((NurseFeedback) feedback);
            }
            case "Hospital" -> {
                feedback = new HospitalFeedback();
                mapDTOToEntity(dto, feedback);
                hospitalRepo.save((HospitalFeedback) feedback);
            }
            case "Patient" -> {
                feedback = new PatientFeedback();
                mapDTOToEntity(dto, feedback);
                patientRepo.save((PatientFeedback) feedback);
            }
            default -> throw new IllegalArgumentException("Invalid category: " + dto.getCategory());
        }
    }

    private void mapDTOToEntity(FeedbackDTO dto, Feedback feedback) {
        feedback.setName(dto.getName());
        feedback.setEmail(dto.getEmail());
        feedback.setContact(dto.getContact());
        feedback.setRating(dto.getRating());
        feedback.setMessage(dto.getMessage());
        feedback.setStatus(FeedbackStatus.PENDING);
    }

    // Get all feedbacks
    public List<FeedbackDTO> getAllFeedbacks() {
        List<FeedbackDTO> all = new ArrayList<>();
        doctorRepo.findAll().forEach(f -> all.add(toDTO(f, "Doctor")));
        nurseRepo.findAll().forEach(f -> all.add(toDTO(f, "Nurse")));
        hospitalRepo.findAll().forEach(f -> all.add(toDTO(f, "Hospital")));
        patientRepo.findAll().forEach(f -> all.add(toDTO(f, "Patient")));
        return all;
    }

    // Get only approved feedbacks
    public List<FeedbackDTO> getApprovedFeedbacks() {
        List<FeedbackDTO> approved = new ArrayList<>();
        doctorRepo.findByStatus(FeedbackStatus.APPROVED).forEach(f -> approved.add(toDTO(f, "Doctor")));
        nurseRepo.findByStatus(FeedbackStatus.APPROVED).forEach(f -> approved.add(toDTO(f, "Nurse")));
        hospitalRepo.findByStatus(FeedbackStatus.APPROVED).forEach(f -> approved.add(toDTO(f, "Hospital")));
        patientRepo.findByStatus(FeedbackStatus.APPROVED).forEach(f -> approved.add(toDTO(f, "Patient")));
        return approved;
    }

    // Update status (Approve / Unapprove)
    public void updateStatus(String category, Long id, FeedbackStatus newStatus) {
        switch (category) {
            case "Doctor" -> {
                DoctorFeedback f = doctorRepo.findById(id).orElseThrow();
                f.setStatus(newStatus);
                doctorRepo.save(f);
            }
            case "Nurse" -> {
                NurseFeedback f = nurseRepo.findById(id).orElseThrow();
                f.setStatus(newStatus);
                nurseRepo.save(f);
            }
            case "Hospital" -> {
                HospitalFeedback f = hospitalRepo.findById(id).orElseThrow();
                f.setStatus(newStatus);
                hospitalRepo.save(f);
            }
            case "Patient" -> {
                PatientFeedback f = patientRepo.findById(id).orElseThrow();
                f.setStatus(newStatus);
                patientRepo.save(f);
            }
            default -> throw new IllegalArgumentException("Invalid category");
        }
    }

    private FeedbackDTO toDTO(Feedback f, String category) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(f.getId());
        dto.setName(f.getName());
        dto.setEmail(f.getEmail());
        dto.setContact(f.getContact());
        dto.setRating(f.getRating());
        dto.setMessage(f.getMessage());
        dto.setDate(f.getCreatedAt().toLocalDate());
        dto.setStatus(f.getStatus().name());
        dto.setCategory(category);
        return dto;
    }
}
