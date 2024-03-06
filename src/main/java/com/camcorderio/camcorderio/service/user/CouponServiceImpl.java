package com.camcorderio.camcorderio.service.user;

import com.camcorderio.camcorderio.entity.user.Coupon;
import com.camcorderio.camcorderio.entity.user.UserCoupon;
import com.camcorderio.camcorderio.entity.user.UserInfo;
import com.camcorderio.camcorderio.model.product.CategoryDto;
import com.camcorderio.camcorderio.model.user.CouponDto;
import com.camcorderio.camcorderio.repository.CouponRepository;
import com.camcorderio.camcorderio.repository.OrderItemRepository;
import com.camcorderio.camcorderio.repository.UserCouponRepository;
import com.camcorderio.camcorderio.repository.UserInfoRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CouponServiceImpl implements CouponService{
    private final CouponRepository couponRepository;

    private final UserInfoRepository userInfoRepository;

    private final OrderItemRepository orderItemRepository;

    private final UserCouponRepository userCouponRepository;

    public CouponServiceImpl(CouponRepository couponRepository, UserInfoRepository userInfoRepository, OrderItemRepository orderItemRepository, UserCouponRepository userCouponRepository) {
        this.couponRepository = couponRepository;
        this.userInfoRepository = userInfoRepository;
        this.orderItemRepository = orderItemRepository;
        this.userCouponRepository = userCouponRepository;
    }

    @Override
    public Coupon addCoupon(CouponDto couponDto) {
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(couponDto, coupon);
        coupon.setCouponCode(generateCouponCode());
        return couponRepository.save(coupon);
    }

    @Override
    public boolean isCouponNameExists(String couponName) {
        return couponRepository.existsByName(couponName);
    }

    @Override
    public boolean isCouponNameExistsNotId(String couponName, long excludeId) {
        return couponRepository.existsByNameAndIdNot(couponName, excludeId);
    }

    @Override
    public List<CouponDto> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAllByIsDeletedFalse();
        return coupons.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    @Override
    public CouponDto getCouponById(long id) {
        Optional<Coupon> optionalCoupon = couponRepository.findById(id);
        return optionalCoupon.map(this::convertToDto).orElse(null);
    }

    public Coupon updateCoupon(CouponDto couponDto) {
        Optional<Coupon> optionalCoupon = couponRepository.findById(couponDto.getId());
        if (optionalCoupon.isPresent()) {
            Coupon existingCoupon = optionalCoupon.get();
            existingCoupon.setName(couponDto.getName());
            existingCoupon.setAmount(couponDto.getAmount());
            existingCoupon.setMinimumOrderAmount(couponDto.getMinimumOrderAmount());
            existingCoupon.setStartDate(couponDto.getStartDate());
            existingCoupon.setExpiryDate(couponDto.getExpiryDate());
            existingCoupon.setCouponDescription(couponDto.getCouponDescription());
            return couponRepository.save(existingCoupon);
        } else {
            return null;
        }
    }

    @Override
    public CouponDto getCouponByCouponCode(String couponCode) {
        Coupon coupon = couponRepository.findByCouponCode(couponCode);
        if (coupon != null) {
            return convertToDto(coupon);
        } else {
            return null;
        }
    }

    @Override
    public boolean isCouponAlreadyUsedByUser(String userEmail, long couponId) {
        UserInfo user = userInfoRepository.findByEmail(userEmail);
        if (user == null) {
            return false;
        }
        UUID userId = user.getUserId();
        return userCouponRepository.existsByUserUserIdAndCouponId(userId, couponId);
    }

    @Override
    public boolean isCouponExpired(long id) {
        Optional<Coupon> optionalCoupon = couponRepository.findById(id);
        if (optionalCoupon.isPresent()) {
            Coupon coupon = optionalCoupon.get();
            LocalDate expiryDate = coupon.getExpiryDate();
            LocalDate currentDate = LocalDate.now();
            return expiryDate != null && expiryDate.isBefore(currentDate);
        } else {
            return false;
        }
    }

    @Override
    public boolean isCouponStarted(long id) {
        Optional<Coupon> optionalCoupon = couponRepository.findById(id);
        if (optionalCoupon.isPresent()) {
            Coupon coupon = optionalCoupon.get();
            LocalDate startDate = coupon.getStartDate();
            LocalDate currentDate = LocalDate.now();
            return startDate != null && !currentDate.isBefore(startDate);
        } else {
            return false;
        }
    }

    @Override
    public List<CouponDto> getAllCouponsForUser(String userEmail) {
        UserInfo user = userInfoRepository.findByEmail(userEmail);

        if (user == null) {
            return Collections.emptyList();
        }

        List<UserCoupon> userCoupons = user.getUserCoupons();
        Set<Long> usedCouponIds = userCoupons.stream()
                .map(userCoupon -> userCoupon.getCoupon().getId())
                .collect(Collectors.toSet());

        LocalDate currentDate = LocalDate.now();

        List<Coupon> allCoupons = couponRepository.findAllByExpiryDateAfterAndIsDeletedFalse(currentDate); // Add this line
        List<CouponDto> couponDtos = new ArrayList<>();

        for (Coupon coupon : allCoupons) {
            CouponDto couponDto = new CouponDto();
            couponDto.setId(coupon.getId());
            couponDto.setName(coupon.getName());
            couponDto.setAmount(coupon.getAmount());
            couponDto.setMinimumOrderAmount(coupon.getMinimumOrderAmount());
            couponDto.setCouponCode(coupon.getCouponCode());
            couponDto.setCouponDescription(coupon.getCouponDescription());
            couponDto.setStartDate(coupon.getStartDate());
            couponDto.setExpiryDate(coupon.getExpiryDate());
            couponDto.setDeleted(coupon.isDeleted());

            couponDto.setUsed(usedCouponIds.contains(coupon.getId()));

            couponDtos.add(couponDto);
        }

        return couponDtos;
    }

    @Override
    public void couponUsed(long id, String userEmail) {
        UserInfo user = userInfoRepository.findByEmail(userEmail);

        if (user == null) {
            return;
        }

        Optional<Coupon> optionalCoupon = couponRepository.findById(id);

        if (!optionalCoupon.isPresent()) {
            return;
        }

        Coupon coupon = optionalCoupon.get();

        boolean isCouponAlreadyUsedByUser = isCouponAlreadyUsedByUser(user.getEmail(), coupon.getId());

        if (!isCouponAlreadyUsedByUser) {
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUser(user);
            userCoupon.setCoupon(coupon);
            userCouponRepository.save(userCoupon);
        }
    }

    @Override
    public boolean deleteCoupon(long id) {
        Optional<Coupon> couponOptional = couponRepository.findById(id);
        if (couponOptional.isPresent()) {
            Coupon coupon = couponOptional.get();
            coupon.setDeleted(true);
            couponRepository.save(coupon);
            return true;
        } else {
            return false;
        }
    }



    private CouponDto convertToDto(Coupon coupon) {
        CouponDto couponDto = new CouponDto();
        BeanUtils.copyProperties(coupon, couponDto);
        return couponDto;
    }

    private String generateCouponCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder couponCode = new StringBuilder();
        Random rnd = new Random();

        for (int i = 0; i < 12; i++) {
            couponCode.append(characters.charAt(rnd.nextInt(characters.length())));
        }
        return couponCode.toString();
    }


}
