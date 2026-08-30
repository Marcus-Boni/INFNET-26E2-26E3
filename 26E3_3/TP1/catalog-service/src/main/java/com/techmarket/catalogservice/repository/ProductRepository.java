package com.techmarket.catalogservice.repository;

import com.techmarket.catalogservice.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findByTagsContainingIgnoreCase(String tag);

    List<Product> findByActiveTrue();
}
