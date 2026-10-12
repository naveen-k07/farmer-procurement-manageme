package in.farmerprocurement.backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @Column(length = 40)
    private String id;

    @ManyToOne(optional = false)
    private Farmer farmer;

    @ManyToOne(optional = false)
    private ProcurementSlot slot;

    private Integer queueNumber;
    private String queueStatus = "BOOKED";
    private String procurementStatus = "PENDING";
    private Double quantityKg;
    private String paymentStatus = "PENDING";
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Booking() {}

    public Booking(String id, Farmer farmer, ProcurementSlot slot) {
        this.id = id;
        this.farmer = farmer;
        this.slot = slot;
    }

    public String getId() { return id; }
    public Farmer getFarmer() { return farmer; }
    public ProcurementSlot getSlot() { return slot; }
    public Integer getQueueNumber() { return queueNumber; }
    public String getQueueStatus() { return queueStatus; }
    public String getProcurementStatus() { return procurementStatus; }
    public Double getQuantityKg() { return quantityKg; }
    public String getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setQueueNumber(Integer queueNumber) { this.queueNumber = queueNumber; }
    public void setQueueStatus(String queueStatus) { this.queueStatus = queueStatus; }
    public void setProcurementStatus(String procurementStatus) { this.procurementStatus = procurementStatus; }
    public void setQuantityKg(Double quantityKg) { this.quantityKg = quantityKg; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
