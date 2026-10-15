package com.amol.docai.model;

import java.util.List;

/**
 * What the AI returns after reading a document.
 * Spring AI converts the AI's JSON answer directly into this record.
 */
public record DocumentAnalysis(
        String documentType,
        String summary,
        List<String> keyPoints) {
}
