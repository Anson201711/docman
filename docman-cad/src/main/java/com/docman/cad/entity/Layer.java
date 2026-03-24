package com.docman.cad.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Layer {
    private Long id;
    private Long cadFileId;
    private String layerName;
    private String layerId;
    private Boolean visible;
    private Boolean locked;
    private String color;
    private String lineType;
    private Float lineWeight;
    private Integer deleted;
}
