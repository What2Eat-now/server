package what.what2eat.domain.store.converter;

import org.springframework.stereotype.Component;
import what.what2eat.domain.store.controller.dto.StoreResponseDTO;
import what.what2eat.domain.store.entity.Store;

@Component
public class StoreConverter {

    public Store toStoreEntity(StoreResponseDTO.StoreInfoDTO storeInfoDTO) {
        return Store.builder()
                .storeApiId(storeInfoDTO.getStoreId())
                .categoryName(storeInfoDTO.getCategoryName())
                .categoryGroupName(storeInfoDTO.getCategoryGroupName())
                .storeAddress(storeInfoDTO.getAddress())
                .storeRoadAddress(storeInfoDTO.getStoreRoadAddress())
                .storeLongitude(storeInfoDTO.getLongitude())
                .storeLatitude(storeInfoDTO.getLatitude())
                .storeName(storeInfoDTO.getStoreName())
                .storeUrl(storeInfoDTO.getStoreUrl())
                .build();
    }

}
