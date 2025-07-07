package com.newgen.spring.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;


@Data
@ToString
@EqualsAndHashCode
public class DocumentDTO {
    private String createdDate;
    private String lastUpdateDate;
    private String databaseId;
    private String replicaId;
    private String noteId;
    private String documentUniqueId;
//    private String content;
//    private String contentFileName;
    private String form;
    private String userName;
    private List<ItemDTO> itemDTOList;
    private Integer attachmentsDetected;

}
