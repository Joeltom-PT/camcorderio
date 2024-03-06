package com.camcorderio.camcorderio.controller;

import com.camcorderio.camcorderio.entity.product.BrandOffer;
import com.camcorderio.camcorderio.entity.product.Product;
import com.camcorderio.camcorderio.model.product.BrandDto;
import com.camcorderio.camcorderio.model.product.ProductMiniDto;
import com.camcorderio.camcorderio.model.product.ProductOfferDto;
import com.camcorderio.camcorderio.service.product.BrandOfferService;
import com.camcorderio.camcorderio.service.product.BrandService;
import com.camcorderio.camcorderio.service.product.ProductOfferService;
import com.camcorderio.camcorderio.service.product.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class OfferController {


    private final ProductOfferService productOfferService;
    private final ProductService productService;

    private final BrandOfferService brandOfferService;

    private final BrandService brandService;

    public OfferController(ProductOfferService productOfferService, ProductService productService, BrandOfferService brandOfferService, BrandService brandService) {
        this.productOfferService = productOfferService;
        this.productService = productService;
        this.brandOfferService = brandOfferService;
        this.brandService = brandService;
    }



    @GetMapping("/admin/offer/brand")
    public String brandOfferList(@PageableDefault(size = 10) Pageable pageable, Model model){
        Page<BrandOffer> brandOffers = brandOfferService.getAllBrandOffers(pageable);
        model.addAttribute("offers",brandOffers);
        return "admin/offer/brand/list";
    }

    @GetMapping("/admin/brand/offer/add")
    public String addBrandOffer(Model model){
        List<BrandDto> brands = brandService.getAllBrandsFor();
        model.addAttribute("brands",brands);
        return "admin/offer/brand/add";
    }

    @PostMapping("/admin/brand/offer/add")
    public String addBrandOfferForm(RedirectAttributes attributes,@RequestParam("brandId") long brandId,
                                    @RequestParam("offerPercentage") int offerPercentage,
                                    @RequestParam("offerEndDate") LocalDate offerEndDate){
        boolean error = false;
        BrandDto brands = brandService.getBrandById(brandId);
        BrandOffer brandOffer = brandOfferService.getBrandByBrandId(brandId);
        if (brandOffer != null){
            attributes.addFlashAttribute("offerExist","Brand Offer is already existing for this brand.");
            error = true;
        }
        if (brands == null){
            attributes.addFlashAttribute("brandError","Brand is not existing. Try another");
            error = true;
        }
        if (offerPercentage>50||offerPercentage<1){
            attributes.addFlashAttribute("offerPercentageError","Offer percentage only allowed 1% to 50%.");
         error = true;
        }
        if (offerEndDate.isBefore(LocalDate.now())){
            attributes.addFlashAttribute("offerDateError","Offer end date should be today or a future date.");
         error = true;
        }

        if (error == true){
            return "redirect:/admin/brand/offer/add";
        }

        boolean add = brandOfferService.addOffer(brandId,offerPercentage,offerEndDate);

        return "redirect:/admin/offer/brand";
    }

    @GetMapping("/admin/offer/brand/edit/{id}")
    public String editBrand(@PathVariable long id,Model model,RedirectAttributes attributes){
        BrandOffer brandOffer = brandOfferService.getBrandOfferById(id).get();
        boolean error = false;
        if (brandOffer == null){
            attributes.addFlashAttribute("offerNotExist","Offer is not existing.");
            error = true;
        }

        model.addAttribute("offer",brandOffer);
       return "admin/offer/brand/edit";
    }

    @PostMapping("/admin/offer/brand/edit")
    public String editBrandForm(RedirectAttributes attributes,
                                @RequestParam("offerId") long offerId,
                                @RequestParam("offerPercentage") int offerPercentage,
                                @RequestParam("offerEndDate") LocalDate offerEndDate){
        BrandOffer brandOffer = brandOfferService.getBrandOfferById(offerId).get();
       Integer offer = brandOffer.getDiscount();
       boolean error = false;
       if (brandOffer == null){
           attributes.addFlashAttribute("notExist","Brand offer is not exist.");
           error = true;
       }
      if (offerPercentage>50||offerPercentage<1){
          attributes.addFlashAttribute("percentageError","Offer percentage should be 1% to 50% is allowed.");
          error = true;
      }
      if (offerEndDate.isBefore(LocalDate.now())){
          attributes.addFlashAttribute("dateError","Offer expire date should be today or future dates.");
       error = true;
      }

      if (error == true){
          return "redirect:/admin/offer/brand/edit/"+offerId;
      }

      boolean check = brandOfferService.updateBrandOffer(offerId,offerPercentage,offerEndDate);

      if (check){
          attributes.addFlashAttribute("success","Brand offer update successful.");
          return "redirect:/admin/offer/brand";
      }

        attributes.addFlashAttribute("error","Error updating brand offer");
        return "redirect:/admin/offer/brand";
    }

    @GetMapping("/admin/offer/brand/delete/{id}")
    public ResponseEntity<String> deleteBrandOffer(@PathVariable("id") long id){
        Optional<BrandOffer> brandOffer = brandOfferService.getBrandOfferById(id);
        if (brandOffer == null){
            return ResponseEntity.badRequest().body("Brand offer is not found");
        }
        boolean check = brandOfferService.deleteBrandOffer(id);

        if (check){
            return ResponseEntity.ok("Brand offer delete successful");
        }
        return ResponseEntity.badRequest().body("Error updating Brand offer");
    }



    ///////////// BRAND OFFER CONTROLLERS END //////////////



    @GetMapping("/admin/offer/product")
    public String productOfferList(Model model,@PageableDefault(size = 10) Pageable pageable){
        Page<ProductOfferDto> productOfferDtos = productOfferService.getAllProductOffers(pageable);
        model.addAttribute("offers",productOfferDtos);
        return "admin/offer/product/list";
    }

    @GetMapping("/admin/offer/product/add")
    public String productOfferAddingPage(Model model){
        List<ProductMiniDto> productMiniDtos = productService.getAllProductsWithIdAndName();
        model.addAttribute("products",productMiniDtos);
        return "admin/offer/product/add";
    }

    @PostMapping("/admin/offer/product/add")
    public String productOfferAddingForm(@RequestParam("productId") long productId,
                                         @RequestParam("offerAmount") double offerAmount,
                                         @RequestParam("offerEndDate") LocalDate offerEndDate,
                                         RedirectAttributes attributes){

        Product product = productService.getProductById(productId);
        boolean error = false;
        if (product == null){
            attributes.addFlashAttribute("productError","Product is not present.");
            error = true;
        }

        if (offerAmount>= product.getPrice()){
            attributes.addFlashAttribute("offerError","Offer discount should be lower than product price.");
            error = true;
        }

        if (offerEndDate.isBefore(LocalDate.now())){
            attributes.addFlashAttribute("dateError","Offer expire date should allowed only today or feature dates.");
            error = true;
        }

        if (product.getOfferDiscount() != null){
            attributes.addFlashAttribute("offerExisting","Offer already existing in this product.");
        }


        if (error == true){
            return "redirect:/admin/offer/product/add";
        }

        boolean check = productOfferService.addOffer(productId,offerAmount,offerEndDate);

        if (check != true){
            attributes.addFlashAttribute("error","Error adding the offer.");
           return "redirect:/admin/offer/product/add";
        }

        attributes.addFlashAttribute("success","Product offer added successful.");
        return "redirect:/admin/offer/product";

    }

    @GetMapping("/admin/offer/product/edit/{id}")
    public String productOfferEditPage(@PathVariable("id") long id,Model model,RedirectAttributes attributes){
        Product product = productService.getProductById(id);
        boolean error = false;
        if (product == null){
            attributes.addFlashAttribute("notFound","Product not found");
            error = true;
        }
        if (product.getOfferDiscount() == null){
            attributes.addFlashAttribute("offerNotExist","Product offer is not exist by this product.");
            error = true;
        }
        if (error == true){
            return "redirect::/admin/offer/product";
        }

        model.addAttribute("offer",product);
        return "admin/offer/product/edit";
    }

    @PostMapping("/admin/offer/product/edit")
    public String productOfferEditForm(RedirectAttributes attributes, @RequestParam("offerId") Long id,
                                       @RequestParam(name = "offerAmount", required = false) Double offerAmount,
                                       @RequestParam(name = "offerEndDate", required = false) LocalDate offerEndDate) {

        if (id == null || offerAmount == null || offerEndDate == null) {
            attributes.addFlashAttribute("error", "Please fill in all required fields.");
            return "redirect:/admin/offer/product/edit/"+id;
        }

        Product product = productService.getProductById(id);
        boolean error = false;

        if (product == null) {
            attributes.addFlashAttribute("productError", "Product not found.");
            error = true;
        }
        if (offerAmount >= product.getPrice()) {
            attributes.addFlashAttribute("offerError", "Product offer should be lower than product price.");
            error = true;
        }
        if (offerEndDate.isBefore(LocalDate.now())) {
            attributes.addFlashAttribute("dateError", "Offer expiration date should be today or a future date.");
            error = true;
        }
        if (product.getOfferDiscount() == null) {
            attributes.addFlashAttribute("offerExist", "Product offer does not exist.");
            error = true;
        }

        if (error) {
            return "redirect:/admin/offer/product/edit";
        }

        boolean check = productOfferService.updateOffer(id, offerAmount, offerEndDate);

        if (!check) {
            attributes.addFlashAttribute("updateError", "Error adding product offer.");
            return "redirect:/admin/offer/product";
        }

        attributes.addFlashAttribute("success", "Product offer added successfully.");
        return "redirect:/admin/offer/product";

    }

    @GetMapping("/admin/offer/product/delete/{id}")
    public ResponseEntity<String> deleteProductOffer(@PathVariable("id") long id) {
        boolean check = productOfferService.deleteOffer(id);
        if (check) {
            return ResponseEntity.ok("Deletion successful");
        } else {
            return ResponseEntity.badRequest().body("Failed to delete product offer");
        }
    }


}
