package com.techmarket.orderservice.client;

import com.techmarket.orderservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", path = "/api/products")
public interface CatalogClient {

    @GetMapping("/{id}")
    ProductDto getProductById(@PathVariable("id") String id);
}
