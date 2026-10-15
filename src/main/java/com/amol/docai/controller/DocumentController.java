package com.amol.docai.controller;

import com.amol.docai.model.AnswerResponse;
import com.amol.docai.model.ProcessedDocument;
import com.amol.docai.model.QuestionRequest;
import com.amol.docai.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // Upload a file -> extract text -> AI summary, type and key points
    @PostMapping("/upload")
    public ProcessedDocument upload(@RequestParam("file") MultipartFile file) throws IOException {
        return documentService.process(file);
    }

    // Ask a question about an uploaded document
    @PostMapping("/{id}/ask")
    public AnswerResponse ask(@PathVariable Long id, @RequestBody QuestionRequest request) {
        String answer = documentService.ask(id, request.question());
        return new AnswerResponse(request.question(), answer);
    }

    // History of processed documents
    @GetMapping
    public List<ProcessedDocument> findAll() {
        return documentService.findAll();
    }

    @GetMapping("/{id}")
    public ProcessedDocument findById(@PathVariable Long id) {
        return documentService.findById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
