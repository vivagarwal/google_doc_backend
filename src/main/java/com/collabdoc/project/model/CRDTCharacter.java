package com.collabdoc.project.model;

public class CRDTCharacter {
    private String value;
    private String uniqueId;

    public CRDTCharacter(String value,String uniqueId){
        this.value=value;
        this.uniqueId=uniqueId;
    }

    public String getValue() {
        return value;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public String toString()
    {
        return ""+value;
    }
    
}
