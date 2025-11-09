package com.problemservice.service.impl;

import com.problemservice.dto.request.TagRequest;
import com.problemservice.dto.response.TagResponse;
import com.problemservice.entity.Tag;
import com.problemservice.exception.BadRequestException;
import com.problemservice.exception.ResourceNotFoundException;
import com.problemservice.repository.TagRepository;
import com.problemservice.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    @Transactional
    public TagResponse createTag(TagRequest request) {
        if (request == null) {
            throw new BadRequestException("Tag request cannot be null");
        }

        // Create and save the Tag entity
        Tag tag = convertToTag(request);
        tag = tagRepository.save(tag);

        return convertToTagResponse(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse getTagById(Long id) {
        if (id == null) {
            throw new BadRequestException("Tag ID cannot be null");
        }

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        return convertToTagResponse(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagResponse> getAllTags(Pageable pageable) {
        if (pageable == null) {
            throw new BadRequestException("Pageable cannot be null");
        }

        return tagRepository.findAll(pageable)
                .map(this::convertToTagResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTagsBySlugs(List<String> slugs) {
        if (slugs == null || slugs.isEmpty()) {
            throw new BadRequestException("Tag slugs cannot be null or empty");
        }

        List<Tag> tags = tagRepository.findAllBySlugIn(slugs);

        if (tags.isEmpty()) {
            throw new ResourceNotFoundException("No tags found with the provided slugs");
        }

        return tags.stream()
                .map(this::convertToTagResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TagResponse updateTag(Long id, TagRequest request) {
        if (id == null) {
            throw new BadRequestException("Tag ID cannot be null");
        }

        if (request == null) {
            throw new BadRequestException("Tag request cannot be null");
        }

        Tag existingTag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        // Update basic fields
        existingTag.setName(request.getName());
        existingTag.setSlug(request.getSlug());
        existingTag.setDescription(request.getDescription());

        existingTag = tagRepository.save(existingTag);
        return convertToTagResponse(existingTag);
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        if (id == null) {
            throw new BadRequestException("Tag ID cannot be null");
        }

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        // Check if tag is associated with problems
        if (!tag.getProblems().isEmpty()) {
            throw new BadRequestException("Cannot delete tag that is associated with problems");
        }

        tagRepository.delete(tag);
    }

    // Conversion methods
    private Tag convertToTag(TagRequest request) {
        return Tag.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .build();
    }

    private TagResponse convertToTagResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .description(tag.getDescription())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}