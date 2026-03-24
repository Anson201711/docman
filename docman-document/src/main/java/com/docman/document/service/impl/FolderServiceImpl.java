package com.docman.document.service.impl;

import com.docman.document.dto.FolderContentResponse;
import com.docman.document.dto.FolderCreateRequest;
import com.docman.document.dto.FolderUpdateRequest;
import com.docman.document.entity.Document;
import com.docman.document.entity.Folder;
import com.docman.document.repository.DocumentRepository;
import com.docman.document.repository.FolderRepository;
import com.docman.document.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;

    @Override
    public FolderContentResponse getFolderContent(Long id) {
        FolderContentResponse response = new FolderContentResponse();

        Folder folder;
        if (id == null || id == 0) {
            folder = new Folder();
            folder.setId(0L);
            folder.setName("Root");
            folder.setPath("/");
            folder.setParentId(null);
        } else {
            folder = folderRepository.selectById(id);
            if (folder == null) {
                throw new RuntimeException("Folder not found: " + id);
            }
        }

        response.setFolderId(folder.getId());
        response.setFolderName(folder.getName());
        response.setFolderPath(folder.getPath());

        List<Folder> subFolders = folderRepository.findByParentId(folder.getId());
        List<Document> documents = documentRepository.findByFolderId(folder.getId());

        response.setSubFolders(subFolders);
        response.setDocuments(documents);

        return response;
    }

    @Override
    @Cacheable(value = "folder", key = "#id")
    public Folder getFolderById(Long id) {
        if (id == null || id == 0) {
            Folder root = new Folder();
            root.setId(0L);
            root.setName("Root");
            root.setPath("/");
            return root;
        }
        return folderRepository.selectById(id);
    }

    @Override
    public List<Folder> getRootFolders(String ownerId) {
        return folderRepository.findByParentIdAndOwnerId(0L, ownerId);
    }

    @Override
    public List<Folder> getSubFolders(Long parentId) {
        return folderRepository.findByParentId(parentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Folder createFolder(FolderCreateRequest request) {
        Folder folder = new Folder();
        folder.setName(request.getName());
        folder.setParentId(request.getParentId());
        folder.setOwnerId(request.getOwnerId());
        folder.setSortOrder(0);

        String path = buildFolderPath(request.getParentId());
        folder.setPath(path + "/" + request.getName());

        folderRepository.insert(folder);
        log.info("Created folder: {} with path: {}", folder.getId(), folder.getPath());
        return folder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "folder", key = "#id")
    public Folder updateFolder(Long id, FolderUpdateRequest request) {
        Folder folder = folderRepository.selectById(id);
        if (folder == null) {
            throw new RuntimeException("Folder not found: " + id);
        }

        if (request.getName() != null) {
            String oldPath = folder.getPath();
            folder.setName(request.getName());

            String parentPath = buildParentPath(id);
            folder.setPath(parentPath + "/" + request.getName());

            updateChildPaths(oldPath, folder.getPath());
        }

        if (request.getSortOrder() != null) {
            folder.setSortOrder(request.getSortOrder());
        }

        folderRepository.updateById(folder);
        log.info("Updated folder: {}", id);
        return folder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "folder", key = "#id")
    public void deleteFolder(Long id) {
        Folder folder = folderRepository.selectById(id);
        if (folder == null) {
            throw new RuntimeException("Folder not found: " + id);
        }

        deleteFolderRecursively(id);
        log.info("Deleted folder: {} and all its contents", id);
    }

    @Override
    public String buildFolderPath(Long folderId) {
        if (folderId == null || folderId == 0) {
            return "";
        }

        List<Folder> folders = getAllParentFolders(folderId);
        StringBuilder path = new StringBuilder();

        for (int i = folders.size() - 1; i >= 0; i--) {
            path.append("/").append(folders.get(i).getName());
        }

        return path.toString();
    }

    @Override
    public List<Folder> getAllParentFolders(Long folderId) {
        List<Folder> parents = new ArrayList<>();
        Long currentId = folderId;

        while (currentId != null && currentId != 0) {
            Folder folder = folderRepository.selectById(currentId);
            if (folder != null) {
                parents.add(folder);
                currentId = folder.getParentId();
            } else {
                break;
            }
        }

        return parents;
    }

    private void deleteFolderRecursively(Long folderId) {
        List<Folder> subFolders = folderRepository.findByParentId(folderId);
        for (Folder subFolder : subFolders) {
            deleteFolderRecursively(subFolder.getId());
        }

        List<Document> documents = documentRepository.findByFolderId(folderId);
        for (Document document : documents) {
            documentRepository.deleteById(document.getId());
        }

        folderRepository.deleteById(folderId);
    }

    private String buildParentPath(Long folderId) {
        if (folderId == null || folderId == 0) {
            return "";
        }

        Folder folder = folderRepository.selectById(folderId);
        if (folder == null) {
            return "";
        }

        return folder.getPath();
    }

    private void updateChildPaths(String oldPath, String newPath) {
        List<Folder> allFolders = folderRepository.findByOwnerId("");
        for (Folder folder : allFolders) {
            if (folder.getPath().startsWith(oldPath + "/")) {
                String updatedPath = folder.getPath().replace(oldPath, newPath);
                folder.setPath(updatedPath);
                folderRepository.updateById(folder);
            }
        }

        List<Document> allDocs = documentRepository.findByOwnerId("");
        for (Document doc : allDocs) {
            if (doc.getPath() != null && doc.getPath().startsWith(oldPath + "/")) {
                String updatedPath = doc.getPath().replace(oldPath, newPath);
                doc.setPath(updatedPath);
                documentRepository.updateById(doc);
            }
        }
    }
}
