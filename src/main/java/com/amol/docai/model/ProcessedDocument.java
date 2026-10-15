package com.amol.docai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * A document that has been uploaded and analysed.
 * The full text is kept so the user can ask questions about it later,
 * but it is not sent back to the browser.
 */
public record ProcessedDocument(
        Long id,
        String fileName,
        int wordCount,
        @JsonIgnore String text,
        DocumentAnalysis analysis,
        LocalDateTime processedAt) {
}
