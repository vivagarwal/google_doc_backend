package com.collabdoc.project.manager;

import com.collabdoc.project.model.CollabDoc;

public class CollabDocState {
    private CollabDoc collabDoc;
    private boolean doc_changed_flag;
    private int connected_clients;

    public CollabDocState(CollabDoc collabDoc , boolean doc_changed_flag, int connected_clients){
        this.collabDoc=collabDoc;
        this.doc_changed_flag=doc_changed_flag;
        this.connected_clients=connected_clients;
    }

    public CollabDoc getCollabDoc() {
        return collabDoc;
    }
    
    public boolean isDoc_changed_flag() {
        return doc_changed_flag;
    }

    public void setDoc_changed_flag(boolean doc_changed_flag) {
        this.doc_changed_flag = doc_changed_flag;
    }

    public int getConnected_clients() {
        return connected_clients;
    }

    public void setConnected_clients(int connected_clients) {
        this.connected_clients = connected_clients;
    }
}