package in.farmerprocurement.backend;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProcurementController {
    private final FarmerRepository farmers;
    private final SlotRepository slots;
    private final BookingRepository bookings;

    public ProcurementController(FarmerRepository farmers, SlotRepository slots, BookingRepository bookings) {
        this.farmers = farmers;
        this.slots = slots;
        this.bookings = bookings;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "Farmer Procurement API");
    }

    public record FarmerRequest(@NotBlank String name, @NotBlank String mobile, @NotBlank String village) {}
    public record SlotRequest(@NotBlank String crop, @NotNull LocalDate date,
                              @NotNull LocalTime startTime, @Min(1) int capacity) {}
    public record BookingRequest(@NotNull Long farmerId, @NotNull Long slotId) {}
    public record ProcurementRequest(@NotNull @Positive Double quantityKg,
                                     @NotBlank String procurementStatus,
                                     @NotBlank String paymentStatus) {}

    @PostMapping("/farmers")
    @ResponseStatus(HttpStatus.CREATED)
    public Farmer createFarmer(@Valid @RequestBody FarmerRequest request) {
        String mobile = request.mobile().trim();
        if (farmers.existsByMobile(mobile)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A farmer with this mobile number already exists");
        }
        return farmers.save(new Farmer(request.name().trim(), mobile, request.village().trim()));
    }

    @GetMapping("/farmers")
    public List<Farmer> getFarmers() {
        return farmers.findAll();
    }

    @GetMapping("/farmers/{id}")
    public Farmer getFarmer(@PathVariable Long id) {
        return farmers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farmer not found"));
    }

    @GetMapping("/slots")
    public List<Map<String, Object>> getSlots() {
        return slots.findAll().stream().map(this::slotView).toList();
    }

    @PostMapping("/slots")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createSlot(@Valid @RequestBody SlotRequest request) {
        ProcurementSlot slot = slots.save(new ProcurementSlot(
                request.crop(), request.date(), request.startTime(), request.capacity()));
        return slotView(slot);
    }

    private Map<String, Object> slotView(ProcurementSlot slot) {
        long booked = bookings.countBySlotId(slot.getId());
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", slot.getId());
        view.put("crop", slot.getCrop());
        view.put("date", slot.getDate());
        view.put("startTime", slot.getStartTime());
        view.put("capacity", slot.getCapacity());
        view.put("bookedCount", booked);
        view.put("availableCount", Math.max(0, slot.getCapacity() - booked));
        return view;
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Booking createBooking(@Valid @RequestBody BookingRequest request) {
        Farmer farmer = farmers.findById(request.farmerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farmer not found"));
        ProcurementSlot slot = slots.findById(request.slotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));

        if (bookings.countBySlotId(slot.getId()) >= slot.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This slot is full. Please choose another slot.");
        }
        String bookingId = "FPM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return bookings.save(new Booking(bookingId, farmer, slot));
    }

    @GetMapping("/bookings")
    public List<Booking> getBookings() {
        return bookings.findAll();
    }

    @GetMapping("/bookings/{id}")
    public Booking getBooking(@PathVariable String id) {
        return bookings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    @PostMapping("/bookings/{id}/arrive")
    @Transactional
    public Booking markArrival(@PathVariable String id) {
        Booking booking = bookings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!"BOOKED".equals(booking.getQueueStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Arrival can only be marked for a booked farmer");
        }
        int nextQueueNumber = bookings.findAll().stream()
                .map(Booking::getQueueNumber).filter(Objects::nonNull)
                .max(Integer::compareTo).orElse(0) + 1;
        booking.setQueueNumber(nextQueueNumber);
        booking.setQueueStatus("WAITING");
        return bookings.save(booking);
    }

    @GetMapping("/queue")
    public List<Booking> getQueue() {
        return bookings.findAll().stream()
                .filter(b -> b.getQueueNumber() != null)
                .sorted(Comparator.comparing(Booking::getQueueNumber))
                .toList();
    }

    @PostMapping("/queue/next")
    @Transactional
    public Booking serveNext() {
        boolean someoneServing = bookings.findAll().stream()
                .anyMatch(b -> "SERVING".equals(b.getQueueStatus()));
        if (someoneServing) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A farmer is already being served. Complete that procurement first.");
        }
        Booking next = bookings.findByQueueStatusOrderByQueueNumberAsc("WAITING")
                .stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No farmers are waiting"));
        next.setQueueStatus("SERVING");
        return bookings.save(next);
    }

    @PatchMapping("/bookings/{id}/procurement")
    @Transactional
    public Booking updateProcurement(@PathVariable String id, @Valid @RequestBody ProcurementRequest request) {
        Booking booking = bookings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        Set<String> validProcurement = Set.of("PENDING", "RECEIVED", "COMPLETED", "REJECTED");
        Set<String> validPayment = Set.of("PENDING", "PROCESSING", "PAID");
        String procurementStatus = request.procurementStatus().toUpperCase(Locale.ROOT);
        String paymentStatus = request.paymentStatus().toUpperCase(Locale.ROOT);
        if (!validProcurement.contains(procurementStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid procurementStatus");
        }
        if (!validPayment.contains(paymentStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid paymentStatus");
        }
        booking.setQuantityKg(request.quantityKg());
        booking.setProcurementStatus(procurementStatus);
        booking.setPaymentStatus(paymentStatus);
        if ("COMPLETED".equals(procurementStatus)) {
            booking.setQueueStatus("COMPLETED");
        }
        return bookings.save(booking);
    }
}
