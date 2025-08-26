package com.penglobe.server.repository;

import com.penglobe.server.domain.transport.UserPlaceBookmark;
import com.penglobe.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPlaceBookmarkRepository extends JpaRepository<UserPlaceBookmark, Long> {
    List<UserPlaceBookmark> findByUser(User user);
}
