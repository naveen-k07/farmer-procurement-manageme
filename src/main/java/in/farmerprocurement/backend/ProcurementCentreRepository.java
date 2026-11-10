
package in.farmerprocurement.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcurementCentreRepository
        extends JpaRepository<ProcurementCentre, Long> {

    // Find centres by village
    List<ProcurementCentre> findByVillageIgnoreCase(String village);

    // Find active procurement centres
    List<ProcurementCentre> findByActiveTrue();

    // Find centres by district
    List<ProcurementCentre> findByDistrictIgnoreCase(String district);
}
