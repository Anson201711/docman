package com.docman.document.exception;

public class FolderNotFoundException extends RuntimeException {

    public FolderNotFoundException(String message) {
        super(message);
    }

    public FolderNotFoundException(Long id) {
        super("Folder not found with id: " + id);
    }
}
