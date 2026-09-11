package com.techmarket.productservice;

import com.techmarket.productservice.domain.Product;
import com.techmarket.productservice.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(ProductRepository repository) {
        return args -> {
            repository.save(Product.builder()
                    .id("1")
                    .name("Notebook Gamer Dell G15")
                    .description("Processador Core i7, 16GB RAM, RTX 4060, SSD 512GB")
                    .price(new BigDecimal("5499.00"))
                    .stockQuantity(15)
                    .build());

            repository.save(Product.builder()
                    .id("2")
                    .name("Mouse Sem Fio Logitech MX Master 3S")
                    .description("Sensor Darkfield 8000 DPI, cliques silenciosos e scroll MagSpeed")
                    .price(new BigDecimal("649.90"))
                    .stockQuantity(40)
                    .build());

            repository.save(Product.builder()
                    .id("3")
                    .name("Teclado Mecânico Keychron K2 Pro")
                    .description("Layout 75%, switches red hot-swappable e conexão Bluetooth/cabo")
                    .price(new BigDecimal("799.00"))
                    .stockQuantity(25)
                    .build());

            // Produto ID 10 para demonstrar o exemplo exato do enunciado da atividade (GET /products/10)
            repository.save(Product.builder()
                    .id("10")
                    .name("Monitor Gamer UltraWide 34 LG Curved")
                    .description("Resolução WQHD 160Hz, 1ms MBR, HDR10 e FreeSync Premium")
                    .price(new BigDecimal("2899.00"))
                    .stockQuantity(12)
                    .build());
        };
    }
}
