package service.CSFC.CSFC_auth_service.common.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import service.CSFC.CSFC_auth_service.common.client.dto.ExternalFranchiseResponse;

@FeignClient(name = "franchise-service", url = "${application.config.franchise-service-url}")
public interface FranchiseClient {
//disable completely for now since we don't have a franchise service to connect to, we will just mock the data in the service layer
////    @GetMapping("/api/v1/franchises/{id}")
//    ExternalFranchiseResponse getFranchiseById(@PathVariable("id") Long id);
}