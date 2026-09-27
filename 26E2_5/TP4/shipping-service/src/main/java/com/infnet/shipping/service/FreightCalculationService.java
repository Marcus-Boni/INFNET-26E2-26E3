package com.infnet.shipping.service;

import com.infnet.shipping.dto.FreightOptionDto;
import com.infnet.shipping.dto.FreightQuoteRequest;
import com.infnet.shipping.dto.FreightQuoteResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class FreightCalculationService {

    public FreightQuoteResponse calculateRates(FreightQuoteRequest request) {
        String cleanCep = request.getZipCode() != null ? request.getZipCode().replaceAll("\\D", "") : "";
        if (cleanCep.length() < 5) {
            throw new IllegalArgumentException("CEP inválido. Deve conter pelo menos os 5 primeiros dígitos.");
        }

        char regionDigit = cleanCep.charAt(0);
        String regionName;
        BigDecimal baseRate;
        int baseDays;

        switch (regionDigit) {
            case '0':
            case '1':
                regionName = "São Paulo / Grande SP";
                baseRate = new BigDecimal("14.90");
                baseDays = 2;
                break;
            case '2':
                regionName = "Rio de Janeiro / Espírito Santo";
                baseRate = new BigDecimal("18.50");
                baseDays = 3;
                break;
            case '3':
                regionName = "Minas Gerais";
                baseRate = new BigDecimal("19.90");
                baseDays = 3;
                break;
            case '4':
            case '5':
                regionName = "Região Nordeste";
                baseRate = new BigDecimal("28.90");
                baseDays = 5;
                break;
            case '6':
                regionName = "Região Norte / Nordeste Setentrional";
                baseRate = new BigDecimal("34.90");
                baseDays = 6;
                break;
            case '7':
                regionName = "Região Centro-Oeste / DF";
                baseRate = new BigDecimal("24.50");
                baseDays = 4;
                break;
            case '8':
            case '9':
                regionName = "Região Sul";
                baseRate = new BigDecimal("21.90");
                baseDays = 4;
                break;
            default:
                regionName = "Brasil (Nacional)";
                baseRate = new BigDecimal("25.00");
                baseDays = 5;
                break;
        }

        int items = (request.getTotalItems() != null && request.getTotalItems() > 0) ? request.getTotalItems() : 1;
        BigDecimal weightMultiplier = BigDecimal.valueOf(1.0 + (items - 1) * 0.15);

        BigDecimal expressPrice = baseRate.multiply(new BigDecimal("1.65")).multiply(weightMultiplier).setScale(2, RoundingMode.HALF_UP);
        int expressDays = Math.max(1, baseDays - 1);

        BigDecimal standardPrice = baseRate.multiply(weightMultiplier).setScale(2, RoundingMode.HALF_UP);
        int standardDays = baseDays;

        BigDecimal ecoPrice = baseRate.multiply(new BigDecimal("0.75")).multiply(weightMultiplier).setScale(2, RoundingMode.HALF_UP);
        int ecoDays = baseDays + 2;

        // Frete grátis no Eco Cargo para compras acima de R$ 350
        if (request.getOrderTotal() != null && request.getOrderTotal().compareTo(new BigDecimal("350.00")) >= 0) {
            ecoPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        List<FreightOptionDto> options = new ArrayList<>();

        options.add(FreightOptionDto.builder()
                .serviceType("EXPRESS")
                .carrierName("Nexus Express Air")
                .description("Entrega prioritária expressa com rastreamento aéreo.")
                .price(expressPrice)
                .estimatedDays(expressDays)
                .build());

        options.add(FreightOptionDto.builder()
                .serviceType("STANDARD")
                .carrierName("LogBrasil Rodoviário")
                .description("Entrega rodoviária convencional com o melhor custo-benefício.")
                .price(standardPrice)
                .estimatedDays(standardDays)
                .build());

        options.add(FreightOptionDto.builder()
                .serviceType("ECONOMICAL")
                .carrierName("Eco Cargo Sustentável")
                .description(ecoPrice.compareTo(BigDecimal.ZERO) == 0 ? "FRETE GRÁTIS - Entrega econômica consolidada." : "Entrega econômica consolidada com menor pegada de carbono.")
                .price(ecoPrice)
                .estimatedDays(ecoDays)
                .build());

        return FreightQuoteResponse.builder()
                .destinationZipCode(request.getZipCode())
                .destinationRegion(regionName)
                .options(options)
                .build();
    }
}
