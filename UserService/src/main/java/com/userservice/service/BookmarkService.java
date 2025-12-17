package com.userservice.service;

import com.userservice.dto.request.BookmarkRequest;
import com.userservice.dto.response.BookmarkResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookmarkService {

    BookmarkResponse addBookmark(String userId, BookmarkRequest request);

    void removeBookmark(String userId, Long problemId);

    List<BookmarkResponse> getUserBookmarks(String userId);

    Page<BookmarkResponse> getUserBookmarksPaginated(String userId, Pageable pageable);

    boolean isBookmarked(String userId, Long problemId);

    long getBookmarkCount(String userId);
}
