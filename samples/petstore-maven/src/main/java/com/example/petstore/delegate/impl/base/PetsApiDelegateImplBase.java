package com.example.petstore.delegate.impl.base;

import com.example.petstore.delegate.PetsApiDelegate;
import com.example.petstore.dto.CreatePetRequest;
import com.example.petstore.dto.PetDTO;
import com.example.petstore.dto.UpdatePetRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public abstract class PetsApiDelegateImplBase implements PetsApiDelegate {
    @Override
    public ResponseEntity<List<PetDTO>> listPets(UUID ownerId, String status) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<PetDTO> createPet(CreatePetRequest createPetRequest) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<PetDTO> getPetById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<PetDTO> updatePet(UUID id, UpdatePetRequest updatePetRequest) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<Void> deletePet(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
