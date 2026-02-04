package com.userservice.service.impl;

import com.userservice.dto.request.BookmarkRequest;
import com.userservice.dto.response.BookmarkResponse;
import com.userservice.entity.Bookmark;
import com.userservice.exception.BadRequestException;
import com.userservice.exception.ResourceNotFoundException;
import com.userservice.repository.BookmarkRepository;
import com.userservice.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    @Override
    @Transactional
    public BookmarkResponse addBookmark(String userId, BookmarkRequest request) {
        log.info("Adding bookmark for user {} and problem {}", userId, request.getProblemId());

        if (bookmarkRepository.existsByUserIdAndProblemId(userId, request.getProblemId())) {
            throw new BadRequestException("Problem already bookmarked");
        }

        Bookmark bookmark = Bookmark.builder()
                .userId(userId)
                .problemId(request.getProblemId())
                .problemTitle(request.getProblemTitle())
                .difficulty(request.getDifficulty())
                .note(request.getNote())
                .build();

        Bookmark saved = bookmarkRepository.save(bookmark);
        log.info("Bookmark created with id {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void removeBookmark(String userId, Long problemId) {
        log.info("Removing bookmark for user {} and problem {}", userId, problemId);

        if (!bookmarkRepository.existsByUserIdAndProblemId(userId, problemId)) {
            throw new ResourceNotFoundException("Bookmark not found");
        }

        bookmarkRepository.deleteByUserIdAndProblemId(userId, problemId);
        log.info("Bookmark removed for user {} and problem {}", userId, problemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getUserBookmarks(String userId) {
        log.info("Getting bookmarks for user {}", userId);
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookmarkResponse> getUserBookmarksPaginated(String userId, Pageable pageable) {
        log.info("Getting paginated bookmarks for user {}", userId);
        return bookmarkRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookmarked(String userId, Long problemId) {
        return bookmarkRepository.existsByUserIdAndProblemId(userId, problemId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getBookmarkCount(String userId) {
        return bookmarkRepository.countByUserId(userId);
    }

    private BookmarkResponse mapToResponse(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .problemId(bookmark.getProblemId())
                .problemTitle(bookmark.getProblemTitle())
                .difficulty(bookmark.getDifficulty())
                .note(bookmark.getNote())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
