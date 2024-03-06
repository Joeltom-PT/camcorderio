package com.camcorderio.camcorderio.controller;

import com.camcorderio.camcorderio.entity.admin.Banner;
import com.camcorderio.camcorderio.model.product.ProductWithOfferDto;
import com.camcorderio.camcorderio.model.user.UserInfoDTO;
import com.camcorderio.camcorderio.service.admin.BannerService;
import com.camcorderio.camcorderio.service.others.UserRedirectService;
import com.camcorderio.camcorderio.service.product.ProductService;
import com.camcorderio.camcorderio.service.user.UserServiceImpl;
import com.camcorderio.camcorderio.service.verification.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Controller
public class MainController {

    private final OtpService otpService;
    private final UserServiceImpl userService;

    private final ProductService productService;
    private final BannerService bannerService;
    private final UserRedirectService userRedirectService;
    public MainController(OtpService otpService, UserServiceImpl userService, ProductService productService, BannerService bannerService, UserRedirectService userRedirectService) {
        this.otpService = otpService;
        this.userService = userService;
        this.productService = productService;
        this.bannerService = bannerService;
        this.userRedirectService = userRedirectService;
    }

    @GetMapping("/")
    public String homePage(Principal principal , Model model) {

        if (principal != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            if (roles.contains("ADMIN")) {
                return "redirect:/admin/dashboard";
            }
            if (roles.contains("USER")){
                model.addAttribute("user",userService.currentUserDetail());
            } else {
                model.addAttribute("user",userService.currentUserDetail());
            }

        }
        List<ProductWithOfferDto> products = productService.getProductsForHome();

        List<Banner> banners = bannerService.getAllBanners();

        model.addAttribute("products", products);
        model.addAttribute("banners",banners);
        return "user/home";
    }

    @GetMapping("/contact-us")
    public String contactUsPage(){
        return "user/contact-us";
    }

    @GetMapping("/about-us")
    public String aboutUsPage(){
        return "user/about-us";
    }

    @GetMapping("/group-chat")
    public String groupChatPage(){
        return "user/group-chat";
    }


    @GetMapping("/login")
    public String loginPage(HttpSession session, Principal principal, Model model) {

        String redirect = userRedirectService.redirectUserByRole(principal);

        if (redirect != null) {
            return redirect;
        }

        String loginError = (String) session.getAttribute("loginError");

        Object userDisabled =  session.getAttribute("userDisabled");
        if(userDisabled!=null){
            model.addAttribute(userDisabled);
            session.removeAttribute("userDisabled");
            return "login";
        }

        if (loginError != null) {
            model.addAttribute("loginError", loginError);
            session.removeAttribute("loginError");
        }

        return "login";
    }

    @GetMapping("/signup")
    public String signUpPage(Principal principal, HttpSession session) {
        String redirect = userRedirectService.redirectUserByRole(principal);

        if (redirect != null) {
            return redirect;
        }

        Object so = session.getAttribute("userDto");

        if (so != null) {
            return "redirect:/verify-otp";
        }

        return "register";
    }

    @GetMapping("/referral/message")
    public ResponseEntity<String> referralIdSubmit(@RequestParam("referral") String referral,HttpSession session){
        boolean check = userService.findReferral(referral);
        if (check){
            session.setAttribute("referralId",referral);
            return ResponseEntity.ok("success");
        }
        return ResponseEntity.badRequest().body("error");
    }

    @PostMapping("/signup")
    public String createUser(@ModelAttribute("userSignupInfo") UserInfoDTO user, HttpSession session, RedirectAttributes attributes) {

        String usernameError = null;
        String emailError = null;
        String passwordError = null;

        if (!isValidUsername(user.getUsername())) {
            usernameError = "Invalid username. Username must be 4 or more characters, and no spaces are allowed.";
        }

        if (!isValidEmail(user.getEmail())) {
            emailError = "Invalid email address format.";
        }

        if (userService.emailExist(user.getEmail())) {
            emailError = "Email already exists. Try another one.";
        }

        if (!isValidPassword(user.getPassword())) {
            passwordError = "Invalid password. Password must contain at least one capital letter, one small letter, and one number.";
        }

        if (usernameError != null || emailError != null || passwordError != null) {
            attributes.addFlashAttribute("usernameError", usernameError);
            attributes.addFlashAttribute("emailError", emailError);
            attributes.addFlashAttribute("passwordError", passwordError);
            return "redirect:/signup";
        }

        session.setAttribute("userDto", user);

        otpService.generateAndSendOtp(user.getEmail(), session);

        attributes.addFlashAttribute("success", "Otp sent to: " + user.getEmail());
        return "redirect:/verify-otp";
    }

    private boolean isValidUsername(String username) {

        if (username.length() < 4 || username.length() >= 20) {
            return false;
        }

        if (username.trim().isEmpty() || username.matches("[.]+")) {
            return false;
        }

        if (username.contains("  ")) {
            return false;
        }

        String allowedCharactersRegex = "^[a-zA-Z0-9_.\\s]+$";

        return username.matches(allowedCharactersRegex);
    }

    private boolean isValidPassword(String password) {

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d\\W_]{8,}$";
        return password.matches(passwordRegex);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,6}$";
        return email.matches(emailRegex);
    }

    @GetMapping("/verify-otp")
    public String showOtpVerificationForm(HttpSession session, RedirectAttributes redirectAttributes) {

        Object userFromSession = session.getAttribute("userDto");

        if (userFromSession != null) {
            return "otp";
        }
        redirectAttributes.addAttribute("404", "Resource Not Found");

        return "redirect:/error";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp1") int otp1,
                            @RequestParam("otp2") int otp2,
                            @RequestParam("otp3") int otp3,
                            @RequestParam("otp4") int otp4,
                            @RequestParam("otp5") int otp5,
                            @RequestParam("otp6") int otp6,
                            HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            String otp = String.valueOf(otp1) + String.valueOf(otp2) + String.valueOf(otp3) + String.valueOf(otp4) + String.valueOf(otp5) + String.valueOf(otp6);

            otpService.verifyOtp(otp, session);
            UserInfoDTO userDto = (UserInfoDTO) session.getAttribute("userDto");
            String referral = (String) session.getAttribute("referralId");
           if (referral!=null){
               userService.addAmountForReferral(referral);
               session.removeAttribute("referralId");
           }
            userService.saveUser(userDto);

            redirectAttributes.addFlashAttribute("success", "Registration successfully completed : " + userDto.getEmail() + ". You can now login");
            session.removeAttribute("userDto");

            return "redirect:/login";
        } catch (OtpService.OtpTimeoutException e) {

            model.addAttribute("error", "Otp time out. You can try again.");
            return "otp";
        } catch (OtpService.OtpVerificationException e) {

            model.addAttribute("error", "Wrong OTP. You can try again or resend OTP.");
            return "otp";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session,RedirectAttributes attributes) {
        try {

            UserInfoDTO userDto = (UserInfoDTO) session.getAttribute("userDto");

            otpService.generateAndSendOtp(userDto.getEmail(), session);

            attributes.addFlashAttribute("resentSuccess", "OTP resent successfully!");
            return "redirect:/verify-otp";
        } catch (Exception e) {
            attributes.addFlashAttribute("resentError", "Error resending OTP. Please try again.");
            return "redirect:/verify-otp";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Principal principal) {
        String redirect = userRedirectService.redirectUserByRole(principal);

        if (redirect != null) {
            return redirect;
        }

        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam("email") String email, HttpSession session, RedirectAttributes attributes) {

        if (!userService.emailExist(email)) {
            attributes.addFlashAttribute("userNotFound", "User not found by this mail : " + email);
            return "redirect:/forgot-password";
        }

        session.setAttribute("email", email);

        otpService.generateAndSendOtp(email, session);

        return "redirect:/verify-forgot-otp";
    }

    @GetMapping("/verify-forgot-otp")
    public String showOtpVerificationFormForForgotPassword(HttpSession session, RedirectAttributes redirectAttributes, Principal principal) {

        String redirect = userRedirectService.redirectUserByRole(principal);

        if (redirect != null) {
            return redirect;
        }

        Object userFromEmailSession = session.getAttribute("email");

        if (userFromEmailSession != null) {
            return "reset-password";
        }
        redirectAttributes.addAttribute("404", "Resource Not Found");

        return "redirect:/error";
    }

    @PostMapping("/verify-forgot-otp")
    public String forgotVerification(@RequestParam("otp1") int otp1,
                                     @RequestParam("otp2") int otp2,
                                     @RequestParam("otp3") int otp3,
                                     @RequestParam("otp4") int otp4,
                                     @RequestParam("otp5") int otp5,
                                     @RequestParam("otp6") int otp6,
                                     @RequestParam("newPassword") String newPassword,
                                     HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        String otp = String.valueOf(otp1) + String.valueOf(otp2) + String.valueOf(otp3) + String.valueOf(otp4) + String.valueOf(otp5) + String.valueOf(otp6);

        try {

            otpService.verifyOtp(otp, session);

            String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d\\W_]{8,}$";
            if (!newPassword.matches(passwordRegex)) {
                model.addAttribute("error", "Password must meet the required criteria.");
                return "forgot-password";
            }

            String email = (String) session.getAttribute("email");

            userService.resetPassword(email, newPassword);

            session.removeAttribute("email");

            redirectAttributes.addFlashAttribute("success", "Password reset successfully. You can now login with the new password");
            return "redirect:/login";
        } catch (OtpService.OtpTimeoutException e) {
            model.addAttribute("error", "Otp time out. You can try again.");
            return "forgot-password";
        } catch (OtpService.OtpVerificationException e) {
            model.addAttribute("error", "Wrong OTP. You can try again or resend OTP.");
            return "forgot-password";
        }
    }

    @PostMapping("/resend-otp-forgot")
    public String resendOtpForPassword(HttpSession session, Model model) {
        try {

            String email = (String) session.getAttribute("email");

            session.setAttribute("email", email);

            otpService.generateAndSendOtp(email, session);

            model.addAttribute("resendSuccess", "OTP resent successfully!");
            return "redirect:/verify-forgot-otp";
        } catch (Exception e) {
            model.addAttribute("resendError", "Error resending OTP. Please try again.");
            return "redirect:/verify-forgot-otp";
        }
    }

}