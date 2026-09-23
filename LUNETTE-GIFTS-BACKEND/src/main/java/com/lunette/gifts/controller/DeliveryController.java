package com.lunette.gifts.controller;

import com.lunette.gifts.dto.OrderDto;
import com.lunette.gifts.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final OrderService orderService;
    private final BigDecimal maduraiCharge;
    private final BigDecimal outsideCharge;

    public DeliveryController(OrderService orderService,
                              @Value("${lunette.delivery.madurai:70}") BigDecimal maduraiCharge,
                              @Value("${lunette.delivery.outside:100}") BigDecimal outsideCharge) {
        this.orderService = orderService;
        this.maduraiCharge = maduraiCharge;
        this.outsideCharge = outsideCharge;
    }

    @GetMapping("/check-pincode")
    public ResponseEntity<OrderDto.PincodeCheckResponse> checkPincode(@RequestParam("pincode") String pinCode) {
        String cleanedPin = pinCode != null ? pinCode.replaceAll("\\s+", "") : "";
        String zone = orderService.determineDeliveryZone(cleanedPin, null);
        boolean isMadurai = "MADURAI".equalsIgnoreCase(zone);
        BigDecimal charge = isMadurai ? maduraiCharge : outsideCharge;

        String city = isMadurai ? "Madurai" : "Tamil Nadu / Outstation";
        String district = isMadurai ? "Madurai District" : "Other District";
        String state = "Tamil Nadu";

        return ResponseEntity.ok(new OrderDto.PincodeCheckResponse(
                cleanedPin,
                city,
                district,
                state,
                zone,
                charge,
                isMadurai
        ));
    }
}
