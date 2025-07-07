package com.odoo.common.core.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 包含所有实体的通用字段
 *
 * @author odoo
 */
@Data
@Schema(description = "基础实体")
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "更新人ID")
    private Long updateBy;

    @Schema(description = "创建人名称")
    private String createByName;

    @Schema(description = "更新人名称")
    private String updateByName;

    @Schema(description = "版本号（乐观锁）")
    private Integer version;

    @Schema(description = "是否删除（0-未删除 1-已删除）")
    private Integer deleted;

    @Schema(description = "备注")
    private String remarks;
} 