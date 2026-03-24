package com.docman.collaboration.service;

import com.docman.collaboration.entity.Comment;
import com.docman.collaboration.entity.UserStatus;

import java.util.List;

public interface CollaborationService {
    void updateUserStatus(UserStatus userStatus);
    UserStatus getUserStatus(Long userId, Long documentId);
    List<UserStatus> getOnlineUsers(Long documentId);
    List<UserStatus> getAllOnlineUsers();

    Comment addComment(Comment comment);
    Comment replyComment(Comment comment);
    Comment updateComment(Long id, String content);
    void deleteComment(Long id);
    Comment getComment(Long id);
    List<Comment> getDocumentComments(Long documentId);
    List<Comment> getCommentReplies(Long parentId);
}
