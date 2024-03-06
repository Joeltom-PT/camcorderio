package com.camcorderio.camcorderio.service.user;

import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.entity.user.*;
import com.camcorderio.camcorderio.exceptions.AddressNotFoundException;
import com.camcorderio.camcorderio.model.user.AddressDto;
import com.camcorderio.camcorderio.model.user.UserInfoDTO;
import com.camcorderio.camcorderio.model.user.UserInfoMapper;
import com.camcorderio.camcorderio.repository.AddressRepository;
import com.camcorderio.camcorderio.repository.UserInfoRepository;
import com.camcorderio.camcorderio.repository.WalletRepository;
import com.camcorderio.camcorderio.repository.WishListRepository;
import com.camcorderio.camcorderio.service.verification.OtpService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserInfoService {

    private  UserInfoMapper userInfoMapper;
    private  UserInfoRepository userInfoRepository;

    private final WalletRepository walletRepository;
    private OtpService otpService;

    private final SessionRegistry sessionRegistry;

    private final AddressRepository addressRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final WishListRepository wishListRepository;

    @Autowired
    public UserServiceImpl(UserInfoMapper userInfoMapper, UserInfoRepository userInfoRepository, WalletRepository walletRepository, OtpService otpService, SessionRegistry sessionRegistry, AddressRepository addressRepository, BCryptPasswordEncoder passwordEncoder, WishListRepository wishListRepository) {
        this.userInfoMapper = userInfoMapper;
        this.userInfoRepository = userInfoRepository;
        this.walletRepository = walletRepository;
        this.sessionRegistry = sessionRegistry;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.wishListRepository = wishListRepository;
    }

    @Override
    public void saveUser(UserInfoDTO userDTO) {
        UserInfo user=userInfoMapper.userInfoDTOtoUserInfo(userDTO);
        String encodedPass = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPass);
        user.setReferralCode(generateReferral(12));
        userInfoRepository.save(user);

        Wallet newWallet = new Wallet();
        newWallet.setUser(user);
        newWallet.setTotalAmount(0.0);
        walletRepository.save(newWallet);

        WishList initialWishList = new WishList();
        initialWishList.setUser(user);
        wishListRepository.save(initialWishList);

        user.getWishLists().add(initialWishList);
        user.setWallet(newWallet);
        userInfoRepository.save(user);
    }

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase(Locale.ENGLISH);
    private static final String NUMBER = "0123456789";
    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static SecureRandom random = new SecureRandom();

    public static String generateReferral(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            sb.append(DATA_FOR_RANDOM_STRING.charAt(randomIndex));
        }
        return sb.toString();
    }


    @Override
    public UserInfo editUserInfo(UUID userId, String newUsername, String newPhoneNumber) {
        Optional<UserInfo> optionalUserInfo = userInfoRepository.findById(userId);

        if (optionalUserInfo.isPresent()) {
            UserInfo userInfo = optionalUserInfo.get();

            if (newUsername != null && newUsername.length() >= 2) {
                userInfo.setUsername(newUsername);
            } else {
                throw new RuntimeException("Username must be at least 2 characters long.");
            }

            if (validatePhoneNumber(newPhoneNumber)) {
                userInfo.setPhoneNumber(newPhoneNumber);
            } else {
                throw new RuntimeException("Phone number must be 10 digits long.");
            }

            return userInfoRepository.save(userInfo);
        } else {
            throw new RuntimeException("User with ID " + userId + " not found");
        }
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        return phoneNumber.length() == 10 && phoneNumber.matches("\\d+");
    }

    @Override
    public UserInfo getUserByEmail(String email) {
        return userInfoRepository.findByEmail(email);
    }

    @Override
    public String generateOtp() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }

    @Override
    public boolean emailExist(String email) {
        return userInfoRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Page<UserInfo> getAllUsers(Pageable pageable) {
        return userInfoRepository.findByRoleNot("ADMIN", pageable);
    }

    public void resetPassword(String email, String newPassword) {

        UserInfo user = userInfoRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userInfoRepository.save(user);
    }

    @Override
    public UserInfo toggleUserStatus(UUID userId) {
        Optional<UserInfo> optionalUserInfo = userInfoRepository.findById(userId);
        if (optionalUserInfo.isPresent()) {
            UserInfo userInfo = optionalUserInfo.get();
            userInfo.setStatus(!userInfo.isStatus());

            if (!userInfo.isStatus()) {
                invalidateUserSessions(userInfo.getEmail());
            }

            return userInfoRepository.save(userInfo);
        }
        return null;
    }

    @Override
    public Page<UserInfo> searchUsers(String query , Pageable pageable) {
        String role = "ADMIN";
        return userInfoRepository.findByRoleNotAndEmailContainingIgnoreCase(role, query, pageable);
    }

    @Override
    public void addAddress(String name, AddressDto addressDto) {
        UserInfo user =  userInfoRepository.findByEmail(name);

        Address address = new Address();
        address.setAddress(addressDto.getAddress());
        address.setName(addressDto.getName());
        address.setCity(addressDto.getCity());
        address.setMobile(addressDto.getMobile());
        address.setPin(addressDto.getPin());
        address.setState(addressDto.getState());

        user.getUserAddresses().add(address);

        userInfoRepository.save(user);

    }

    public List<AddressDto> getUserAddressByEmail(String email) {
        UserInfo user = userInfoRepository.findByEmail(email);

        if (user != null && !user.isDeleted()) {
            List<AddressDto> addressDtos = user.getUserAddresses().stream()
                    .filter(address -> !address.isDeleted())
                    .map(address -> new AddressDto(
                            address.getId(),
                            address.getName(),
                            address.getMobile(),
                            address.getAddress(),
                            address.getCity(),
                            address.getPin(),
                            address.getState()
                    ))
                    .collect(Collectors.toList());

            return addressDtos;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @Override
    @Transactional
    public UserInfo currentUserDetail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return userInfoRepository.findByEmail(userEmail);
    }

    @Override
    public AddressDto getAddressByIdAndUser(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        UserInfo user = userInfoRepository.findByEmail(userEmail);
        if (user != null) {
            Optional<Address> addressEntityOptional = user.getUserAddresses().stream()
                    .filter(address -> address.getId().equals(id))
                    .findFirst();

            return addressEntityOptional.map(Address::toDto).orElse(null);
        }

        return null;
    }

    @Override
    public void updateAddress(long id, AddressDto updatedAddress) {
        Optional<Address> addressEntityOptional = addressRepository.findById(id);

        if (addressEntityOptional.isPresent()) {
            Address existingAddress = addressEntityOptional.get();

            existingAddress.updateFromDto(updatedAddress);

            addressRepository.save(existingAddress);
        } else {
            throw new AddressNotFoundException("Address with ID " + id + " not found");
        }
    }

    @Override
    public void deleteAddress(long id) {

        Optional<Address> optionalAddress = addressRepository.findById(id);

        optionalAddress.ifPresent(address -> {
            address.setDeleted(true);
            addressRepository.save(address);
        });
    }

    @Override
    public boolean isUserAddressAvailableByEmail(String userEmail, Long selectedAddressId) {
        UserInfo user = userInfoRepository.findByEmail(userEmail);
        return user != null && user.getUserAddresses().stream()
                .anyMatch(address -> address.getId().equals(selectedAddressId));
    }

    @Override
    @Transactional
    public void addToWishlist(UserInfo user, Product product) {
        Optional<WishList> wishListOptional = wishListRepository.findByUser(user);

        WishList wishList = wishListOptional.orElseGet(() -> {
            WishList newWishList = new WishList();
            newWishList.setUser(user);
            return newWishList;
        });

        if (!wishList.getProducts().contains(product)) {
            wishList.getProducts().add(product);
            wishListRepository.save(wishList);
        }
    }

    public void removeFromWishlist(UserInfo user, Product product) {
        Optional<WishList> wishListOptional = wishListRepository.findByUser(user);

        wishListOptional.ifPresent(wishList -> {
            wishList.getProducts().remove(product);
            wishListRepository.save(wishList);
        });
    }

    @Override
    @Transactional
    public List<Product> getWishlistItems(String userEmail) {
        UserInfo user = userInfoRepository.findByEmail(userEmail);
        if (user != null) {
            WishList wishList = wishListRepository.findByUser(user).orElse(new WishList());

            Hibernate.initialize(wishList.getProducts());

            return wishList.getProducts();
        }
        return List.of();
    }

    @Override
    public boolean findReferral(String referral) {
        return userInfoRepository.existsByReferralCode(referral);
    }

    @Override
    public void addAmountForReferral(String referral) {
        UserInfo user = userInfoRepository.findByReferralCode(referral);

        if (user != null && user.getWallet() != null) {

            Wallet wallet = user.getWallet();

            double currentAmount = wallet.getTotalAmount();
            wallet.setTotalAmount(currentAmount + 10);
            walletRepository.save(wallet);

            WalletHistory history = new WalletHistory();
            history.setAddedAmount(10);
            history.setAmountAddedTime(LocalDateTime.now().toString());
            history.setDepositOrWithdraw("Deposit");
            history.setWallet(wallet);
            wallet.getWalletHistories().add(history);
        }
    }

    public boolean isUsernameValid(String username) {
        return username != null && username.length() >= 2;
    }

    public boolean isPhoneNumberValid(String phoneNumber) {
        String phoneNumberStr = String.valueOf(phoneNumber);
        return phoneNumberStr != null && phoneNumberStr.matches("\\d{10}");
    }

    private void invalidateUserSessions(String email) {
        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();

        for (Object principal : allPrincipals) {
            if (principal instanceof UserDetails userDetails) {
                if (userDetails.getUsername().equals(email)) {
                    List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);

                    for (SessionInformation session : sessions) {
                        session.expireNow();
                    }
                }
            }
        }
    }

}