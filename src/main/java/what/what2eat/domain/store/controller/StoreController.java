package what.what2eat.domain.store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import what.what2eat.domain.store.controller.dto.StoreRequestDTO;
import what.what2eat.domain.store.controller.dto.StoreResponseDTO;
import what.what2eat.domain.store.service.StoreService;
import what.what2eat.global.response.ApiResponse;

import java.net.URISyntaxException;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<StoreResponseDTO.StoreApiResultDTO>> getAllStores(@Valid @RequestBody StoreRequestDTO.StoreGetDTO request) throws URISyntaxException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(storeService.getNearbyPlaces(request)));
    }

    @GetMapping("/random")
    public ResponseEntity<ApiResponse<StoreResponseDTO.StoreInfoDTO>> getRandomStore(@RequestBody StoreRequestDTO.StoreGetDTO request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(storeService.getRandomStore(request)));
    }
}
