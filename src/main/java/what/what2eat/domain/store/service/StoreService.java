package what.what2eat.domain.store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
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
import java.util.NoSuchElementException;
import java.util.Random;
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

    private final GeometryFactory geometryFactory;


    // 주변 음식점 데이터 조회
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

        // 중복 제거한 가게 리스트 조회
        List<Store> storeList = convertAndFilterStores(storeApiResultDTO);

        if (!storeList.isEmpty()) {
            storeRepository.saveAll(storeList);
        }else{
            throw new NoSuchElementException("음식점 데이터가 없습니다.");
        }

        return storeApiResultDTO;
    }

    // 위치 기반 랜덤 음식점 반환
    public StoreResponseDTO.StoreInfoDTO getRandomStore(StoreRequestDTO.StoreGetDTO request) {
        Double longitude = request.getLongitude();
        Double latitude = request.getLatitude();
        Integer distance = request.getDistance() != null ? request.getDistance() : 300;

        // 거리 기반 음식점 리스트 추출
        List<Store> stores = storeRepository.findByLocationBased(geometryFactory.createPoint(new Coordinate(longitude, latitude)), distance);

        if(stores.isEmpty()){
            throw new NoSuchElementException("해당 위치에서 조회된 음식점이 없습니다.");
        }

        // 랜덤 음식점 추출
        return  stores.stream()
                .skip((int) (Math.random() * stores.size()))
                .findFirst()
                .map(storeConverter::toDTO)
                .orElseThrow(() -> new NoSuchElementException("음식점 데이터가 없습니다."));
    }

    // DTO -> ENTITY 변환 및 중복 제거
    private List<Store> convertAndFilterStores(StoreResponseDTO.StoreApiResultDTO storeApiResultDTO) {
        List<String> existingIdList = storeRepository.findAllStoreApiIdList();

        // 데이터 변환후 저장
        List<Store> storeList = storeApiResultDTO.getDocuments().stream()
                .map(storeConverter::toStoreEntity)
                .filter(store -> !existingIdList.contains(store.getStoreApiId()))
                .collect(Collectors.toList());
        return storeList;
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
