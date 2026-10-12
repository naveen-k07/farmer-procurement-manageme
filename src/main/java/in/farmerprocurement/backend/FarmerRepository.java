package in.farmerprocurement.backend;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {
    boolean existsByMobile(String mobile);
}
