package com.penglobe.server.service;

import com.penglobe.server.domain.transport.UserPlaceBookmark;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.BookmarkDto;
import com.penglobe.server.repository.UserPlaceBookmarkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final UserPlaceBookmarkRepository bookmarkRepository;

    @Transactional
    public UserPlaceBookmark create(User user, BookmarkDto dto) {
        UserPlaceBookmark b = UserPlaceBookmark.builder()
                .user(user)
                .bookmarkLabel(dto.getBookmarkLabel())
                .address(dto.getAddress())
                .lat(dto.getLat())
                .lng(dto.getLng())
                .build();
        return bookmarkRepository.save(b);
    }

    public List<UserPlaceBookmark> findByUser(User user) {
        return bookmarkRepository.findByUser(user);
    }

    public UserPlaceBookmark get(Long id) {
        return bookmarkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("북마크를 찾을 수 없습니다."));
    }

    @Transactional
    public UserPlaceBookmark update(Long id, BookmarkDto dto) {
        UserPlaceBookmark b = get(id);
        b.setBookmarkLabel(dto.getBookmarkLabel());
        b.setAddress(dto.getAddress());
        b.setLat(dto.getLat());
        b.setLng(dto.getLng());
        return b;
    }

    @Transactional
    public void delete(Long id) {
        bookmarkRepository.deleteById(id);
    }
}

