package com.camcorderio.camcorderio.service.admin;

import com.camcorderio.camcorderio.entity.admin.Banner;
import com.camcorderio.camcorderio.model.product.BrandDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BannerService {

    boolean addBanner(String url, MultipartFile file);


    List<Banner> getAllBanners();

    boolean deleteBanner(long id);

    boolean updateBanner(long id, String url);
}
