package com.example.petstore.delegate;

import com.example.petstore.dto.CreatePetRequest;
import com.example.petstore.dto.PetDTO;
import com.example.petstore.dto.UpdatePetRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

/**
 * @generated
 */
public interface PetsApiDelegate {
    ResponseEntity<List<PetDTO>> listPets(UUID ownerId, String status);

    ResponseEntity<PetDTO> createPet(CreatePetRequest createPetRequest);

    ResponseEntity<PetDTO> getPetById(UUID id);

    ResponseEntity<PetDTO> updatePet(UUID id, UpdatePetRequest updatePetRequest);

    ResponseEntity<Void> deletePet(UUID id);
}
