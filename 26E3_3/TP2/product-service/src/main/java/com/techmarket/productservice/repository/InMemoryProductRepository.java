package com.techmarket.productservice.repository;

import com.techmarket.productservice.domain.Product;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public Product save(Product product) {
        if (product.getId() == null || product.getId().isBlank()) {
            product.setId(String.valueOf(idSequence.incrementAndGet()));
        } else {
            // Update sequence if numerical id is higher
            try {
                long numericId = Long.parseLong(product.getId());
                idSequence.accumulateAndGet(numericId, Math::max);
            } catch (NumberFormatException ignored) {}
        }
        storage.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(String id) {
        return storage.containsKey(id);
    }

    @Override
    public void deleteById(String id) {
        storage.remove(id);
    }
}
