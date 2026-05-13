package com.ucto.backend.dto;

/**
 * Request DTO for Documentation Agent operations.
 */
public class DocRequest {
    private Long projectId;
    private String action; // GENERATE, UPDATE, PUBLISH, ARCHIVE
    private String docType; // PRODUCT_SPEC, API_DOC, SETUP_GUIDE, RELEASE_NOTE, ADR
    private String title;
    private String content;
    private Long storyId;
    private Long adrId;
    private String version;
    private Long docId;

    public DocRequest() {}

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getStoryId() { return storyId; }
    public void setStoryId(Long storyId) { this.storyId = storyId; }

    public Long getAdrId() { return adrId; }
    public void setAdrId(Long adrId) { this.adrId = adrId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }
}
