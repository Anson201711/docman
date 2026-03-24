package com.docman.collaboration.controller;

import com.docman.collaboration.entity.Comment;
import com.docman.collaboration.entity.UserStatus;
import com.docman.collaboration.service.CollaborationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collaboration")
@RequiredArgsConstructor
public class CollaborationController {

    private final CollaborationService collaborationService;

    @PostMapping("/status")
    public ResponseEntity<Void> updateUserStatus(@RequestBody UserStatus userStatus) {
        collaborationService.updateUserStatus(userStatus);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<UserStatus> getUserStatus(@RequestParam Long userId, @RequestParam Long documentId) {
        return ResponseEntity.ok(collaborationService.getUserStatus(userId, documentId));
    }

    @GetMapping("/online/{documentId}")
    public ResponseEntity<List<UserStatus>> getOnlineUsers(@PathVariable Long documentId) {
        return ResponseEntity.ok(collaborationService.getOnlineUsers(documentId));
    }

    @GetMapping("/online")
    public ResponseEntity<List<UserStatus>> getAllOnlineUsers() {
        return ResponseEntity.ok(collaborationService.getAllOnlineUsers());
    }

    @PostMapping("/comment")
    public ResponseEntity<Comment> addComment(@RequestBody Comment comment) {
        return ResponseEntity.ok(collaborationService.addComment(comment));
    }

    @PostMapping("/comment/reply")
    public ResponseEntity<Comment> replyComment(@RequestBody Comment comment) {
        return ResponseEntity.ok(collaborationService.replyComment(comment));
    }

    @PutMapping("/comment/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable Long id, @RequestParam String content) {
        return ResponseEntity.ok(collaborationService.updateComment(id, content));
    }

    @DeleteMapping("/comment/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        collaborationService.deleteComment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/comment/{id}")
    public ResponseEntity<Comment> getComment(@PathVariable Long id) {
        return ResponseEntity.ok(collaborationService.getComment(id));
    }

    @GetMapping("/comments/{documentId}")
    public ResponseEntity<List<Comment>> getDocumentComments(@PathVariable Long documentId) {
        return ResponseEntity.ok(collaborationService.getDocumentComments(documentId));
    }

    @GetMapping("/comment/{parentId}/replies")
    public ResponseEntity<List<Comment>> getCommentReplies(@PathVariable Long parentId) {
        return ResponseEntity.ok(collaborationService.getCommentReplies(parentId));
    }
}
