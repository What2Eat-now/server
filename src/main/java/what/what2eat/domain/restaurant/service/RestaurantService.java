package what.what2eat.domain.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import what.what2eat.domain.restaurant.controller.dto.RestaurantRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import what.what2eat.domain.restaurant.converter.RestaurantConverter;
import what.what2eat.domain.restaurant.entity.Restaurant;
import what.what2eat.domain.restaurant.repository.RestaurantRepository;
import what.what2eat.domain.restaurant.controller.dto.RestaurantResponseDTO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class RestaurantService {

    @Value("${kakao.rest.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.rest.api.url}")
    private String kakaoApiUrl;

    private final RestTemplate restTemplate;

    private final RestaurantRepository restaurantRepository;

    private final RestaurantConverter restaurantConverter;

    private final GeometryFactory geometryFactory;


    // 주변 음식점 데이터 조회
    public RestaurantResponseDTO.RestaurantApiResultDTO getNearbyRestaurants(RestaurantRequestDTO.RestaurantGetDTO request) throws URISyntaxException {

        // URI 설정
        URI uri = buildUri(request);

        // Header 셋팅한 HttpEntity 생성
        HttpEntity<Void> entity = createHttpEntity();

        // GET 요청 보내기
        RestaurantResponseDTO.RestaurantApiResultDTO restaurantApiResultDTO = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                RestaurantResponseDTO.RestaurantApiResultDTO.class
        ).getBody();

        // 중복 제거한 가게 리스트 조회
        List<Restaurant> restaurantList = convertAndFilterrestaurants(restaurantApiResultDTO);

        if (!restaurantList.isEmpty()) {
            restaurantRepository.saveAll(restaurantList);
        }else{
            throw new NoSuchElementException("음식점 데이터가 없습니다.");
        }

        return restaurantApiResultDTO;
    }

    // 위치 기반 랜덤 음식점 반환
    public RestaurantResponseDTO.RestaurantInfoDTO getRandomRestaurant(RestaurantRequestDTO.RestaurantGetDTO request) {
        Double longitude = request.getLongitude();
        Double latitude = request.getLatitude();
        Integer distance = request.getDistance() != null ? request.getDistance() : 300;

        // 거리 기반 음식점 리스트 추출
        List<Restaurant> restaurants = restaurantRepository.findByLocationBased(geometryFactory.createPoint(new Coordinate(longitude, latitude)), distance);

        //해당 위치 근처 음식점 리스트가 db에 없을 경우 먼저 API 호출해서 데이터 저장후 db에서 조회
        if(restaurants.isEmpty()){
            try {
                getNearbyRestaurants(request); // 데이터 저장
                restaurants = restaurantRepository.findByLocationBased(
                        geometryFactory.createPoint(new Coordinate(longitude, latitude)), distance);
            } catch (Exception e) {
                throw new NoSuchElementException("해당 위치에서 음식점 데이터를 가져오는 중 문제가 발생했습니다.", e);
            }        }

        // 랜덤 음식점 추출
        return  restaurants.stream()
                .skip((int) (Math.random() * restaurants.size()))
                .findFirst()
                .map(restaurantConverter::toDTO)
                .orElseThrow(() -> new NoSuchElementException("음식점 데이터가 없습니다."));
    }

    // DTO -> ENTITY 변환 및 중복 제거
    private List<Restaurant> convertAndFilterrestaurants(RestaurantResponseDTO.RestaurantApiResultDTO restaurantApiResultDTO) {
        List<String> existingIdList = restaurantRepository.findAllrestaurantApiIdList();

        // 데이터 변환후 저장
        List<Restaurant> restaurantList = restaurantApiResultDTO.getDocuments().stream()
                .map(restaurantConverter::toRestaurantEntity)
                .filter(restaurant -> !existingIdList.contains(restaurant.getRestaurantApiId()))
                .collect(Collectors.toList());
        return restaurantList;
    }

    // uri 빌드
    private URI buildUri(RestaurantRequestDTO.RestaurantGetDTO request) throws URISyntaxException {
        return UriComponentsBuilder.fromHttpUrl(kakaoApiUrl)
                .queryParam("query", "음식점")
                .queryParam("category_group_code", "FD6")
                .queryParam("x", request.getLongitude())
                .queryParam("y", request.getLatitude())
                .queryParam("radius", request.getDistance())
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
