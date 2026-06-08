package com.pikacards.payment.service;

import com.pikacards.auth.model.User;
import com.pikacards.cart.repository.CartItemRepository;
import com.pikacards.catalog.service.CardService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class StripeService {

    @Value("${pikacards.stripe.secret-key}") private String secretKey;
    @Value("${pikacards.frontend.url}") private String frontendUrl;
    private final CartItemRepository cartItemRepository;
    private final CardService cardService;

    public StripeService(CartItemRepository cartItemRepository, CardService cardService) {
        this.cartItemRepository = cartItemRepository; this.cardService = cardService;
    }

    @PostConstruct
    public void init() { Stripe.apiKey = secretKey; }

    public String createCheckoutSession(User user) throws StripeException {
        var cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) throw new IllegalStateException("Cart is empty");

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        for (var item : cartItems) {
            long unitAmount = (long) (cardService.getCardPrice(item.getCard()) * 100);
            lineItems.add(SessionCreateParams.LineItem.builder()
                    .setQuantity((long) item.getQuantity())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("pen")
                            .setUnitAmount(unitAmount)
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.getCard().getName()).build())
                            .build())
                    .build());
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addAllPaymentMethodType(List.of(SessionCreateParams.PaymentMethodType.CARD))
                .addAllLineItem(lineItems)
                .setSuccessUrl(frontendUrl + "/history?success=1")
                .setCancelUrl(frontendUrl + "/cancel")
                .setCustomerEmail(user.getEmail())
                .setClientReferenceId(user.getId().toString())
                .build();

        return Session.create(params).getUrl();
    }
}
