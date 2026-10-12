package in.farmerprocurement.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, String> {
    long countBySlotId(Long slotId);
    List<Booking> findByQueueStatusOrderByQueueNumberAsc(String queueStatus);
    List<Booking> findBySlotIdOrderByCreatedAtAsc(Long slotId);
}
