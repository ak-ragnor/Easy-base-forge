package com.easybase.forge.core.writer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable summary of what happened during a generation run. */
public final class GenerationReport {

    private final List<String> created;
    private final List<String> updated;
    private final List<String> skipped;
    private final List<String> errors;

    private GenerationReport(List<String> created, List<String> updated,
                              List<String> skipped, List<String> errors) {
        this.created = Collections.unmodifiableList(created);
        this.updated = Collections.unmodifiableList(updated);
        this.skipped = Collections.unmodifiableList(skipped);
        this.errors  = Collections.unmodifiableList(errors);
    }

    public List<String> created() { return created; }
    public List<String> updated() { return updated; }
    public List<String> skipped() { return skipped; }
    public List<String> errors()  { return errors; }
    public boolean hasErrors()    { return !errors.isEmpty(); }

    public String errorSummary() {
        return String.join("; ", errors);
    }

    @Override
    public String toString() {
        return String.format(
                "GenerationReport{created=%d, updated=%d, skipped=%d, errors=%d}",
                created.size(), updated.size(), skipped.size(), errors.size());
    }

    // -------------------------------------------------------------------------

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final List<String> created = new ArrayList<>();
        private final List<String> updated = new ArrayList<>();
        private final List<String> skipped = new ArrayList<>();
        private final List<String> errors  = new ArrayList<>();

        public Builder created(String path) { created.add(path); return this; }
        public Builder updated(String path) { updated.add(path); return this; }
        public Builder skipped(String path) { skipped.add(path); return this; }
        public Builder error(String msg)    { errors.add(msg);   return this; }

        public GenerationReport build() {
            return new GenerationReport(created, updated, skipped, errors);
        }
    }
}
