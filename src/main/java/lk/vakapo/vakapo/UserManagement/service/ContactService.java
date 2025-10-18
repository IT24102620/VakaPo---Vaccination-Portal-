package lk.vakapo.vakapo.UserManagement.service;

import lk.vakapo.vakapo.UserManagement.model.ContactInfo;
import lk.vakapo.vakapo.UserManagement.repository.ContactInfoRepository;
import lk.vakapo.vakapo.MailManagement.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {
    
    private final ContactInfoRepository contactInfoRepository;
    private final EmailService emailService;
    
    /**
     * Submit a new contact form
     */
    @Transactional
    public ContactInfo submitContactForm(String fullName, String emailAddress, String phoneNumber, String message) {
        try {
            log.info("Submitting contact form for: {}", emailAddress);
            
            ContactInfo contactInfo = new ContactInfo(fullName, emailAddress, phoneNumber, message);
            
            ContactInfo savedContact = contactInfoRepository.save(contactInfo);
            log.info("Contact form submitted successfully with ID: {}", savedContact.getId());
            
            // Send confirmation email to the user
            try {
                sendContactConfirmationEmail(fullName, emailAddress, message);
                log.info("Confirmation email sent to: {}", emailAddress);
            } catch (Exception emailException) {
                log.error("Failed to send confirmation email to: {}", emailAddress, emailException);
                // Don't fail the entire operation if email fails
            }
            
            return savedContact;
        } catch (Exception e) {
            log.error("Error submitting contact form for email: {}", emailAddress, e);
            throw new RuntimeException("Failed to submit contact form", e);
        }
    }
    
    /**
     * Get all contact submissions
     */
    public List<ContactInfo> getAllContacts() {
        return contactInfoRepository.findAll();
    }
    
    /**
     * Get contacts by status
     */
    public List<ContactInfo> getContactsByStatus(String status) {
        return contactInfoRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    /**
     * Get pending contacts
     */
    public List<ContactInfo> getPendingContacts() {
        return contactInfoRepository.findByStatusOrderByCreatedAtDesc("pending");
    }
    
    /**
     * Get contact by ID
     */
    public Optional<ContactInfo> getContactById(Long id) {
        return contactInfoRepository.findById(id);
    }
    
    /**
     * Update contact status
     */
    @Transactional
    public ContactInfo updateContactStatus(Long id, String status) {
        try {
            Optional<ContactInfo> contactOpt = contactInfoRepository.findById(id);
            if (contactOpt.isEmpty()) {
                throw new RuntimeException("Contact not found with ID: " + id);
            }
            
            ContactInfo contact = contactOpt.get();
            contact.setStatus(status);
            
            ContactInfo updatedContact = contactInfoRepository.save(contact);
            log.info("Contact status updated for ID: {} to status: {}", id, status);
            
            return updatedContact;
        } catch (Exception e) {
            log.error("Error updating contact status for ID: {}", id, e);
            throw new RuntimeException("Failed to update contact status", e);
        }
    }
    
    /**
     * Update contact with response from admin
     */
    @Transactional
    public ContactInfo updateContactWithResponse(Long id, String status, String response, String respondedBy) {
        try {
            Optional<ContactInfo> contactOpt = contactInfoRepository.findById(id);
            if (contactOpt.isEmpty()) {
                throw new RuntimeException("Contact not found with ID: " + id);
            }
            
            ContactInfo contact = contactOpt.get();
            contact.setStatus(status);
            contact.setResponse(response);
            contact.setRespondedBy(respondedBy);
            contact.setRespondedAt(LocalDateTime.now());
            
            ContactInfo updatedContact = contactInfoRepository.save(contact);
            log.info("Contact updated with response for ID: {} by: {}", id, respondedBy);
            
            // Send response email to the user
            if (response != null && !response.trim().isEmpty()) {
                try {
                    sendContactResponseEmail(contact.getFullName(), contact.getEmailAddress(), response, respondedBy);
                    log.info("Response email sent to: {}", contact.getEmailAddress());
                } catch (Exception emailException) {
                    log.error("Failed to send response email to: {}", contact.getEmailAddress(), emailException);
                    // Don't fail the entire operation if email fails
                }
            }
            
            return updatedContact;
        } catch (Exception e) {
            log.error("Error updating contact with response for ID: {}", id, e);
            throw new RuntimeException("Failed to update contact with response", e);
        }
    }
    
    /**
     * Get recent contacts (last 30 days)
     */
    public List<ContactInfo> getRecentContacts() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return contactInfoRepository.findRecentContacts(thirtyDaysAgo);
    }
    
    /**
     * Get contacts by date range
     */
    public List<ContactInfo> getContactsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return contactInfoRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    /**
     * Search contacts by name or email
     */
    public List<ContactInfo> searchContacts(String searchTerm) {
        return contactInfoRepository.searchByNameOrEmail(searchTerm);
    }
    
    /**
     * Get paginated contacts
     */
    public Page<ContactInfo> getContactsPage(Pageable pageable) {
        return contactInfoRepository.findAll(pageable);
    }
    
    /**
     * Get paginated contacts by status
     */
    public Page<ContactInfo> getContactsByStatusPage(String status, Pageable pageable) {
        return contactInfoRepository.findByStatus(status, pageable);
    }
    
    /**
     * Get contact statistics
     */
    public long getContactCountByStatus(String status) {
        return contactInfoRepository.countByStatus(status);
    }
    
    /**
     * Get contact statistics for date range
     */
    public long getContactCountByStatusAndDateRange(String status, LocalDateTime startDate) {
        return contactInfoRepository.countByStatusAndDateRange(status, startDate);
    }
    
    /**
     * Delete contact
     */
    @Transactional
    public void deleteContact(Long id) {
        try {
            if (!contactInfoRepository.existsById(id)) {
                throw new RuntimeException("Contact not found with ID: " + id);
            }
            
            contactInfoRepository.deleteById(id);
            log.info("Contact deleted with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting contact with ID: {}", id, e);
            throw new RuntimeException("Failed to delete contact", e);
        }
    }
    
    /**
     * Send confirmation email when contact form is submitted
     */
    private void sendContactConfirmationEmail(String fullName, String emailAddress, String message) {
        try {
            String subject = "Thank you for contacting VakaPo - We've received your message";
            
            String emailBody = buildContactConfirmationEmailBody(fullName, message);
            
            emailService.sendSimpleEmail(emailAddress, subject, emailBody);
            log.info("Contact confirmation email sent to: {}", emailAddress);
        } catch (Exception e) {
            log.error("Error sending contact confirmation email to: {}", emailAddress, e);
            throw e;
        }
    }
    
    /**
     * Send response email when admin responds to contact
     */
    private void sendContactResponseEmail(String fullName, String emailAddress, String response, String respondedBy) {
        try {
            String subject = "Response from VakaPo Support Team";
            
            String emailBody = buildContactResponseEmailBody(fullName, response, respondedBy);
            
            emailService.sendSimpleEmail(emailAddress, subject, emailBody);
            log.info("Contact response email sent to: {}", emailAddress);
        } catch (Exception e) {
            log.error("Error sending contact response email to: {}", emailAddress, e);
            throw e;
        }
    }
    
    /**
     * Build confirmation email body
     */
    private String buildContactConfirmationEmailBody(String fullName, String message) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #007bff;">VakaPo</h1>
                        <p style="color: #666; font-size: 14px;">Making Vaccination Booking Simple & Secure</p>
                    </div>
                    
                    <h2 style="color: #007bff;">Thank you for contacting us!</h2>
                    
                    <p>Dear %s,</p>
                    
                    <p>We have successfully received your message and our support team will review it shortly.</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #007bff; margin: 20px 0;">
                        <p><strong>Your Message:</strong></p>
                        <p style="font-style: italic;">"%s"</p>
                    </div>
                    
                    <p>We typically respond within 24-48 hours during business days. If your inquiry is urgent, please don't hesitate to contact us directly.</p>
                    
                    <p>Thank you for choosing VakaPo for your vaccination needs!</p>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #666; font-size: 12px;">
                        <p>This is an automated message. Please do not reply to this email.</p>
                        <p>© 2025 VakaPo. All Rights Reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, fullName, message);
    }
    
    /**
     * Build response email body
     */
    private String buildContactResponseEmailBody(String fullName, String response, String respondedBy) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #007bff;">VakaPo</h1>
                        <p style="color: #666; font-size: 14px;">Making Vaccination Booking Simple & Secure</p>
                    </div>
                    
                    <h2 style="color: #007bff;">Response from our Support Team</h2>
                    
                    <p>Dear %s,</p>
                    
                    <p>Thank you for contacting VakaPo. Here is our response to your inquiry:</p>
                    
                    <div style="background-color: #f8f9fa; padding: 20px; border-left: 4px solid #28a745; margin: 20px 0;">
                        <p><strong>Our Response:</strong></p>
                        <p>%s</p>
                    </div>
                    
                    <p>If you have any further questions or need additional assistance, please don't hesitate to contact us again.</p>
                    
                    <p>Best regards,<br>
                    <strong>%s</strong><br>
                    VakaPo Support Team</p>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #666; font-size: 12px;">
                        <p>This message was sent by our support team. You can reply to this email if you need further assistance.</p>
                        <p>© 2025 VakaPo. All Rights Reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, fullName, response, respondedBy);
    }
}
