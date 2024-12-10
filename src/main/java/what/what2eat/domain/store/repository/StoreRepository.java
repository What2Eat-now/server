package what.what2eat.domain.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import what.what2eat.domain.store.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {


}
