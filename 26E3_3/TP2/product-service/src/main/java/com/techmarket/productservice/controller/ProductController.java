package com.techmarket.productservice.controller;

import com.techmarket.productservice.dto.ProductRequest;
import com.techmarket.productservice.dto.ProductResponse;
import com.techmarket.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("X-Served-By", productService.getHostIdentity())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok()
                .header("X-Served-By", productService.getHostIdentity())
                .body(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        return ResponseEntity.ok()
                .header("X-Served-By", productService.getHostIdentity())
                .body(productService.getProductById(id));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getServiceInfo() {
        return ResponseEntity.ok(Map.of(
                "service", "product-service",
                "servedBy", productService.getHostIdentity(),
                "status", "UP"
        ));
    }
}
