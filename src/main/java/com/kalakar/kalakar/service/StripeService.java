package com.kalakar.kalakar.service;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StripeService {

    @Value("${stripe.secret.key:sk_test_placeholder}")
    private String secretKey;

    @Value("${app.base-url:http://localhost:3036}")
    private String baseUrl;

    public String createSingleCheckoutSession(String productName,
                                               long priceInCents,
                                               int quantity) throws Exception {
        Stripe.apiKey = secretKey;

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(baseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(baseUrl + "/cart")
            .addLineItem(SessionCreateParams.LineItem.builder()
                .setQuantity((long) quantity)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("inr")
                    .setUnitAmount(priceInCents)
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(productName)
                        .build())
                    .build())
                .build())
            .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    public String createCartCheckoutSession(List<String> names,
                                             List<Long> prices,
                                             List<Long> quantities) throws Exception {
        Stripe.apiKey = secretKey;

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            lineItems.add(SessionCreateParams.LineItem.builder()
                .setQuantity(quantities.get(i))
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("inr")
                    .setUnitAmount(prices.get(i))
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(names.get(i))
                        .build())
                    .build())
                .build());
        }

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(baseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(baseUrl + "/cart")
            .addAllLineItem(lineItems)
            .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    public Session getSession(String sessionId) throws Exception {
        Stripe.apiKey = secretKey;
        return Session.retrieve(sessionId);
    }
}
