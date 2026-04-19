package com.example.petstore.delegate.impl.base;

import com.example.petstore.delegate.OwnersApiDelegate;
import com.example.petstore.dto.CreateOwnerRequest;
import com.example.petstore.dto.OwnerDTO;
import com.example.petstore.dto.UpdateOwnerRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public abstract class OwnersApiDelegateImplBase implements OwnersApiDelegate {
    @Override
    public ResponseEntity<List<OwnerDTO>> listOwners(String email) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<OwnerDTO> createOwner(CreateOwnerRequest createOwnerRequest) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<OwnerDTO> getOwnerById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<OwnerDTO> updateOwner(UUID id, UpdateOwnerRequest updateOwnerRequest) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<Void> deleteOwner(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
