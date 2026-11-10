package in.farmerprocurement.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "procurement_centres")
public class ProcurementCentre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String centreName;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String village;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private boolean active = true;

    // Required by JPA
    protected ProcurementCentre() {
    }

    public ProcurementCentre(
            String centreName,
            String location,
            String village,
            String district) {

        this.centreName = centreName;
        this.location = location;
        this.village = village;
        this.district = district;
        this.active = true;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getCentreName() {
        return centreName;
    }

    public String getLocation() {
        return location;
    }

    public String getVillage() {
        return village;
    }

    public String getDistrict() {
        return district;
    }

    public boolean isActive() {
        return active;
    }

    // Setters
    public void setCentreName(String centreName) {
        this.centreName = centreName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
