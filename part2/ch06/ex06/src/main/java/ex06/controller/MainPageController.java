package ex06.controller;

import ex06.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPageController {

    private ProductService productService;

    public MainPageController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/main")
    public String main(Authentication a, Model model){
        model.addAttribute("username", a.getName());
        model.addAttribute("products", productService.findAll());

        return "main";
    }

}
