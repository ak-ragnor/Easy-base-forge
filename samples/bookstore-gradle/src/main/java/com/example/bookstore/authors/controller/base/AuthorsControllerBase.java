package com.example.bookstore.authors.controller.base;

import com.example.bookstore.authors.delegate.AuthorsApiDelegate;
import com.example.bookstore.authors.dto.AuthorDTO;
import com.example.bookstore.authors.dto.CreateAuthorRequest;
import com.example.bookstore.authors.dto.UpdateAuthorRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @generated
 */
public abstract class AuthorsControllerBase {
    private final AuthorsApiDelegate delegate;

    protected AuthorsControllerBase(AuthorsApiDelegate delegate) {
        this.delegate = delegate;
    }

    protected AuthorsApiDelegate getDelegate() {
        return delegate;
    }

    /**
     * <pre>curl -X GET http://localhost:8080/authors</pre>
     */
    @GetMapping("/authors")
    public Page<AuthorDTO> listAuthors(@RequestParam(value = "name", required = false) String name,
            Pageable pageable) {
        return delegate.listAuthors(name, pageable);
    }

    /**
     * <pre>curl -X POST http://localhost:8080/authors \
     *   -H 'Content-Type: application/json' \
     *   -d '{}'</pre>
     */
    @PostMapping("/authors")
    public AuthorDTO createAuthor(
            @Valid @RequestBody(required = true) CreateAuthorRequest createAuthorRequest) {
        return delegate.createAuthor(createAuthorRequest);
    }

    /**
     * <pre>curl -X GET http://localhost:8080/authors/{id}</pre>
     */
    @GetMapping("/authors/{id}")
    public AuthorDTO getAuthorById(@PathVariable("id") UUID id) {
        return delegate.getAuthorById(id);
    }

    /**
     * <pre>curl -X PUT http://localhost:8080/authors/{id} \
     *   -H 'Content-Type: application/json' \
     *   -d '{}'</pre>
     */
    @PutMapping("/authors/{id}")
    public AuthorDTO updateAuthor(@PathVariable("id") UUID id,
            @Valid @RequestBody(required = true) UpdateAuthorRequest updateAuthorRequest) {
        return delegate.updateAuthor(id, updateAuthorRequest);
    }

    /**
     * <pre>curl -X DELETE http://localhost:8080/authors/{id}</pre>
     */
    @DeleteMapping("/authors/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable("id") UUID id) {
        return delegate.deleteAuthor(id);
    }
}
