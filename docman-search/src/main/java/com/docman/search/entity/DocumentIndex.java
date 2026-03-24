package com.docman.search.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("document_index")
public class DocumentIndex {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String documentId;

    private String title;

    private String content;

    private String author;

    private String keywords;

    private LocalDateTime indexedAt;

    private String status;
}
