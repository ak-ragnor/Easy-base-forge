package com.example.petstore.delegate;

import com.example.petstore.dto.CreateVaccinationRequest;
import com.example.petstore.dto.VaccinationDTO;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

/**
 * @generated
 */
public interface VaccinationsApiDelegate {
    ResponseEntity<List<VaccinationDTO>> listVaccinations(UUID petId);

    ResponseEntity<VaccinationDTO> createVaccination(
            CreateVaccinationRequest createVaccinationRequest);

    ResponseEntity<VaccinationDTO> getVaccinationById(UUID id);

    ResponseEntity<Void> deleteVaccination(UUID id);
}
