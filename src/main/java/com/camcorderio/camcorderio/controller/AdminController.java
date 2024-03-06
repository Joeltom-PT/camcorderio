package com.camcorderio.camcorderio.controller;

import com.camcorderio.camcorderio.entity.admin.Banner;
import com.camcorderio.camcorderio.entity.user.Coupon;
import com.camcorderio.camcorderio.entity.user.Orders;
import com.camcorderio.camcorderio.entity.user.ReturnRequest;
import com.camcorderio.camcorderio.entity.user.UserInfo;
import com.camcorderio.camcorderio.exceptions.FutureDateException;
import com.camcorderio.camcorderio.exceptions.NoOrdersFoundException;
import com.camcorderio.camcorderio.model.admin.*;
import com.camcorderio.camcorderio.model.product.BrandDto;
import com.camcorderio.camcorderio.model.product.CategoryDto;
import com.camcorderio.camcorderio.model.product.ProductDto;
import com.camcorderio.camcorderio.model.user.CouponDto;
import com.camcorderio.camcorderio.service.admin.BannerService;
import com.camcorderio.camcorderio.service.admin.SalesReport;
import com.camcorderio.camcorderio.service.others.UserRedirectService;
import com.camcorderio.camcorderio.service.product.BrandService;
import com.camcorderio.camcorderio.service.product.CategoryService;
import com.camcorderio.camcorderio.service.product.ProductService;
import com.camcorderio.camcorderio.service.user.CouponService;
import com.camcorderio.camcorderio.service.user.OrderService;
import com.camcorderio.camcorderio.service.user.ReturnRequestService;
import com.camcorderio.camcorderio.service.user.UserInfoService;
import com.razorpay.Order;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping(path = "/admin")
public class AdminController {
    private UserRedirectService userRedirectService;
    private final UserInfoService userService;
    private final ReturnRequestService returnRequestService;
    private final CategoryService categoryService;
    private final SalesReport salesReportService;
    private final BrandService brandService;
    private final CouponService couponService;
    private final ProductService productService;
    private final BannerService bannerService;

    private final OrderService orderService;

    public AdminController(UserRedirectService userRedirectService, UserInfoService userService, ReturnRequestService returnRequestService, CategoryService categoryService, SalesReport salesReportService, BrandService brandService, CouponService couponService, ProductService productService, BannerService bannerService, OrderService orderService) {
        this.userRedirectService = userRedirectService;
        this.userService = userService;
        this.returnRequestService = returnRequestService;
        this.categoryService = categoryService;
        this.salesReportService = salesReportService;
        this.brandService = brandService;
        this.couponService = couponService;
        this.productService = productService;
        this.bannerService = bannerService;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Principal principal,Model model){
       List<ProductDto> productDtos = productService.getTop10SellingProducts();
       List<CategoryDto> categoryDtos = categoryService.getTop10SellingCategories();
       List<BrandDto> brandDtos = brandService.getTop10SellingBrand();

        model.addAttribute("products",productDtos);
        model.addAttribute("categories",categoryDtos);
        model.addAttribute("brands",brandDtos);
        return "admin/dashboard";
    }



    @GetMapping("/users")
    public String listUser(@PageableDefault(size = 10) Pageable pageable, Model model, HttpSession session) {
        Page<UserInfo> userInfos = (Page<UserInfo>) session.getAttribute("userSearchResult");

        if (userInfos !=null) {
            model.addAttribute("users",userInfos);
            session.removeAttribute("userSearchResult");
            return "admin/listusers";
        }else {
            Page<UserInfo> userInfoPage = userService.getAllUsers(pageable);
            model.addAttribute("users", userInfoPage);
            return "admin/listusers";
        }

    }

    @PostMapping("/user/search")
    public String searchUser(@RequestParam("search") String query, @PageableDefault(size = 10) Pageable pageable, HttpSession session) {
        Page<UserInfo> userInfoPage = userService.searchUsers(query, pageable);
        session.setAttribute("userSearchResult", userInfoPage);
        return "redirect:/admin/users";
    }

    @GetMapping("/user/{userId}/togglestatus")
    public String toggleUserStatus(@PathVariable UUID userId) {
        userService.toggleUserStatus(userId);
        return "redirect:/admin/users";
    }


    @GetMapping("/sales")
    public String salesPage(Model model) {
        List<DailySales> dailySales = salesReportService.getDailySales();
        List<WeeklySales> weeklySales = salesReportService.getWeeklySales();
        List<MonthlySales> monthlySales = salesReportService.getMonthlySales();
        List<YearlySales> yearlySales = salesReportService.getYearlySales();

        List<Integer> dailyValues = salesReportService.getDailyValues();
        List<Integer> weeklyValues = salesReportService.getWeeklyValues();
        List<Integer> monthlyValues = salesReportService.getMonthlyValues();
        List<Integer> yearlyValues = salesReportService.getYearlyValues();

        model.addAttribute("dailySales",dailySales);
        model.addAttribute("weeklySales",weeklySales);
        model.addAttribute("monthlySales",monthlySales);
        model.addAttribute("yearlySales",yearlySales);

        model.addAttribute("monthlyValues", monthlyValues);
        model.addAttribute("yearlyValues", yearlyValues);
        model.addAttribute("weeklyValues" , weeklyValues);
        model.addAttribute("dailyValues", dailyValues);
        return "admin/sales";
    }

    @GetMapping(value = "/sales/salesReport/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportSalesReportPDF(@RequestParam("start") String start,
                                                       @RequestParam("end") String end) {
        try {
            byte[] pdfBytes = salesReportService.generateSalesReportPDF(start, end);
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (NoOrdersFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
        catch (FutureDateException e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/banner")
    public String bannerManager(Model model){
        List<Banner> banners = bannerService.getAllBanners();
        model.addAttribute("banners",banners);
      return "admin/banner/banner";
    }

    @PostMapping("/banner/add")
    public String addBanner(@RequestParam("link") String url,
                            @RequestParam("file") MultipartFile file,
                            RedirectAttributes attributes) {
        if (url == null || file == null){
            attributes.addFlashAttribute("error","Fill fields to add the banner.");
          return "redirect:/admin/banner";
        }
        boolean banner = bannerService.addBanner(url,file);
         if (banner == true){
             attributes.addFlashAttribute("success", "Banner added successfully");
         } else {
             attributes.addFlashAttribute("error", "Error uploading banner");
         }
        return "redirect:/admin/banner";
    }

    @PostMapping("/banner/edit/{id}")
    public String bannerEdit(@PathVariable("id") long id,@RequestParam("link") String url,RedirectAttributes attributes){
        if (url == null){
            attributes.addFlashAttribute("error","Fill the input and try .");
            return "redirect:/admin/banner";
        }
        boolean check = bannerService.updateBanner(id,url);
        if (check == true){
            attributes.addFlashAttribute("success","Successfully edited the banner link");
            return "redirect:/admin/banner";
        }
        attributes.addFlashAttribute("error","Error editing the banner link");
        return "redirect:/admin/banner";
    }

    @GetMapping("/banner/delete/{id}")
    public String bannerDelete(@PathVariable("id") long id,RedirectAttributes attributes) {
        System.out.println("working properly" + id);
        boolean check = bannerService.deleteBanner(id);
        if (check) {
            attributes.addFlashAttribute("success","Banner deleted successful.");
            return "redirect:/admin/banner";
        } else {
            attributes.addFlashAttribute("error","Error deleting the banner");
            return "redirect:/admin/banner";
        }
    }


    @GetMapping("/return")
    public String returnRequestPage(Model model){
        List<ReturnRequest> requests = returnRequestService.getAllReturnRequests();
        model.addAttribute("requests", requests);
        return "admin/returnRequest";
    }

    @GetMapping("/return/details/{id}")
    public String returnRequestDetail(@PathVariable long id,Model model){
        ReturnRequest request = returnRequestService.getReturnRequestById(id);
        model.addAttribute("request",request);
        return "admin/returnDetails";
    }


    @GetMapping("/coupons")
    public String couponManagePage(Model model){
        List<CouponDto> coupons = couponService.getAllCoupons();
        model.addAttribute("coupons",coupons);
        return "admin/coupons/coupons";
    }


    @PostMapping("/add/coupon")
    public String addCoupon(@ModelAttribute("couponDto") @Valid CouponDto couponDto, BindingResult result, RedirectAttributes attributes) {
        List<String> errors = new ArrayList<>();

        // Check for validation errors
        if (result.hasErrors()) {
            errors.add("Error Validating Coupon. Fill in all required inputs.");
        }

        // Check for null values in couponDto fields
        if (couponDto.getName() == null || couponDto.getAmount() == null|| couponDto.getMinimumOrderAmount() == null ||
                couponDto.getStartDate() == null || couponDto.getExpiryDate() == null) {
            errors.add("Fill in all required inputs.");
        }

        // Check for logical errors
        if (couponDto.getAmount() != null && couponDto.getMinimumOrderAmount() != null &&
                couponDto.getAmount() >= couponDto.getMinimumOrderAmount()) {
            errors.add("Coupon offer amount should be lower than minimum order amount.");
        }

        if (couponDto.getStartDate() != null && couponDto.getExpiryDate() != null) {
            LocalDate today = LocalDate.now();
            if (!couponDto.getStartDate().isAfter(today) || !couponDto.getExpiryDate().isAfter(today)) {
                errors.add("Start date and expiry date should be today or a future date.");
            }
        }

        if (couponDto.getStartDate() != null && couponDto.getExpiryDate() != null &&
                couponDto.getStartDate().isAfter(couponDto.getExpiryDate())) {
            errors.add("Start date must be before expiry date.");
        }

        // Check for duplicate coupon name
        if (couponService.isCouponNameExists(couponDto.getName())) {
            errors.add("Coupon with this name already exists");
        }

        // Handle errors
        if (!errors.isEmpty()) {
            attributes.addFlashAttribute("errors", errors);
            return "redirect:/admin/coupons";
        }

        // Proceed with adding the coupon
        Coupon coupon = couponService.addCoupon(couponDto);
        if (coupon == null) {
            attributes.addFlashAttribute("error", "Error adding the coupon");
            return "redirect:/admin/coupons";
        }
        attributes.addFlashAttribute("success", "Coupon added successfully by the code of :" + coupon.getCouponCode());
        return "redirect:/admin/coupons";
    }


    @GetMapping("/coupons/edit/{id}")
    public String editCoupon(@PathVariable long id,Model model){
        CouponDto coupon = couponService.getCouponById(id);
        model.addAttribute("coupon",coupon);
        return "admin/coupons/edit";
    }


    @PostMapping("/coupons/edit/{id}")
    public String editCouponForm(@ModelAttribute("couponDto") @Valid CouponDto couponDto, BindingResult result, @PathVariable long id, RedirectAttributes attributes) {
        List<String> errors = new ArrayList<>();


        if (result.hasErrors()) {
            errors.add("Error Validating Coupon");
        }


        if (couponDto.getName() == null || couponDto.getAmount() == null || couponDto.getMinimumOrderAmount() == null ||
                couponDto.getStartDate() == null || couponDto.getExpiryDate() == null) {
            errors.add("Fill in all required inputs.");
        }


        if (couponDto.getAmount() != null && couponDto.getMinimumOrderAmount() != null &&
                couponDto.getAmount() >= couponDto.getMinimumOrderAmount()) {
            errors.add("Coupon offer amount should be lower than minimum order amount.");
        }

        if (couponDto.getStartDate() != null && couponDto.getExpiryDate() != null) {
            LocalDate today = LocalDate.now();
            if (!couponDto.getStartDate().isAfter(today) || !couponDto.getExpiryDate().isAfter(today)) {
                errors.add("Start date and expiry date should be today or a future date.");
            }
        }

        if (couponDto.getStartDate() != null && couponDto.getExpiryDate() != null &&
                couponDto.getStartDate().isAfter(couponDto.getExpiryDate())) {
            errors.add("Start date must be before expiry date.");
        }


        if (couponService.isCouponNameExistsNotId(couponDto.getName(), id)) {
            errors.add("Coupon with this name already exists");
        }


        if (!errors.isEmpty()) {
            attributes.addFlashAttribute("errors", errors);
            return "redirect:/admin/coupons/edit/" + id;
        }

        CouponDto existingCoupon = couponService.getCouponById(id);
        if (existingCoupon == null) {
            attributes.addFlashAttribute("error", "Coupon not found.");
            return "redirect:/admin/coupons";
        }

        couponDto.setId(id);
        Coupon updatedCoupon = couponService.updateCoupon(couponDto);
        if (updatedCoupon == null) {
            attributes.addFlashAttribute("error", "Error editing the coupon");
            return "redirect:/admin/coupons/edit/" + id;
        }
        attributes.addFlashAttribute("success", "Coupon edited successfully by the code of :" + updatedCoupon.getCouponCode());
        return "redirect:/admin/coupons";
    }


    @GetMapping("/coupon/delete/{id}")
    public String deleteCoupon(@PathVariable long id, RedirectAttributes attributes){
        boolean coupon = couponService.deleteCoupon(id);
        if (coupon == true){
            attributes.addFlashAttribute("deleteError","Error in deletion");
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/order/return/from")
    public String orderReturnDecision(@RequestParam("returnDecision") String returnDecision, @RequestParam("id") long id, RedirectAttributes attributes) {
        boolean check = returnRequestService.returnReturnDecision(id,returnDecision);
        if (check == true){
            attributes.addFlashAttribute("success","Return request handled successful.");
            return "redirect:/admin/return";
        }

        attributes.addFlashAttribute("error","Error return request handle.");
        return "redirect:/admin/return";
    }

}