package com.problemservice.service;

import com.problemservice.dto.request.TagRequest;
import com.problemservice.dto.response.TagResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TagService {
    TagResponse createTag(TagRequest request);

    TagResponse getTagById(Long id);

    Page<TagResponse> getAllTags(Pageable pageable);

    List<TagResponse> getTagsBySlugs(List<String> slugs);

    TagResponse updateTag(Long id, TagRequest request);

    void deleteTag(Long id);
}