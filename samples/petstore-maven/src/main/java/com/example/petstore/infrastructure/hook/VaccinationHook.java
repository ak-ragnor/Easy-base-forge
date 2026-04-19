package com.example.petstore.infrastructure.hook;

import com.example.petstore.domain.model.Vaccination;
import com.example.petstore.infrastructure.hook.base.VaccinationHookBase;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Lifecycle hook for {@code Vaccination}.
 *
 * <p>Defaults nextDueDate to one year after administeredDate when not provided.
 */
@Component
public class VaccinationHook implements VaccinationHookBase {

    private static final Logger log = LoggerFactory.getLogger(VaccinationHook.class);

    @Override
    public void beforeSave(Vaccination vaccination) {
        if (vaccination.getAdministeredDate() != null && vaccination.getNextDueDate() == null) {
            vaccination.setNextDueDate(vaccination.getAdministeredDate().plusYears(1));
        }
    }

    @Override
    public void afterSave(Vaccination vaccination) {
        log.debug(
                "Vaccination saved: id={}, vaccine={}, pet={}",
                vaccination.getId(),
                vaccination.getVaccineName(),
                vaccination.getPetId());
    }
}
