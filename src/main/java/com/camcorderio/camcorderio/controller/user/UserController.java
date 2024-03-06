package com.camcorderio.camcorderio.controller.user;

import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.entity.user.Cart;
import com.camcorderio.camcorderio.entity.user.UserInfo;
import com.camcorderio.camcorderio.entity.user.Wallet;
import com.camcorderio.camcorderio.exceptions.AddressNotFoundException;
import com.camcorderio.camcorderio.model.user.AddressDto;
import com.camcorderio.camcorderio.model.user.CartItemDTO;
import com.camcorderio.camcorderio.model.user.CouponDto;
import com.camcorderio.camcorderio.service.product.CartService;
import com.camcorderio.camcorderio.service.user.CouponService;
import com.camcorderio.camcorderio.service.user.UserInfoService;
import com.camcorderio.camcorderio.service.user.WalletService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
public class UserController {

    private final UserInfoService userInfoService;
    private final CartService cartService;
    private final CouponService couponService;
    private final WalletService walletService;

    public UserController(UserInfoService userInfoService, CartService cartService, CouponService couponService, WalletService walletService) {
        this.userInfoService = userInfoService;
        this.cartService = cartService;
        this.couponService = couponService;
        this.walletService = walletService;
    }

    private String redirectUserPage(Principal principal) {
        if (principal != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            if (roles.contains("ADMIN")) {
                return "redirect:/admin/dashboard";
            } else if (roles.contains("USER")) {

                return null;
            }
        }

        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profilePage(Principal principal, Model model) {
        String redirect = redirectUserPage(principal);

        if (redirect != null) {
            return redirect;
        }

        model.addAttribute("user",userInfoService.getUserByEmail(principal.getName()));

        return "user/profile";
    }

    @GetMapping("/user/address")
    public String addressPage(Model model, Principal principal){

        List<AddressDto> addressDtoList =  userInfoService.getUserAddressByEmail(principal.getName());

        model.addAttribute("allAddress", addressDtoList);
        model.addAttribute("user",userInfoService.getUserByEmail(principal.getName()));

        return "user/address/list";
    }

    @GetMapping("/user/address/add")
    public String addAddress(Model model,AddressDto addressDto){

        model.addAttribute("address", addressDto);

        return "user/address/add";
    }

    @PostMapping("/user/address/add")
    public String addAddress(
            Principal principal,
            @ModelAttribute("address") AddressDto addressDto,
            RedirectAttributes attributes
    ) {
        boolean validationError = validateAddress(addressDto, attributes);

        if (validationError) {
            return "redirect:/user/address/add";
        }

        userInfoService.addAddress(principal.getName(), addressDto);
        return "redirect:/user/address";
    }

    @GetMapping("/user/address/edit/{id}")
    public String editAddress(@PathVariable long id, Model model, Principal principal) {
        AddressDto addressDto = userInfoService.getAddressByIdAndUser(id);

        model.addAttribute("address", addressDto);

        return "user/address/edit";
    }

    @PostMapping("/user/address/edit")
    public String editAddressForm(@RequestParam("id") long id, @ModelAttribute("address") AddressDto updatedAddress, RedirectAttributes attributes) {
        boolean validationError = validateAddress(updatedAddress, attributes);

        if (validationError) {
            return "redirect:/user/address/edit/" + id;
        }

        userInfoService.updateAddress(id, updatedAddress);
        return "redirect:/user/address";
    }

    @GetMapping("/user/address/delete/{id}")
    public String deleteAddress(@PathVariable long id, RedirectAttributes attributes){
        try {
            userInfoService.deleteAddress(id);
        }catch (AddressNotFoundException e){
            attributes.addFlashAttribute("deletionError","Some error found at deletion of the address");
        }
        return "redirect:/user/address";
    }

    @GetMapping("/user/profile/edit")
    public String editProfile(Model model,Principal principal){

        UserInfo user = userInfoService.getUserByEmail(principal.getName());

        model.addAttribute("user", user);
        return "user/edit-profile";
    }

    @PostMapping("/user/profile/edit")
    public String editUserInfo(@RequestParam("userId") UUID userId,
                               @RequestParam("username") String username,
                               @RequestParam("phoneNumber") String number, RedirectAttributes attributes) {
        BigInteger phoneNumber = null;

        if (StringUtils.isEmpty(number)) {
            attributes.addFlashAttribute("error2", "Enter the phone number.");
            return "redirect:/user/profile/edit";
        }

        try {
            phoneNumber = new BigInteger(number);
        } catch (NumberFormatException e) {
            attributes.addFlashAttribute("error2", "Invalid phone number format.");
            return "redirect:/user/profile/edit";
        }

        if (StringUtils.isEmpty(username)) {
            attributes.addFlashAttribute("error1", "Enter the username.");
            return "redirect:/user/profile/edit";
        }

        if (phoneNumber != null && !userInfoService.isPhoneNumberValid(phoneNumber.toString())) {
            attributes.addFlashAttribute("error2", "Phone number should contain 10 digits.");
        }

        if (!userInfoService.isUsernameValid(username)) {
            attributes.addFlashAttribute("error1", "Username must be at least 2 characters long.");
            return "redirect:/user/profile/edit";
        }

        try {
            UserInfo updatedUser = userInfoService.editUserInfo(userId, username, phoneNumber.toString());
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error1", "Failed to update user information.");
            return "redirect:/user/profile/edit";
        }

        return "redirect:/profile";
    }

    @GetMapping("/user/cart")
    public String cartPage(Model model,RedirectAttributes attributes) {
        String email = userInfoService.currentUser();

//        if (cartedProducts == null){
//            attributes.addFlashAttribute("error","First you need to add products in cart");
//            return "redirect:/products";
//        }
        Cart cart = null;
        List<CartItemDTO> cartedProducts = null;
        try {
            cart = cartService.getCart(email);
            cartedProducts = cartService.getAllCartItems(email);
        } catch (Exception e){
                attributes.addFlashAttribute("error","First you add the products in cart");
                return "redirect:/products";
        }

        Double total = 0.0;
        for (CartItemDTO h : cartedProducts){
           total += (h.getProductPrice()-h.getOfferAmount()) * h.getQuantity();
        }
        cart.setTotalPrice(total);
        model.addAttribute("cart", cart);
        model.addAttribute("cartedProducts", cartedProducts);
        return "user/cart";
    }

    @GetMapping("/user/cart/submit")
    public String cartCheckOutButton(HttpSession session,RedirectAttributes attributes){
        String email = userInfoService.currentUser();
        if (cartService.hasCartItemsWithQuantityGreaterThanZero(email)){
            session.setAttribute("checkout","Access checkout Page");
            return "redirect:/user/checkout";
        }
        attributes.addFlashAttribute("error","First you will add the products to cart");
        return "redirect:/user/cart";
    }

    @GetMapping("/user/wallet")
    public String walletPage(Principal principal,Model model){
        String email = userInfoService.currentUser();

        Wallet wallet = walletService.getWalletByEmail(email);
        model.addAttribute("user",userInfoService.getUserByEmail(principal.getName()));
        model.addAttribute("wallet",wallet);
        return "user/wallet";
    }

    @GetMapping("/user/wishlist")
    public String getWishlist(Model model) {
        String userEmail = userInfoService.currentUser();
        List<Product> wishlistItems = userInfoService.getWishlistItems(userEmail);

        model.addAttribute("products", wishlistItems);

        return "user/wishlist";
    }

    private boolean validatePhoneNumber(Integer phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        String phoneNumberStr = String.valueOf(phoneNumber);
        return phoneNumberStr.length() == 10 && phoneNumberStr.matches("\\d+");
    }

    private boolean validateAddress(AddressDto addressDto, RedirectAttributes attributes) {
        boolean error = false;

        try {
            BigInteger number = new BigInteger(addressDto.getMobile());
        } catch (RuntimeException e) {
            error = true;
            attributes.addFlashAttribute("numberError", "Invalid Phone number");
        }

        try {
            Integer pin = Integer.parseInt(addressDto.getPin());
        } catch (NumberFormatException e) {
            error = true;
            attributes.addFlashAttribute("pinError", "Invalid Pin number");
        }

        if (addressDto.getMobile().length() < 10) {
            attributes.addFlashAttribute("numberError", "Invalid Phone number");
            error = true;
        }
        if (addressDto.getName().length() < 2 || addressDto.getName().length() > 20) {
            attributes.addFlashAttribute("nameError", "Name must be contain 2 to 20 characters");
            error = true;
        }
        if (addressDto.getCity().length() < 2) {
            attributes.addFlashAttribute("cityError", "City Should be 2 or more characters needed");
            error = true;
        }
        if (addressDto.getPin().length() < 5) {
            attributes.addFlashAttribute("pinError", "Invalid Pin number");
            error = true;
        }
        if (addressDto.getAddress().length() < 5) {
            attributes.addFlashAttribute("addressError", "Invalid Address");
            error = true;
        }
        if (addressDto.getState().length() < 2) {
            attributes.addFlashAttribute("stateError", "Enter a valid state");
            error = true;
        }

        return error;
    }

    @GetMapping("/user/coupon")
    public String couponPage(Model model){
        String userEmail = userInfoService.currentUser();
        List<CouponDto> couponDto = couponService.getAllCouponsForUser(userEmail);
        model.addAttribute("coupons",couponDto);
        return "user/coupons";
    }



}