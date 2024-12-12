package what.what2eat.domain.store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import what.what2eat.domain.store.controller.dto.StoreRequestDTO;
import what.what2eat.domain.store.controller.dto.StoreResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import what.what2eat.domain.store.converter.StoreConverter;
import what.what2eat.domain.store.entity.Store;
import what.what2eat.domain.store.repository.StoreRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class StoreService {

    @Value("${kakao.rest.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.rest.api.url}")
    private String kakaoApiUrl;

    private final RestTemplate restTemplate;

    private final StoreRepository storeRepository;

    private final StoreConverter storeConverter;

    public StoreResponseDTO.StoreApiResultDTO getNearbyPlaces(StoreRequestDTO.StoreGetDTO request) throws URISyntaxException {

        // URI 설정
        URI uri = buildUri(request);

        // Header 셋팅한 HttpEntity 생성
        HttpEntity<Void> entity = createHttpEntity();

        // GET 요청 보내기
        StoreResponseDTO.StoreApiResultDTO storeApiResultDTO = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                StoreResponseDTO.StoreApiResultDTO.class
        ).getBody();

        // 데이터 변환후 저장
        List<Store> storeList = storeApiResultDTO.getDocuments().stream()
                .map(storeConverter::toStoreEntity)
                .collect(Collectors.toList());

        storeRepository.saveAll(storeList);

        return storeApiResultDTO;
    }

    // uri 빌드
    private URI buildUri(StoreRequestDTO.StoreGetDTO request) throws URISyntaxException {
        return UriComponentsBuilder.fromHttpUrl(kakaoApiUrl)
                .queryParam("query", "음식점")
                .queryParam("category_group_code", "FD6")
                .queryParam("x", request.getLongitude())
                .queryParam("y", request.getLatitude())
                .queryParam("radius", 1500)
                .queryParam("page", 1)
                .queryParam("size", 15)
                .build()
                .encode()
                .toUri();
    }

    // httpEntity 객체 생성후 header 셋팅
    private HttpEntity<Void> createHttpEntity() {
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }
}
