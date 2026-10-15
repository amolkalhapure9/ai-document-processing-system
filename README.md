# AI-Powered Intelligent Document Processing System

Upload a PDF or text document and let AI read it for you. The app extracts the text, then uses **Spring AI** to:

- detect the **document type** (Invoice, Resume, Contract, Report, ...)
- write a short **summary**
- list the **key points**
- answer **questions** about the document (only from its content)

Every processed document is kept in a **history** list so you can open it again or delete it.

## Tech Stack

| Layer    | Technology                                   |
|----------|----------------------------------------------|
| Backend  | Java 17, Spring Boot 3, Spring AI (OpenAI)   |
| Parsing  | Apache PDFBox                                |
| Frontend | HTML, CSS, JavaScript (Fetch API)            |
| Build    | Maven                                        |

## How It Works

```
Browser (HTML/CSS/JS)
   │  upload file
   ▼
DocumentController  ──►  DocumentService  ──►  TextExtractor (PDFBox)
                              │
                              ▼
                        Spring AI ChatClient  ──►  OpenAI (gpt-4o-mini)
                              │
                              ▼
                 DocumentAnalysis { documentType, summary, keyPoints }
```

Spring AI's `.entity(DocumentAnalysis.class)` turns the AI's answer straight into a Java record, so no manual JSON parsing is needed.

## Project Structure

```
src/main/java/com/amol/docai
├── DocumentAiApplication.java      # main class
├── controller
│   ├── DocumentController.java     # REST endpoints
│   └── GlobalExceptionHandler.java # returns { "error": "..." }
├── model                           # simple Java records
└── service
    ├── DocumentService.java        # AI calls + in-memory storage
    └── TextExtractor.java          # reads PDF / TXT / MD files
src/main/resources/static           # index.html, style.css, app.js
```

## REST API

| Method | Endpoint                    | Description                              |
|--------|-----------------------------|------------------------------------------|
| POST   | `/api/documents/upload`     | Upload a file (form field `file`)        |
| POST   | `/api/documents/{id}/ask`   | Ask a question `{ "question": "..." }`   |
| GET    | `/api/documents`            | List processed documents                 |
| GET    | `/api/documents/{id}`       | Get one document                         |
| DELETE | `/api/documents/{id}`       | Delete a document                        |

## Run Locally

1. Get an API key from https://platform.openai.com/api-keys
2. Set it as an environment variable

   ```bash
   # Windows (PowerShell)
   $env:OPENAI_API_KEY="your-key"

   # Linux / macOS
   export OPENAI_API_KEY=your-key
   ```

3. Start the app (no Maven install needed, the wrapper downloads it)

   ```bash
   ./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
   ```

4. Open http://localhost:8080

## Notes

- Documents are stored in memory, so the history is cleared when the app restarts.
- Very long documents are trimmed to the first 12,000 characters before being sent to the AI.
- Max upload size is 10 MB.
