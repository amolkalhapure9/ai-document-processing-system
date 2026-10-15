package com.amol.docai.service;

import com.amol.docai.model.DocumentAnalysis;
import com.amol.docai.model.ProcessedDocument;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DocumentService {

    // Large documents are cut to this size so the AI request stays small and cheap
    private static final int MAX_CHARS = 12_000;

    private final ChatClient chatClient;
    private final TextExtractor textExtractor;

    // Simple in-memory storage (data is lost when the app restarts)
    private final Map<Long, ProcessedDocument> documents = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public DocumentService(ChatClient.Builder chatClientBuilder, TextExtractor textExtractor) {
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a helpful assistant that analyses documents. "
                        + "Answer only using the document content you are given.")
                .build();
        this.textExtractor = textExtractor;
    }

    public ProcessedDocument process(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please choose a file to upload");
        }

        String text = textExtractor.extract(file).trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("No text could be read from this file");
        }

        DocumentAnalysis analysis = chatClient.prompt()
                .user(u -> u.text("""
                        Read the document below and return:
                        - documentType: one short label, e.g. Invoice, Resume, Contract, Report, Article
                        - summary: a clear summary in 3 to 5 sentences
                        - keyPoints: the 5 most important points

                        Document:
                        {text}
                        """).param("text", shorten(text)))
                .call()
                .entity(DocumentAnalysis.class);

        long id = nextId.getAndIncrement();
        ProcessedDocument document = new ProcessedDocument(
                id,
                file.getOriginalFilename(),
                countWords(text),
                text,
                analysis,
                LocalDateTime.now());

        documents.put(id, document);
        return document;
    }

    public String ask(Long id, String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Please type a question");
        }

        ProcessedDocument document = findById(id);

        return chatClient.prompt()
                .user(u -> u.text("""
                        Answer the question using only the document below.
                        If the answer is not in the document, say "I could not find that in the document."

                        Document:
                        {text}

                        Question: {question}
                        """)
                        .param("text", shorten(document.text()))
                        .param("question", question))
                .call()
                .content();
    }

    public List<ProcessedDocument> findAll() {
        List<ProcessedDocument> list = new ArrayList<>(documents.values());
        list.sort((a, b) -> b.id().compareTo(a.id())); // newest first
        return list;
    }

    public ProcessedDocument findById(Long id) {
        ProcessedDocument document = documents.get(id);
        if (document == null) {
            throw new IllegalArgumentException("Document not found: " + id);
        }
        return document;
    }

    public void delete(Long id) {
        documents.remove(id);
    }

    private String shorten(String text) {
        return text.length() > MAX_CHARS ? text.substring(0, MAX_CHARS) : text;
    }

    private int countWords(String text) {
        return text.split("\\s+").length;
    }
}
