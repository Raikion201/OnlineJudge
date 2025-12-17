package com.userservice.controller;

import com.userservice.dto.request.BookmarkRequest;
import com.userservice.dto.response.BookmarkResponse;
import com.userservice.service.BookmarkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private static final String USER_HEADER = "X-User-Id";
    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<BookmarkResponse> addBookmark(
            @RequestHeader(USER_HEADER) String userId,
            @Valid @RequestBody BookmarkRequest request) {
        BookmarkResponse response = bookmarkService.addBookmark(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> removeBookmark(
            @RequestHeader(USER_HEADER) String userId,
            @PathVariable Long problemId) {
        bookmarkService.removeBookmark(userId, problemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BookmarkResponse>> getBookmarks(
            @RequestHeader(USER_HEADER) String userId) {
        return ResponseEntity.ok(bookmarkService.getUserBookmarks(userId));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<BookmarkResponse>> getBookmarksPaginated(
            @RequestHeader(USER_HEADER) String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookmarkService.getUserBookmarksPaginated(userId, pageable));
    }

    @GetMapping("/check/{problemId}")
    public ResponseEntity<Map<String, Boolean>> checkBookmark(
            @RequestHeader(USER_HEADER) String userId,
            @PathVariable Long problemId) {
        boolean isBookmarked = bookmarkService.isBookmarked(userId, problemId);
        return ResponseEntity.ok(Map.of("bookmarked", isBookmarked));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getBookmarkCount(
            @RequestHeader(USER_HEADER) String userId) {
        long count = bookmarkService.getBookmarkCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
