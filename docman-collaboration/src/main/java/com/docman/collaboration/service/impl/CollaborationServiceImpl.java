package com.docman.collaboration.service.impl;

import com.docman.collaboration.entity.Comment;
import com.docman.collaboration.entity.UserStatus;
import com.docman.collaboration.service.CollaborationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborationServiceImpl implements CollaborationService {

    private final Map<String, UserStatus> userStatusStore = new ConcurrentHashMap<>();
    private final Map<Long, Comment> commentStore = new ConcurrentHashMap<>();
    private final AtomicLong commentIdCounter = new AtomicLong(1);

    private String getStatusKey(Long userId, Long documentId) {
        return userId + ":" + documentId;
    }

    @Override
    public void updateUserStatus(UserStatus userStatus) {
        String key = getStatusKey(userStatus.getUserId(), userStatus.getDocumentId());
        userStatus.setLastActiveTime(LocalDateTime.now());
        userStatusStore.put(key, userStatus);
        log.info("User {} status updated to {} on document {}",
                userStatus.getUserId(), userStatus.getStatus(), userStatus.getDocumentId());
    }

    @Override
    public UserStatus getUserStatus(Long userId, Long documentId) {
        return userStatusStore.get(getStatusKey(userId, documentId));
    }

    @Override
    public List<UserStatus> getOnlineUsers(Long documentId) {
        return userStatusStore.values().stream()
                .filter(us -> documentId.equals(us.getDocumentId()) && "ONLINE".equals(us.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserStatus> getAllOnlineUsers() {
        return userStatusStore.values().stream()
                .filter(us -> "ONLINE".equals(us.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public Comment addComment(Comment comment) {
        comment.setId(commentIdCounter.getAndIncrement());
        comment.setType("ANNOTATION");
        comment.setStatus("ACTIVE");
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        commentStore.put(comment.getId(), comment);
        log.info("User {} added comment on document {}", comment.getUserId(), comment.getDocumentId());
        return comment;
    }

    @Override
    public Comment replyComment(Comment comment) {
        comment.setId(commentIdCounter.getAndIncrement());
        comment.setType("REPLY");
        comment.setStatus("ACTIVE");
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        commentStore.put(comment.getId(), comment);
        return comment;
    }

    @Override
    public Comment updateComment(Long id, String content) {
        Comment comment = commentStore.get(id);
        if (comment != null) {
            comment.setContent(content);
            comment.setUpdateTime(LocalDateTime.now());
        }
        return comment;
    }

    @Override
    public void deleteComment(Long id) {
        commentStore.remove(id);
    }

    @Override
    public Comment getComment(Long id) {
        return commentStore.get(id);
    }

    @Override
    public List<Comment> getDocumentComments(Long documentId) {
        return commentStore.values().stream()
                .filter(c -> documentId.equals(c.getDocumentId()) && "ACTIVE".equals(c.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Comment> getCommentReplies(Long parentId) {
        return commentStore.values().stream()
                .filter(c -> parentId.equals(c.getParentId()) && "ACTIVE".equals(c.getStatus()))
                .collect(Collectors.toList());
    }
}
