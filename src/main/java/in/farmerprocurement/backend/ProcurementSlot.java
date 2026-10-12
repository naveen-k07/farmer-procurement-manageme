package in.farmerprocurement.backend;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "procurement_slots")
public class ProcurementSlot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String crop;
    private LocalDate date;
    private LocalTime startTime;
    private int capacity;

    protected ProcurementSlot() {}

    public ProcurementSlot(String crop, LocalDate date, LocalTime startTime, int capacity) {
        this.crop = crop;
        this.date = date;
        this.startTime = startTime;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public String getCrop() { return crop; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public int getCapacity() { return capacity; }
    public void setCrop(String crop) { this.crop = crop; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}
