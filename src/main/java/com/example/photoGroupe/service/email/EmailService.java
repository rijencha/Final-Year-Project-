package com.example.photoGroupe.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendTestEmail(String toEmail) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("SMTP Test");
        message.setText("Spring Boot SMTP is working successfully!");

        mailSender.send(message);
    }
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset OTP - PhotoGroupe");
        message.setText(
                "Hello,\n\n" +
                        "Your OTP for password reset is:\n\n" +
                        "  " + otp + "\n\n" +
                        "This OTP is valid for 10 minutes.\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "— PhotoGroupe Team"
        );
        mailSender.send(message);
    }

    public void sendBookingRequestEmail(String toEmail, String clientName, String eventTitle, Long bookingId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("New Booking Request - PhotoGroupe");
        message.setText(
                "Hello,\n\n" +
                        clientName + " sent you a booking request for \"" + eventTitle + "\".\n\n" +
                        "Please log in to your dashboard to accept or decline it:\n" +
                        "/dashboard/bookings/" + bookingId + "\n\n" +
                        "— PhotoGroupe Team"
        );
        mailSender.send(message);
    }

    public void sendBookingConfirmedEmail(String toEmail, String photographerName, String eventTitle, Long bookingId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Booking Confirmed - PhotoGroupe");
        message.setText(
                "Hello,\n\n" +
                        photographerName + " confirmed your booking for \"" + eventTitle + "\".\n\n" +
                        "View details here:\n" +
                        "/my-bookings/" + bookingId + "\n\n" +
                        "— PhotoGroupe Team"
        );
        mailSender.send(message);
    }

    public void sendBookingRejectedEmail(String toEmail, String photographerName, String eventTitle, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Booking Declined - PhotoGroupe");
        message.setText(
                "Hello,\n\n" +
                        photographerName + " declined your booking request for \"" + eventTitle + "\"." +
                        (reason != null && !reason.isBlank() ? "\n\nReason: " + reason : "") + "\n\n" +
                        "— PhotoGroupe Team"
        );
        mailSender.send(message);
    }

    public void sendWorkshopRegistrationEmail(String toEmail, String participantName, String workshopTitle, Long workshopId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("New Workshop Registration - PhotoGroupe");
        message.setText(
                "Hello,\n\n" +
                        participantName + " registered for your workshop \"" + workshopTitle + "\".\n\n" +
                        "View participants here:\n" +
                        "/dashboard/workshops/" + workshopId + "\n\n" +
                        "— PhotoGroupe Team"
        );
        mailSender.send(message);
    }

    public void sendEscrowReleasedEmail(String toEmail, String eventTitle, String amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Payment Released - PhotoGroupe");
        message.setText(
                "Hello,\n\n" +
                        "Payment for \"" + eventTitle + "\" has been released to you.\n" +
                        "Amount transferred: NPR " + amount + "\n\n" +
                        "— PhotoGroupe Team"
        );
        mailSender.send(message);
    }

    public void sendWorkshopPayoutEmail(String toEmail, String workshopTitle, String amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Workshop Payment Received - PhotoGroupe");
        message.setText(
                "Hello,\n\n" +
                        "A participant has paid for your workshop \"" + workshopTitle + "\".\n" +
                        "Amount due to you: NPR " + amount + "\n\n" +
                        "— PhotoGroupe Team"
        );
        mailSender.send(message);
    }
}
