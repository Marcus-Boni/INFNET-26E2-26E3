package com.techmarket.productservice.repository;

import com.techmarket.productservice.domain.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(String id);
    List<Product> findAll();
    boolean existsById(String id);
    void deleteById(String id);
}
