package com.collabdoc.project.model;

public class CRDTCharacter {
    private String value;
    private String uniqueId;
    private boolean isDeleted;

    public CRDTCharacter(String value,String uniqueId){
        this.value=value;
        this.uniqueId=uniqueId;
        isDeleted=false;
    }

    public String getValue() {
        return value;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void delete(){
        isDeleted=true;
    }
    
}
