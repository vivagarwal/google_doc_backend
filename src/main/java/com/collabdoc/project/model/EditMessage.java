package com.collabdoc.project.model;

public class EditMessage {
    private String contentDelta;
    private int lineNumber; // ✅ Line number where the edit happens
    private int columnNumber; // ✅ Column number where the edit happens
    private String sessionId;
    private boolean deleteOperation;

    public boolean getDeleteOperation() {
        return deleteOperation;
    }

    public void setDeleteOperation(Boolean deleteOperation) {
        this.deleteOperation = deleteOperation;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getContentDelta() {
        return contentDelta;
    }

    public void setContentDelta(String contentDelta) {
        this.contentDelta = contentDelta;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }
}
