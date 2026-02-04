package com.problemservice.service;

import com.problemservice.dto.request.EditorialRequest;
import com.problemservice.dto.response.EditorialResponse;

public interface EditorialService {

    EditorialResponse getEditorial(Long problemId);

    EditorialResponse createEditorial(Long problemId, EditorialRequest request, String authorId, String authorName);

    EditorialResponse updateEditorial(Long problemId, EditorialRequest request);

    void deleteEditorial(Long problemId);

    boolean hasEditorial(Long problemId);

    void markHelpful(Long problemId);
}
