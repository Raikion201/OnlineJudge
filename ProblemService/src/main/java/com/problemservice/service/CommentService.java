package com.problemservice.service;

import com.problemservice.dto.CommentDTO;
import com.problemservice.entity.Comment;
import com.problemservice.mapper.CommentMapper;
import com.problemservice.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public Page<CommentDTO> getCommentsByProblemId(Long problemId, Pageable pageable) {
        return commentRepository.findByProblemIdAndParentIsNullOrderByCreatedAtDesc(problemId, pageable)
                .map(commentMapper::toDTO);
    }

    @Transactional
    public CommentDTO addComment(CommentDTO commentDTO) {
        Comment comment = commentMapper.toEntity(commentDTO);
        
        if (commentDTO.getParentId() != null) {
            Comment parent = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParent(parent);
        }

        // Ensure problemId is set
        if (comment.getProblemId() == null) {
             throw new RuntimeException("Problem ID is required");
        }

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDTO(savedComment);
    }

    @Transactional
    public void deleteComment(Long id, String userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }
        
        commentRepository.delete(comment);
    }
}
