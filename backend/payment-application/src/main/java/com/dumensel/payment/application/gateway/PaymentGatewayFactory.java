package com.dumensel.payment.application.gateway;

import com.dumensel.payment.domain.model.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Payment Gateway Factory
 * Strategy pattern kullanarak doğru gateway'i seçer
 * 
 * Application layer - Gateway selection logic
 */
@Component
public class PaymentGatewayFactory {
    
    private final Map<PaymentProvider, PaymentGateway> gateways;

    /**
     * Constructor injection ile tüm PaymentGateway implementation'ları alınır
     * Spring otomatik olarak PaymentGateway interface'ini implement eden tüm bean'leri inject eder
     */
    public PaymentGatewayFactory(List<PaymentGateway> gatewayList) {
        this.gateways = new EnumMap<>(PaymentProvider.class);
        
        // Her gateway'i provider'ına göre map'e koy
        for (PaymentGateway gateway : gatewayList) {
            PaymentProvider provider = detectProvider(gateway);
            gateways.put(provider, gateway);
        }
    }

    /**
     * Provider'a göre gateway seç
     */
    public PaymentGateway getGateway(PaymentProvider provider) {
        PaymentGateway gateway = gateways.get(provider);
        
        if (gateway == null) {
            throw new IllegalArgumentException(
                "No payment gateway found for provider: " + provider
            );
        }
        
        return gateway;
    }

    /**
     * Gateway class name'den provider'ı tespit et
     * Convention: {Provider}PaymentGateway (örn: CraftgatePaymentGateway)
     */
    private PaymentProvider detectProvider(PaymentGateway gateway) {
        String className = gateway.getClass().getSimpleName();
        
        if (className.contains("Craftgate")) {
            return PaymentProvider.CRAFTGATE;
        } else if (className.contains("Akbank")) {
            return PaymentProvider.AKBANK;
        }
        
        throw new IllegalArgumentException(
            "Cannot detect provider for gateway: " + className
        );
    }

    /**
     * Aktif gateway'leri listele
     */
    public Map<PaymentProvider, PaymentGateway> getActiveGateways() {
        return Map.copyOf(gateways);
    }
}

