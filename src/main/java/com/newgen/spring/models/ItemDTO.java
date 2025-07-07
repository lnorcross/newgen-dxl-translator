package com.newgen.spring.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@EqualsAndHashCode
public class ItemDTO {

    private String key;
    private String value;
    private String type;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ItemAttr> attr;

}
