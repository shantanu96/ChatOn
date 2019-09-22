package dev.shantanu.com.chaton.data.entities;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    private String id;
    private String firstName;
    private String lastName;
    private String emailId;
    private List<String> coverssataionIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public List<String> getCoverssataionIds() {
        return coverssataionIds;
    }

    public void setCoverssataionIds(List<String> coverssataionIds) {
        this.coverssataionIds = coverssataionIds;
    }
}
