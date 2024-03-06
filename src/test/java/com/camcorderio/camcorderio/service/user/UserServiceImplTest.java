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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserInfoMapper mockUserInfoMapper;
    @Mock
    private UserInfoRepository mockUserInfoRepository;
    @Mock
    private WalletRepository mockWalletRepository;
    @Mock
    private OtpService mockOtpService;
    @Mock
    private AddressRepository mockAddressRepository;
    @Mock
    private BCryptPasswordEncoder mockPasswordEncoder;
    @Mock
    private WishListRepository mockWishListRepository;

    private UserServiceImpl userServiceImplUnderTest;

    @BeforeEach
    void setUp() {
        userServiceImplUnderTest = new UserServiceImpl(mockUserInfoMapper, mockUserInfoRepository, mockWalletRepository,
                mockOtpService, null, mockAddressRepository, mockPasswordEncoder,
                mockWishListRepository);
    }

    private UserInfo createUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("John");
        userInfo.setEmail("john@gmail.com");
        userInfo.setPassword("password");
        userInfo.setStatus(false);
        userInfo.setPhoneNumber("1234567890");
        Address address = new Address();
        address.setId(1L);
        address.setName("John's Address");
        address.setMobile("1234567890");
        address.setAddress("123 Main St");
        address.setCity("Cityville");
        address.setPin("12345");
        address.setState("State");
        address.setDeleted(false);
        userInfo.setUserAddresses(List.of(address));
        Wallet wallet = new Wallet();
        wallet.setTotalAmount(100.0);
        WalletHistory walletHistory = new WalletHistory();
        walletHistory.setAddedAmount(50.0);
        walletHistory.setAmountAddedTime("2024-03-05 10:00:00");
        walletHistory.setDepositOrWithdraw("Deposit");
        wallet.setWalletHistories(List.of(walletHistory));
        userInfo.setWallet(wallet);
        WishList wishList = new WishList();
        Product product = new Product();
        product.setName("Dummy Product");
        product.setPrice(20.0);
        wishList.setProducts(List.of(product));
        userInfo.setWishLists(List.of(wishList));
        userInfo.setDeleted(false);
        userInfo.setReferralCode("REF123");
        return userInfo;
    }

    private Wallet createWallet() {
        Wallet wallet = new Wallet();
        wallet.setTotalAmount(0.0);
        return wallet;
    }

    private WalletHistory createWalletHistory() {
        WalletHistory walletHistory = new WalletHistory();
        walletHistory.setAddedAmount(0.0);
        walletHistory.setAmountAddedTime("2024-03-05 10:00:00");
        walletHistory.setDepositOrWithdraw("Deposit");
        return walletHistory;
    }

    @Test
    void testEditUserInfo_UserInfoRepositoryFindByIdReturnsAbsent() {
        when(mockUserInfoRepository.findById(UUID.fromString("cf2fbeb6-87b7-409f-b684-86c19e61a856")))
                .thenReturn(Optional.empty());
        assertThatThrownBy(
                () -> userServiceImplUnderTest.editUserInfo(UUID.fromString("cf2fbeb6-87b7-409f-b684-86c19e61a856"),
                        "John", "1234567890")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testGetUserByEmail() {
        UserInfo userInfo = createUserInfo();
        when(mockUserInfoRepository.findByEmail("john@gmail.com")).thenReturn(userInfo);
        final UserInfo result = userServiceImplUnderTest.getUserByEmail("john@gmail.com");
        assertThat(result).isEqualTo(userInfo);
    }

    @Test
    void testEmailExist() {
        when(mockUserInfoRepository.existsByEmailIgnoreCase("john@gmail.com")).thenReturn(false);
        final boolean result = userServiceImplUnderTest.emailExist("john@gmail.com");
        assertThat(result).isFalse();
    }

    @Test
    void testEmailExist_UserInfoRepositoryReturnsTrue() {
        when(mockUserInfoRepository.existsByEmailIgnoreCase("john@gmail.com")).thenReturn(true);
        final boolean result = userServiceImplUnderTest.emailExist("john@gmail.com");
        assertThat(result).isTrue();
    }

    @Test
    void testGetAllUsers() {
        UserInfo userInfo = createUserInfo();
        final Page<UserInfo> userInfos = new PageImpl<>(List.of(userInfo));
        when(mockUserInfoRepository.findByRoleNot(eq("ADMIN"), any(PageRequest.class))).thenReturn(userInfos);
        final Page<UserInfo> result = userServiceImplUnderTest.getAllUsers(PageRequest.of(0, 1));
        assertThat(result).isEqualTo(userInfos);
    }

    @Test
    void testGetAllUsers_UserInfoRepositoryReturnsNoItems() {
        when(mockUserInfoRepository.findByRoleNot(eq("ADMIN"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        final Page<UserInfo> result = userServiceImplUnderTest.getAllUsers(PageRequest.of(0, 1));
        assertThat(result).isEmpty();
    }

    @Test
    void testResetPassword() {
        UserInfo userInfo = createUserInfo();
        when(mockUserInfoRepository.findByEmail("john@gmail.com")).thenReturn(userInfo);
        when(mockPasswordEncoder.encode("newPassword")).thenReturn("password");
        userServiceImplUnderTest.resetPassword("john@gmail.com", "newPassword");
        verify(mockUserInfoRepository).save(userInfo);
    }

    @Test
    void testResetPassword_UserInfoRepositoryFindByEmailReturnsNull() {
        when(mockUserInfoRepository.findByEmail("john@gmail.com")).thenReturn(null);
        assertThatThrownBy(() -> userServiceImplUnderTest.resetPassword("john@gmail.com", "newPassword"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void testFindReferral() {
        when(mockUserInfoRepository.existsByReferralCode("REF123")).thenReturn(false);
        final boolean result = userServiceImplUnderTest.findReferral("REF123");
        assertThat(result).isFalse();
    }

    @Test
    void testFindReferral_UserInfoRepositoryReturnsTrue() {
        when(mockUserInfoRepository.existsByReferralCode("REF123")).thenReturn(true);
        final boolean result = userServiceImplUnderTest.findReferral("REF123");
        assertThat(result).isTrue();
    }

    @Test
    void testAddAmountForReferral_UserInfoRepositoryReturnsNull() {
        when(mockUserInfoRepository.findByReferralCode("REF123")).thenReturn(null);
        userServiceImplUnderTest.addAmountForReferral("REF123");
    }

    @Test
    void testIsPhoneNumberValid() {
        assertThat(userServiceImplUnderTest.isPhoneNumberValid("1234567890")).isTrue();
    }

}
