package in.farmerprocurement.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, String> {
    long countBySlotId(Long slotId);
    boolean existsByFarmer_IdAndSlot_Id(Long farmerId, Long slotId);
    List<Booking> findByQueueStatusOrderByQueueNumberAsc(String queueStatus);
    List<Booking> findBySlotIdOrderByCreatedAtAsc(Long slotId);
    
    List<Booking> findBySlot_Centre_IdAndQueueNumberIsNotNull(Long centreId);

    List<Booking> findBySlot_Centre_IdAndQueueStatusOrderByQueueNumberAsc(
            Long centreId, String queueStatus);
    
}
