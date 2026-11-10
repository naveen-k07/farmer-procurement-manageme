
package in.farmerprocurement.backend;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProcurementController {

        private final FarmerRepository farmerRepository;
        private final SlotRepository slotRepository;
        private final BookingRepository bookingRepository;
        private final ProcurementCentreRepository centreRepository;

        public ProcurementController(
                        FarmerRepository farmerRepository,
                        SlotRepository slotRepository,
                        BookingRepository bookingRepository,
                        ProcurementCentreRepository centreRepository) {

                this.farmerRepository = farmerRepository;
                this.slotRepository = slotRepository;
                this.bookingRepository = bookingRepository;
                this.centreRepository = centreRepository;
        }

        // Request DTOs

        public record FarmerRequest(
                        @NotBlank String name,
                        @NotBlank String mobile,
                        @NotBlank String village) {
        }

        public record SlotRequest(
                        @NotBlank String crop,
                        @NotNull LocalDate date,
                        @NotNull java.time.LocalTime startTime,
                        @Min(1) int capacity,
                        @NotNull Long centreId) {
        }

        public record BookingRequest(
                        @NotNull Long farmerId,
                        @NotNull Long slotId) {
        }

        public record ProcurementRequest(
                        @NotNull @Positive Double quantityKg,
                        @NotBlank String procurementStatus,
                        @NotBlank String paymentStatus) {
        }

        public record CentreRequest(
                        @NotBlank String centreName,
                        @NotBlank String location,
                        @NotBlank String village,
                        @NotBlank String district) {
        }

        public record BookingResponse(
                        String bookingId,
                        Long farmerId,
                        String farmerName,
                        Long slotId,
                        String crop,
                        LocalDate date,
                        java.time.LocalTime startTime,
                        Long centreId,
                        String centreName,
                        Integer queueNumber,
                        String queueStatus,
                        String procurementStatus,
                        Double quantityKg,
                        String paymentStatus) {
        }

        public record SlotResponse(
                        Long id,
                        String crop,
                        LocalDate date,
                        java.time.LocalTime startTime,
                        int capacity,
                        Long centreId,
                        String centreName) {
        }
        // Health check

        @GetMapping("/health")
        public Map<String, String> health() {
                return Map.of("status", "OK");
        }

        // Farmer APIs

        @PostMapping("/farmers")
        @ResponseStatus(HttpStatus.CREATED)
        public Farmer createFarmer(@Valid @RequestBody FarmerRequest request) {

                String mobile = request.mobile().trim();

                if (farmerRepository.existsByMobile(mobile)) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Mobile number already registered");
                }

                Farmer farmer = new Farmer(
                                request.name().trim(),
                                mobile,
                                request.village().trim());

                return farmerRepository.save(farmer);
        }

        @GetMapping("/farmers")
        public List<Farmer> getFarmers() {
                return farmerRepository.findAll();
        }

        @GetMapping("/farmers/{id}")
        public Farmer getFarmer(@PathVariable Long id) {
                return farmerRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Farmer not found"));
        }

        // Procurement Centre APIs

        @PostMapping("/centres")
        @ResponseStatus(HttpStatus.CREATED)
        public ProcurementCentre createCentre(
                        @Valid @RequestBody CentreRequest request) {

                ProcurementCentre centre = new ProcurementCentre(
                                request.centreName().trim(),
                                request.location().trim(),
                                request.village().trim(),
                                request.district().trim());

                return centreRepository.save(centre);
        }

        @GetMapping("/centres")
        public List<ProcurementCentre> getCentres() {
                return centreRepository.findAll();
        }

        @GetMapping("/centres/active")
        public List<ProcurementCentre> getActiveCentres() {
                return centreRepository.findByActiveTrue();
        }

        @GetMapping("/centres/{id}")
        public ProcurementCentre getCentre(@PathVariable Long id) {
                return centreRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Centre not found"));
        }

        // Slot APIs

        @GetMapping("/slots")
        @Transactional(readOnly = true)
        public List<Map<String, Object>> getSlots() {
                List<Map<String, Object>> result = new ArrayList<>();

                for (ProcurementSlot slot : slotRepository.findAll()) {

                        Map<String, Object> data = new LinkedHashMap<>();

                        data.put("id", slot.getId());
                        data.put("crop", slot.getCrop());
                        data.put("date", slot.getDate());
                        data.put("startTime", slot.getStartTime());
                        data.put("capacity", slot.getCapacity());
                        data.put("bookedCount",
                                        bookingRepository.countBySlotId(slot.getId()));
                        data.put("availableCount",
                                        slot.getCapacity()
                                                        - bookingRepository.countBySlotId(slot.getId()));
                        data.put("centreId", slot.getCentre().getId());
                        data.put("centreName", slot.getCentre().getCentreName());

                        result.add(data);
                }

                return result;
        }

        @Transactional
        @PostMapping("/slots")
        @ResponseStatus(HttpStatus.CREATED)
        public SlotResponse createSlot(
                        @Valid @RequestBody SlotRequest request) {
                ProcurementCentre centre = centreRepository.findById(request.centreId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Procurement centre not found"));

                ProcurementSlot slot = new ProcurementSlot(
                                request.crop().trim(),
                                request.date(),
                                request.startTime(),
                                request.capacity(),
                                centre);

                ProcurementSlot savedSlot = slotRepository.save(slot);
                return new SlotResponse(
                                savedSlot.getId(),
                                savedSlot.getCrop(),
                                savedSlot.getDate(),
                                savedSlot.getStartTime(),
                                savedSlot.getCapacity(),
                                savedSlot.getCentre().getId(),
                                savedSlot.getCentre().getCentreName());
        }

        // Booking APIs

        @PostMapping("/bookings")
        @ResponseStatus(HttpStatus.CREATED)
        @Transactional

        public BookingResponse createBooking(
                        @Valid @RequestBody BookingRequest request) {

                Farmer farmer = farmerRepository.findById(request.farmerId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Farmer not found"));

                ProcurementSlot slot = slotRepository.findById(request.slotId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Slot not found"));

                // Check whether the slot date has already passed
                if (slot.getDate().isBefore(LocalDate.now()) ||
                                (slot.getDate().isEqual(LocalDate.now()) &&
                                                !slot.getStartTime().isAfter(java.time.LocalTime.now()))) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Cannot book a slot that has already started");
                }
                // Prevent duplicate bookings
                if (bookingRepository.existsByFarmer_IdAndSlot_Id(
                                farmer.getId(), slot.getId())) {

                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Farmer has already booked this slot");
                }

                // Check slot capacity
                if (bookingRepository.countBySlotId(slot.getId()) >= slot.getCapacity()) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT, "Slot is full");
                }

                String bookingId = "FPM-" +
                                UUID.randomUUID().toString()
                                                .substring(0, 8).toUpperCase();

                Booking booking = new Booking(bookingId, farmer, slot);

                Booking savedBooking = bookingRepository.save(booking);

                return toBookingResponse(savedBooking);
        }

        @GetMapping("/bookings")
        @Transactional(readOnly = true)
        public List<BookingResponse> getBookings() {
                return bookingRepository.findAll()
                                .stream()
                                .map(this::toBookingResponse)
                                .toList();
        }

        @GetMapping("/bookings/{bookingId}")
        @Transactional(readOnly = true)
        public BookingResponse getBooking(@PathVariable String bookingId) {

                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Booking not found"));

                return toBookingResponse(booking);
        }

        // Arrival and queue APIs

        @PostMapping("/bookings/{bookingId}/arrive")
        @Transactional
        public BookingResponse markArrival(@PathVariable String bookingId) {

                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Booking not found"));

                if (!"BOOKED".equals(booking.getQueueStatus())) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT, "Booking is not in BOOKED status");
                }

                Long centreId = booking.getSlot().getCentre().getId();

                int nextQueueNumber = bookingRepository
                                .findBySlot_Centre_IdAndQueueNumberIsNotNull(centreId)
                                .stream()
                                .map(Booking::getQueueNumber)
                                .max(Integer::compareTo)
                                .orElse(0) + 1;

                booking.setQueueNumber(nextQueueNumber);
                booking.setQueueStatus("WAITING");

                return toBookingResponse(bookingRepository.save(booking));
        }

        @GetMapping("/queue")
        @Transactional(readOnly = true)
        public List<BookingResponse> getQueue(@RequestParam Long centreId) {

                centreRepository.findById(centreId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Procurement centre not found"));

                return bookingRepository
                                .findBySlot_Centre_IdAndQueueStatusOrderByQueueNumberAsc(
                                                centreId, "WAITING")
                                .stream()
                                .map(this::toBookingResponse)
                                .toList();
        }

        @PostMapping("/queue/next")
        @Transactional
        public BookingResponse callNext(@RequestParam Long centreId) {

                centreRepository.findById(centreId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Procurement centre not found"));

                List<Booking> serving = bookingRepository
                                .findBySlot_Centre_IdAndQueueStatusOrderByQueueNumberAsc(
                                                centreId, "SERVING");

                if (!serving.isEmpty()) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "A booking is already being served at this centre");
                }

                List<Booking> waiting = bookingRepository
                                .findBySlot_Centre_IdAndQueueStatusOrderByQueueNumberAsc(
                                                centreId, "WAITING");

                if (waiting.isEmpty()) {
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "No farmers waiting at this centre");
                }

                Booking next = waiting.get(0);
                next.setQueueStatus("SERVING");

                return toBookingResponse(bookingRepository.save(next));
        }
        // Procurement and payment status API

        @PatchMapping("/bookings/{bookingId}/procurement")
        @Transactional
        public BookingResponse updateProcurement(
                        @PathVariable String bookingId,
                        @Valid @RequestBody ProcurementRequest request) {

                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Booking not found"));

                // Farmer must be currently serving
                if (!"SERVING".equals(booking.getQueueStatus())) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Procurement can only be updated when farmer is being served");
                }

                String procurementStatus = request.procurementStatus().trim().toUpperCase();

                String paymentStatus = request.paymentStatus().trim().toUpperCase();

                // Validate procurement status
                Set<String> validProcurementStatuses = Set.of("PENDING", "RECEIVED", "COMPLETED", "REJECTED");

                if (!validProcurementStatuses.contains(procurementStatus)) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid procurement status");
                }

                // Validate payment status
                Set<String> validPaymentStatuses = Set.of("PENDING", "PROCESSING", "PAID");

                if (!validPaymentStatuses.contains(paymentStatus)) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid payment status");
                }

                // Payment cannot be PAID before procurement is completed
                if ("PAID".equals(paymentStatus)
                                && !"COMPLETED".equals(procurementStatus)) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Payment can only be marked PAID after procurement is completed");
                }

                booking.setQuantityKg(request.quantityKg());
                booking.setProcurementStatus(procurementStatus);
                booking.setPaymentStatus(paymentStatus);

                // Close the queue when procurement is completed or rejected
                if ("COMPLETED".equals(procurementStatus)
                                || "REJECTED".equals(procurementStatus)) {
                        booking.setQueueStatus("COMPLETED");
                }

                return toBookingResponse(bookingRepository.save(booking));
        }

        private BookingResponse toBookingResponse(Booking booking) {

                ProcurementSlot slot = booking.getSlot();
                ProcurementCentre centre = slot.getCentre();
                Farmer farmer = booking.getFarmer();

                return new BookingResponse(
                                booking.getId(),
                                farmer.getId(),
                                farmer.getName(),
                                slot.getId(),
                                slot.getCrop(),
                                slot.getDate(),
                                slot.getStartTime(),
                                centre.getId(),
                                centre.getCentreName(),
                                booking.getQueueNumber(),
                                booking.getQueueStatus(),
                                booking.getProcurementStatus(),
                                booking.getQuantityKg(),
                                booking.getPaymentStatus());
        }
}