const API = "/api/documents";

let currentDocumentId = null;

const fileInput = document.getElementById("fileInput");
const fileLabel = document.getElementById("fileLabel");
const message = document.getElementById("message");

// Show the chosen file name
fileInput.addEventListener("change", () => {
    if (fileInput.files.length > 0) {
        fileLabel.textContent = fileInput.files[0].name;
    }
});

// Upload and analyze
document.getElementById("uploadForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    if (fileInput.files.length === 0) {
        showMessage("Please choose a file first", true);
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);

    const button = document.getElementById("uploadBtn");
    button.disabled = true;
    showMessage("Reading document and asking AI... this can take a few seconds");

    try {
        const response = await fetch(API + "/upload", { method: "POST", body: formData });
        const data = await response.json();

        if (!response.ok) {
            showMessage(data.error, true);
            return;
        }

        showMessage("Done!");
        showResult(data);
        loadHistory();
    } catch (error) {
        showMessage("Could not reach the server", true);
    } finally {
        button.disabled = false;
    }
});

// Ask a question about the current document
document.getElementById("askForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const input = document.getElementById("questionInput");
    const question = input.value.trim();
    if (!question || currentDocumentId === null) return;

    const button = document.getElementById("askBtn");
    button.disabled = true;

    try {
        const response = await fetch(`${API}/${currentDocumentId}/ask`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ question })
        });
        const data = await response.json();

        addAnswer(question, response.ok ? data.answer : data.error);
        input.value = "";
    } catch (error) {
        addAnswer(question, "Could not reach the server");
    } finally {
        button.disabled = false;
    }
});

function showResult(doc) {
    currentDocumentId = doc.id;

    document.getElementById("resultTitle").textContent = doc.fileName;
    document.getElementById("resultType").textContent = doc.analysis.documentType;
    document.getElementById("resultMeta").textContent =
        `${doc.wordCount} words · processed ${new Date(doc.processedAt).toLocaleString()}`;
    document.getElementById("resultSummary").textContent = doc.analysis.summary;

    const points = document.getElementById("resultPoints");
    points.innerHTML = "";
    doc.analysis.keyPoints.forEach(point => {
        const li = document.createElement("li");
        li.textContent = point;
        points.appendChild(li);
    });

    document.getElementById("answers").innerHTML = "";
    document.getElementById("resultCard").classList.remove("hidden");
}

function addAnswer(question, answer) {
    const div = document.createElement("div");
    div.className = "answer";

    const q = document.createElement("strong");
    q.textContent = "Q: " + question;
    const a = document.createElement("p");
    a.textContent = answer;

    div.append(q, a);
    document.getElementById("answers").prepend(div);
}

async function loadHistory() {
    const response = await fetch(API);
    const documents = await response.json();

    const list = document.getElementById("historyList");
    list.innerHTML = "";

    if (documents.length === 0) {
        list.innerHTML = "<li>No documents processed yet.</li>";
        return;
    }

    documents.forEach(doc => {
        const li = document.createElement("li");

        const name = document.createElement("span");
        name.className = "name";
        name.textContent = `${doc.fileName} (${doc.analysis.documentType})`;
        name.onclick = () => showResult(doc);

        const deleteBtn = document.createElement("button");
        deleteBtn.className = "delete";
        deleteBtn.textContent = "Delete";
        deleteBtn.onclick = () => deleteDocument(doc.id);

        li.append(name, deleteBtn);
        list.appendChild(li);
    });
}

async function deleteDocument(id) {
    await fetch(`${API}/${id}`, { method: "DELETE" });
    if (id === currentDocumentId) {
        document.getElementById("resultCard").classList.add("hidden");
        currentDocumentId = null;
    }
    loadHistory();
}

function showMessage(text, isError = false) {
    message.textContent = text;
    message.className = isError ? "message error" : "message";
}

loadHistory();
