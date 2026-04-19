package com.example.bookstore.authors.delegate;

import com.example.bookstore.authors.dto.AuthorDTO;
import com.example.bookstore.authors.dto.CreateAuthorRequest;
import com.example.bookstore.authors.dto.UpdateAuthorRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * @generated
 */
public interface AuthorsApiDelegate {
    Page<AuthorDTO> listAuthors(String name, Pageable pageable);

    AuthorDTO createAuthor(CreateAuthorRequest createAuthorRequest);

    AuthorDTO getAuthorById(UUID id);

    AuthorDTO updateAuthor(UUID id, UpdateAuthorRequest updateAuthorRequest);

    ResponseEntity<Void> deleteAuthor(UUID id);
}
