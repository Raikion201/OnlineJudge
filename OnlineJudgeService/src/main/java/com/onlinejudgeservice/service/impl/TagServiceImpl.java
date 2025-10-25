package com.onlinejudgeservice.service.impl;

import com.onlinejudgeservice.dto.request.TagRequest;
import com.onlinejudgeservice.dto.response.TagResponse;
import com.onlinejudgeservice.entity.Tag;
import com.onlinejudgeservice.exception.BadRequestException;
import com.onlinejudgeservice.exception.ResourceNotFoundException;
import com.onlinejudgeservice.mapper.TagMapper;
import com.onlinejudgeservice.repository.TagRepository;
import com.onlinejudgeservice.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    public TagResponse createTag(TagRequest request) {
        Tag tag = tagMapper.toEntity(request);
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toResponse(savedTag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        return tagMapper.toResponse(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagResponse> getAllTags(Pageable pageable) {
        return tagRepository.findAll(pageable)
                .map(tagMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTagsBySlugs(List<String> slugs) {
        List<Tag> tags = tagRepository.findAllBySlugIn(slugs);

        if (tags.isEmpty()) {
            throw new ResourceNotFoundException("No tags found with the provided slugs");
        }

        return tags.stream()
                .map(tagMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TagResponse updateTag(Long id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        tagMapper.updateEntity(request, tag);
        Tag updatedTag = tagRepository.save(tag);
        return tagMapper.toResponse(updatedTag);
    }

    @Override
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        if (!tag.getProblems().isEmpty()) {
            throw new BadRequestException("Cannot delete tag that is associated with problems");
        }

        tagRepository.delete(tag);
    }
}