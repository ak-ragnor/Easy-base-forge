package com.example.petstore.delegate;

import com.example.petstore.dto.CreateOwnerRequest;
import com.example.petstore.dto.OwnerDTO;
import com.example.petstore.dto.UpdateOwnerRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

/**
 * @generated
 */
public interface OwnersApiDelegate {
    ResponseEntity<List<OwnerDTO>> listOwners(String email);

    ResponseEntity<OwnerDTO> createOwner(CreateOwnerRequest createOwnerRequest);

    ResponseEntity<OwnerDTO> getOwnerById(UUID id);

    ResponseEntity<OwnerDTO> updateOwner(UUID id, UpdateOwnerRequest updateOwnerRequest);

    ResponseEntity<Void> deleteOwner(UUID id);
}
