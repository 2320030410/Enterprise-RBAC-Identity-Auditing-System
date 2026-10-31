package com.enterprise.identityauditor.dto;

public class PermissionDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private boolean elevated;

    public PermissionDto() {
    }

    public PermissionDto(Long id, String name, String description, String category, boolean elevated) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.elevated = elevated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isElevated() {
        return elevated;
    }

    public void setElevated(boolean elevated) {
        this.elevated = elevated;
    }
}
