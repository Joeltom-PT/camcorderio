package com.camcorderio.camcorderio.service.admin;

import com.camcorderio.camcorderio.entity.admin.Banner;
import com.camcorderio.camcorderio.repository.BannerRepository;
import com.camcorderio.camcorderio.service.admin.BannerService;
import com.uploadcare.api.Client;
import com.uploadcare.upload.FileUploader;
import com.uploadcare.upload.UploadFailureException;
import com.uploadcare.upload.Uploader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    @Value("${uploadcare.publicKey}")
    private String uploadcarePublicKey;

    public BannerServiceImpl(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @Override
    public boolean addBanner(String url, MultipartFile file) {
        try {

            Client client = Client.demoClient();

            // Convert MultipartFile to File
            File sourceFile = convertMultiPartToFile(file);

            // Create a FileUploader instance
            Uploader uploader = new FileUploader(client, sourceFile);

            // Upload the file and save it
            com.uploadcare.api.File uploadedFile = uploader.upload().save();

            // Save banner details to database
            Banner banner = new Banner();
            banner.setImagePath(uploadedFile.getOriginalFileUrl().toString());
            banner.setUrl(url);
            bannerRepository.save(banner);

            return true;
        } catch (UploadFailureException | IOException e) {
            // Handle upload failure
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }

    @Override
    public boolean deleteBanner(long id) {
        try {
            bannerRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateBanner(long id, String url) {
        Optional<Banner> optionalBanner = bannerRepository.findById(id);
        if (optionalBanner.isPresent()) {
            Banner banner = optionalBanner.get();
            banner.setUrl(url);
            bannerRepository.save(banner);
            return true;
        } else {
            return false;
        }
    }


    // Helper method to convert MultipartFile to File
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        // Create a temporary directory to save the uploaded file
        File tempDir = new File(System.getProperty("java.io.tmpdir"));

        // Create a new File object with a unique name in the temporary directory
        File convFile = File.createTempFile("upload-", "-" + file.getOriginalFilename(), tempDir);

        // Transfer the uploaded file to the created File object
        file.transferTo(convFile);

        return convFile;
    }
}
