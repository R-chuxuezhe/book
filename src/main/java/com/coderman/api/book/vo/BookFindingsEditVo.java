package com.coderman.api.book.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class BookFindingsEditVo {

    private Long id;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @NotNull(message = "审核状态不能为空")
    private Integer status;

    @NotNull(message = "书籍ID不能为空")
    private Long bookId;

    @NotNull(message = "创建人不能为空")
    private Long createUser;

    @NotBlank(message = "审核结果不能为空")
    private String findings;
}