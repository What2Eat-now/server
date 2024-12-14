package what.what2eat.domain.store.converter;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Component;
import what.what2eat.domain.store.controller.dto.StoreResponseDTO;
import what.what2eat.domain.store.entity.Store;

@Component
@RequiredArgsConstructor
public class StoreConverter {

    private final GeometryFactory geometryFactory;

    public Store toStoreEntity(StoreResponseDTO.StoreInfoDTO storeInfoDTO) {

        return Store.builder()
                .storeApiId(storeInfoDTO.getStoreId())
                .categoryName(storeInfoDTO.getCategoryName())
                .categoryGroupName(storeInfoDTO.getCategoryGroupName())
                .storeAddress(storeInfoDTO.getAddress())
                .storeRoadAddress(storeInfoDTO.getStoreRoadAddress())
                .location(geometryFactory.createPoint(new Coordinate(storeInfoDTO.getLongitude(), storeInfoDTO.getLatitude())))
                .storeName(storeInfoDTO.getStoreName())
                .storeUrl(storeInfoDTO.getStoreUrl())
                .storeNumber(storeInfoDTO.getStorePhoneNumber())
                .build();
    }

    public StoreResponseDTO.StoreInfoDTO toDTO(Store store) {
        return StoreResponseDTO.StoreInfoDTO.builder()
                .address(store.getStoreAddress())
                .longitude(store.getLocation().getX())
                .latitude(store.getLocation().getY())
                .storeName(store.getStoreName())
                .storePhoneNumber(store.getStoreNumber())
                .storeUrl(store.getStoreUrl())
                .storeId(store.getStoreApiId())
                .build();
    }
}
