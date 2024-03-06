package com.camcorderio.camcorderio.service.user;

import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.entity.user.UserInfo;
import com.camcorderio.camcorderio.model.user.AddressDto;
import com.camcorderio.camcorderio.model.user.UserInfoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserInfoService {

    public void saveUser(UserInfoDTO user);

    public UserInfo editUserInfo(UUID userId, String newUsername, String newPhoneNumber);

    public UserInfo getUserByEmail(String email);

    public String generateOtp();

    public  boolean emailExist(String email);

   public Page<UserInfo> getAllUsers(Pageable pageable);

   public UserInfo toggleUserStatus(UUID userId);

  public   Page<UserInfo> searchUsers(String query , Pageable pageable);

   public void addAddress(String name, AddressDto addressDto);

   public List<AddressDto> getUserAddressByEmail(String name);


   public String currentUser();

    public UserInfo currentUserDetail();

    boolean isUserAddressAvailableByEmail(String userEmail, Long selectedAddressId);

    boolean isUsernameValid(String username);

    boolean isPhoneNumberValid(String phoneNumber);

    public AddressDto getAddressByIdAndUser(long id);

    void updateAddress(long id, AddressDto updatedAddress);

    void deleteAddress(long id);

    void addToWishlist(UserInfo user, Product product);

    void removeFromWishlist(UserInfo user, Product product);

    List<Product> getWishlistItems(String userEmail);

    boolean findReferral(String referral);

    void addAmountForReferral(String referral);

//    void deleteAddress(String id, String userEmail);
}
