package com.collabdoc.project.model;

public class EditMessage {
    private String contentDelta;
    private int cursorPosition;
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

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(int cursorPosition) {
        this.cursorPosition = cursorPosition;
    }
}