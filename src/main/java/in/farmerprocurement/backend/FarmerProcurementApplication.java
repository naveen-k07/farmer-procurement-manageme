
package in.farmerprocurement.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalTime;

@SpringBootApplication
public class FarmerProcurementApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmerProcurementApplication.class, args);
    }

    @Bean
    CommandLineRunner initializeData(
            ProcurementCentreRepository centreRepository,
            SlotRepository slotRepository) {

        return args -> {

            // Create a demo centre if no centre exists
            ProcurementCentre centre;

            if (centreRepository.count() == 0) {

                centre = new ProcurementCentre(
                        "Krishnagiri Procurement Centre",
                        "Main Road",
                        "Krishnagiri",
                        "Krishnagiri");

                centre = centreRepository.save(centre);

            } else {
                centre = centreRepository.findAll().get(0);
            }

            // Create demo slots if no slots exist
            if (slotRepository.count() == 0) {

                LocalDate tomorrow = LocalDate.now().plusDays(1);

                slotRepository.save(
                        new ProcurementSlot(
                                "Paddy",
                                tomorrow,
                                LocalTime.of(9, 0),
                                20,
                                centre));

                slotRepository.save(
                        new ProcurementSlot(
                                "Paddy",
                                tomorrow,
                                LocalTime.of(11, 0),
                                20,
                                centre));

                slotRepository.save(
                        new ProcurementSlot(
                                "Paddy",
                                tomorrow,
                                LocalTime.of(14, 0),
                                20,
                                centre));
            }
        };
    }
}
