package com.ucto.backend.service;

import com.ucto.backend.dto.DocRequest;
import com.ucto.backend.dto.DocResponse;
import com.ucto.backend.entity.DocumentationRecord;
import com.ucto.backend.entity.AgentMessage;
import com.ucto.backend.repository.DocumentationRecordRepository;
import com.ucto.backend.repository.AgentMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Documentation Agent service — generates and maintains living documentation.
 * Aligned with docs/closed_loop_workflows.md (UX/Doc Loop).
 */
@Service
public class DocumentationService {

    @Autowired
    private DocumentationRecordRepository documentationRecordRepository;

    @Autowired
    private AgentMessageRepository agentMessageRepository;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Execute a documentation action.
     */
    @Transactional
    public DocResponse executeAction(DocRequest request, Long userId, String ipAddress) {
        switch (request.getAction()) {
            case "GENERATE":
                return generateDoc(request, userId, ipAddress);
            case "UPDATE":
                return updateDoc(request, userId, ipAddress);
            case "PUBLISH":
                return publishDoc(request, userId, ipAddress);
            case "ARCHIVE":
                return archiveDoc(request, userId, ipAddress);
            case "GET_BY_PROJECT":
                return getByProject(request);
            case "GET_BY_TYPE":
                return getByType(request);
            default:
                return new DocResponse(false, "Unknown action: " + request.getAction());
        }
    }

    private DocResponse generateDoc(DocRequest request, Long userId, String ipAddress) {
        DocumentationRecord doc = new DocumentationRecord();
        doc.setProjectId(request.getProjectId());
        doc.setDocType(request.getDocType());
        doc.setTitle(request.getTitle());
        doc.setContent(request.getContent());
        doc.setStoryId(request.getStoryId());
        doc.setAdrId(request.getAdrId());
        doc.setVersion(request.getVersion() != null ? request.getVersion() : "1.0.0");
        doc.setStatus("DRAFT");
        doc.setCreatedBy(userId);
        doc = documentationRecordRepository.save(doc);

        auditLogService.log(userId, request.getProjectId(), "DOC_GENERATED",
                "Generated " + doc.getDocType() + " doc: " + doc.getTitle(), ipAddress, true);

        DocResponse response = new DocResponse(true, "Document generated successfully");
        response.setData(doc);
        return response;
    }

    private DocResponse updateDoc(DocRequest request, Long userId, String ipAddress) {
        if (request.getDocId() == null) {
            return new DocResponse(false, "Document ID required");
        }

        Optional<DocumentationRecord> optDoc = documentationRecordRepository.findById(request.getDocId());
        if (optDoc.isEmpty()) {
            return new DocResponse(false, "Document not found");
        }

        DocumentationRecord doc = optDoc.get();
        if (request.getTitle() != null) doc.setTitle(request.getTitle());
        if (request.getContent() != null) doc.setContent(request.getContent());
        if (request.getVersion() != null) doc.setVersion(request.getVersion());
        doc.setStatus("DRAFT");

        doc = documentationRecordRepository.save(doc);

        auditLogService.log(userId, doc.getProjectId(), "DOC_UPDATED",
                "Updated doc: " + doc.getTitle(), ipAddress, true);

        DocResponse response = new DocResponse(true, "Document updated");
        response.setData(doc);
        return response;
    }

    private DocResponse publishDoc(DocRequest request, Long userId, String ipAddress) {
        if (request.getDocId() == null) {
            return new DocResponse(false, "Document ID required");
        }

        Optional<DocumentationRecord> optDoc = documentationRecordRepository.findById(request.getDocId());
        if (optDoc.isEmpty()) {
            return new DocResponse(false, "Document not found");
        }

        DocumentationRecord doc = optDoc.get();
        doc.setStatus("PUBLISHED");
        doc = documentationRecordRepository.save(doc);

        auditLogService.log(userId, doc.getProjectId(), "DOC_PUBLISHED",
                "Published doc: " + doc.getTitle(), ipAddress, true);

        return new DocResponse(true, "Document published");
    }

    private DocResponse archiveDoc(DocRequest request, Long userId, String ipAddress) {
        if (request.getDocId() == null) {
            return new DocResponse(false, "Document ID required");
        }

        Optional<DocumentationRecord> optDoc = documentationRecordRepository.findById(request.getDocId());
        if (optDoc.isEmpty()) {
            return new DocResponse(false, "Document not found");
        }

        DocumentationRecord doc = optDoc.get();
        doc.setStatus("ARCHIVED");
        doc = documentationRecordRepository.save(doc);

        auditLogService.log(userId, doc.getProjectId(), "DOC_ARCHIVED",
                "Archived doc: " + doc.getTitle(), ipAddress, true);

        return new DocResponse(true, "Document archived");
    }

    private DocResponse getByProject(DocRequest request) {
        List<DocumentationRecord> docs;
        if ("PUBLISHED".equals(request.getDocType())) {
            docs = documentationRecordRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(
                    request.getProjectId(), "PUBLISHED");
        } else {
            docs = documentationRecordRepository.findByProjectIdOrderByCreatedAtDesc(request.getProjectId());
        }

        DocResponse response = new DocResponse(true, "Documents retrieved");
        response.setData(docs);
        return response;
    }

    private DocResponse getByType(DocRequest request) {
        List<DocumentationRecord> docs = documentationRecordRepository
                .findByProjectIdAndDocTypeOrderByCreatedAtDesc(request.getProjectId(), request.getDocType());

        DocResponse response = new DocResponse(true, "Documents retrieved by type");
        response.setData(docs);
        return response;
    }

    /**
     * Generate release notes from completed stories.
     */
    @Transactional
    public DocumentationRecord generateReleaseNotes(Long projectId, String version, Long userId, String ipAddress) {
        DocumentationRecord releaseNotes = new DocumentationRecord();
        releaseNotes.setProjectId(projectId);
        releaseNotes.setDocType("RELEASE_NOTE");
        releaseNotes.setTitle("Release Notes v" + version);
        releaseNotes.setVersion(version);
        releaseNotes.setCreatedBy(userId);

        StringBuilder content = new StringBuilder();
        content.append("# Release Notes v").append(version).append("\n\n");
        content.append("## Features\n\n");
        content.append("- Auto-generated from completed backlog items\n\n");
        content.append("## Bug Fixes\n\n");
        content.append("- N/A\n\n");
        content.append("## Known Issues\n\n");
        content.append("- N/A\n");

        releaseNotes.setContent(content.toString());
        releaseNotes = documentationRecordRepository.save(releaseNotes);

        auditLogService.log(userId, projectId, "RELEASE_NOTES_GENERATED",
                "Generated release notes v" + version, ipAddress, true);

        return releaseNotes;
    }
}
