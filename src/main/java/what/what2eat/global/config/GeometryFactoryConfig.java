package what.what2eat.global.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeometryFactoryConfig {

    /**
     * SRID를 인스턴스 생성시마다 초기화 해주어야 하기에 Config 파일로 관리
     */
    @Bean
    public GeometryFactory GeometryFactoryConfig() {
        return new GeometryFactory(new PrecisionModel(), 4326);
    }
}
