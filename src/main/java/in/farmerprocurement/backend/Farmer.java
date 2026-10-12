package in.farmerprocurement.backend;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "farmers", uniqueConstraints = @UniqueConstraint(columnNames = "mobile"))
public class Farmer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;
    @NotBlank
    @Column(nullable = false, unique = true)
    private String mobile;
    @NotBlank
    private String village;

    protected Farmer() {}

    public Farmer(String name, String mobile, String village) {
        this.name = name;
        this.mobile = mobile;
        this.village = village;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getMobile() { return mobile; }
    public String getVillage() { return village; }
}
