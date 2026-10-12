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
    CommandLineRunner seedSlots(SlotRepository slots) {
        return args -> {
            if (slots.count() == 0) {
                slots.save(new ProcurementSlot("Paddy", LocalDate.now().plusDays(1), LocalTime.of(9, 0), 20));
                slots.save(new ProcurementSlot("Paddy", LocalDate.now().plusDays(1), LocalTime.of(11, 0), 20));
                slots.save(new ProcurementSlot("Paddy", LocalDate.now().plusDays(1), LocalTime.of(14, 0), 20));
            }
        };
    }
}
