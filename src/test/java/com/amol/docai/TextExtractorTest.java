package com.amol.docai;

import com.amol.docai.service.TextExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextExtractorTest {

    private final TextExtractor extractor = new TextExtractor();

    @Test
    void readsTextFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "Hello Spring AI".getBytes());

        assertEquals("Hello Spring AI", extractor.extract(file));
    }

    @Test
    void rejectsUnsupportedFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", new byte[]{1, 2, 3});

        assertThrows(IllegalArgumentException.class, () -> extractor.extract(file));
    }
}
