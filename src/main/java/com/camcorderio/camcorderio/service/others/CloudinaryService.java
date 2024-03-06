//package com.camcorderio.camcorderio.service.others;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.Formatter;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class CloudinaryService {
//
//    private final Cloudinary cloudinary;
//    private final String apiKey;
//    private final String apiSecret;
//
//    public CloudinaryService(@Value("${cloudinary.cloud_name}") String cloudName,
//                             @Value("${cloudinary.api_key}") String apiKey,
//                             @Value("${cloudinary.api_secret}") String apiSecret) {
//        this.apiKey = apiKey;
//        this.apiSecret = apiSecret;
//        cloudinary = new Cloudinary(ObjectUtils.asMap(
//                "cloud_name", cloudName,
//                "api_key", apiKey,
//                "api_secret", apiSecret));
//    }
//
//    public String uploadImage(MultipartFile file) throws IOException {
//        try {
//            long timestamp = System.currentTimeMillis() / 1000L; // Unix time in seconds
//            String signature = generateSignature(cloudinary.config.cloudName, apiKey, apiSecret, timestamp);
//
//            Map<String, Object> uploadOptions = new HashMap<>();
//            uploadOptions.put("timestamp", timestamp);
//            uploadOptions.put("signature", signature);
//
//            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
//            return (String) uploadResult.get("secure_url");
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            // Handle the exception
//            return null;
//        }
//    }
//
//    private String generateSignature(String cloudName, String apiKey, String apiSecret, long timestamp) throws NoSuchAlgorithmException {
//        String toSign = "cloud_name=" + cloudName + "&api_key=" + apiKey + "&timestamp=" + timestamp;
//        String toSignWithSecret = toSign + apiSecret;
//
//        MessageDigest md = MessageDigest.getInstance("SHA-1");
//        byte[] digest = md.digest(toSignWithSecret.getBytes());
//
//        // Convert the byte array to a hexadecimal string
//        Formatter formatter = new Formatter();
//        for (byte b : digest) {
//            formatter.format("%02x", b);
//        }
//
//        return formatter.toString();
//    }
//}
