package service.CSFC.CSFC_auth_service.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import service.CSFC.CSFC_auth_service.common.client.dto.ExternalOrderResponse;

import java.util.List;

@FeignClient(name = "order-service", url = "${application.config.order-service-url}")
public interface OrderClient {

    @GetMapping("/api/v1/orders/{id}")
    ExternalOrderResponse getOrderById(@PathVariable("id") String id);

    @GetMapping("/api/v1/orders/customer/{customerId}")
    List<ExternalOrderResponse> getOrdersByCustomer(@PathVariable("customerId") String customerId);
}