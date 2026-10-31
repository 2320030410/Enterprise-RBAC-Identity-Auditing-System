package com.enterprise.identityauditor.dto;

public class PrivilegeReviewActionRequest {
    private String reviewNotes;

    public PrivilegeReviewActionRequest() {
    }

    public PrivilegeReviewActionRequest(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
}
