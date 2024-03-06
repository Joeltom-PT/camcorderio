package com.camcorderio.camcorderio.service.verification;

import com.camcorderio.camcorderio.model.user.UserInfoDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {
    private final JavaMailSender mailSender;
    private final Map<String, OtpSessionData> otpSessions = new HashMap<>();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 5;

    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void generateAndSendOtp(String email, HttpSession session) {
        String otp = generateOtp();
        sendOtpByEmail(email, otp);

        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);
        OtpSessionData otpSessionData = new OtpSessionData(otp, email, expirationTime);
        otpSessions.put(session.getId(), otpSessionData);
    }

    public void verifyOtp(String userEnteredOtp, HttpSession session) throws OtpTimeoutException, OtpVerificationException {
        OtpSessionData otpSessionData = otpSessions.get(session.getId());

        if (otpSessionData == null) {
            throw new OtpVerificationException("OTP session not found. Please request a new OTP.");
        }

        if (otpSessionData.isExpired()) {
            otpSessions.remove(session.getId());
            throw new OtpTimeoutException("OTP has expired. Please request a new OTP.");
        }

        if (!otpSessionData.getOtp().equals(userEnteredOtp)) {
            throw new OtpVerificationException("Incorrect OTP. Please enter the correct OTP.");
        }

        otpSessions.remove(session.getId());
    }

    public void resendOtp(HttpSession session) throws OtpVerificationException {
        UserInfoDTO userDto = (UserInfoDTO) session.getAttribute("userDto");

        if (userDto != null) {

            generateAndSendOtp(userDto.getEmail(), session);
        } else {

            throw new OtpVerificationException("User information not found. Please start the signup process again.");
        }
    }

    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }

    private void sendOtpByEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("OTP for Verification");
        message.setText("Your OTP for verification is: " + otp);

        mailSender.send(message);
    }

    public static class OtpSessionData {
        private final String otp;
        private final String email;
        private final LocalDateTime expirationTime;

        public OtpSessionData(String otp, String email, LocalDateTime expirationTime) {
            this.otp = otp;
            this.email = email;
            this.expirationTime = expirationTime;
        }

        public String getOtp() {
            return otp;
        }

        public String getEmail() {
            return email;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expirationTime);
        }
    }

    public static class OtpTimeoutException extends Exception {
        public OtpTimeoutException(String message) {
            super(message);
        }
    }

    public static class OtpVerificationException extends Exception {
        public OtpVerificationException(String message) {
            super(message);
        }
    }
}