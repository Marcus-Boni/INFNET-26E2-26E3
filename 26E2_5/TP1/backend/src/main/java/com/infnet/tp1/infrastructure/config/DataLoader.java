package com.infnet.tp1.infrastructure.config;

import com.infnet.tp1.domain.model.Product;
import com.infnet.tp1.domain.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Autowired
    public DataLoader(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            List<Product> seedProducts = List.of(
                Product.builder()
                    .name("Teclado Mecânico RGB Premium")
                    .description("Teclado mecânico switch blue com retroiluminação RGB personalizável e layout ABNT2.")
                    .price(new BigDecimal("349.90"))
                    .stock(15)
                    .build(),
                Product.builder()
                    .name("Mouse Ergonômico Sem Fio")
                    .description("Mouse ergonômico vertical com conexão wireless 2.4Ghz, sensor óptico de 3200 DPI e bateria recarregável.")
                    .price(new BigDecimal("189.90"))
                    .stock(25)
                    .build(),
                Product.builder()
                    .name("Monitor UltraWide 34'' 144Hz")
                    .description("Monitor curvo 34 polegadas IPS UltraWide com taxa de atualização de 144Hz e tempo de resposta de 1ms.")
                    .price(new BigDecimal("2499.00"))
                    .stock(5)
                    .build(),
                Product.builder()
                    .name("Headphone Noise-Cancelling Bluetooth")
                    .description("Headphone premium com cancelamento ativo de ruído (ANC), drivers de 40mm e 40h de autonomia de bateria.")
                    .price(new BigDecimal("899.90"))
                    .stock(10)
                    .build(),
                Product.builder()
                    .name("Carregador por Indução Fast Charge")
                    .description("Base de carregamento rápido sem fio 15W compatível com padrão Qi e acabamento antiderrapante.")
                    .price(new BigDecimal("129.90"))
                    .stock(30)
                    .build(),
                Product.builder()
                    .name("Luminária de Mesa Articulada Smart")
                    .description("Luminária inteligente de mesa compatível com Alexa e Google Assistant, controle de temperatura e brilho.")
                    .price(new BigDecimal("199.90"))
                    .stock(0) // Out of stock to test UI out-of-stock states
                    .build(),
                Product.builder()
                    .name("Webcam 4K Ultra HD")
                    .description("Webcam de alta resolução 4K com microfones estéreo integrados, foco automático e tampa de privacidade.")
                    .price(new BigDecimal("459.00"))
                    .stock(3) // Low stock to test low-stock state warning
                    .build()
            );

            productRepository.saveAll(seedProducts);
            System.out.println("Banco de dados populado com " + seedProducts.size() + " produtos de demonstração.");
        }
    }
}
