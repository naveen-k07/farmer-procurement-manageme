package in.farmerprocurement.backend;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "procurement_slots")
public class ProcurementSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String crop;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Min(1)
    @Column(nullable = false)
    private int capacity;

    // Connect slot with procurement centre
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centre_id", nullable = false)
    private ProcurementCentre centre;

    protected ProcurementSlot() {
    }

    public ProcurementSlot(
            String crop,
            LocalDate date,
            LocalTime startTime,
            int capacity,
            ProcurementCentre centre) {

        this.crop = crop;
        this.date = date;
        this.startTime = startTime;
        this.capacity = capacity;
        this.centre = centre;
    }

    public Long getId() {
        return id;
    }

    public String getCrop() {
        return crop;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public ProcurementCentre getCentre() {
        return centre;
    }

    public void setCrop(String crop) {
        this.crop = crop;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setCentre(ProcurementCentre centre) {
        this.centre = centre;
    }
}
