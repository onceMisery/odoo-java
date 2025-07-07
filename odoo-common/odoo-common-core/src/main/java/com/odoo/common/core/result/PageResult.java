package com.odoo.common.core.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装
 *
 * @param <T> 数据类型
 * @author odoo
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Long current;

    @Schema(description = "每页大小")
    private Long size;

    @Schema(description = "总页数")
    private Long pages;

    @Schema(description = "是否有上一页")
    private Boolean hasPrevious;

    @Schema(description = "是否有下一页")
    private Boolean hasNext;

    public PageResult() {
        this.records = Collections.emptyList();
        this.total = 0L;
        this.current = 1L;
        this.size = 10L;
        this.pages = 0L;
        this.hasPrevious = false;
        this.hasNext = false;
    }

    public PageResult(List<T> records, Long total, Long current, Long size) {
        this.records = records != null ? records : Collections.emptyList();
        this.total = total != null ? total : 0L;
        this.current = current != null ? current : 1L;
        this.size = size != null ? size : 10L;
        
        // 计算总页数
        this.pages = this.total == 0 ? 0 : (this.total + this.size - 1) / this.size;
        
        // 计算是否有上一页和下一页
        this.hasPrevious = this.current > 1;
        this.hasNext = this.current < this.pages;
    }

    /**
     * 创建空的分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>();
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size) {
        return new PageResult<>(records, total, current, size);
    }

    /**
     * 创建分页结果（从0开始的页码）
     */
    public static <T> PageResult<T> ofZeroBased(List<T> records, Long total, Long page, Long size) {
        return new PageResult<>(records, total, page + 1, size);
    }

    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    /**
     * 判断是否不为空
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    /**
     * 获取记录数量
     */
    public int getRecordCount() {
        return records != null ? records.size() : 0;
    }

    /**
     * 是否为第一页
     */
    public boolean isFirstPage() {
        return current <= 1;
    }

    /**
     * 是否为最后一页
     */
    public boolean isLastPage() {
        return current >= pages;
    }

    /**
     * 获取上一页页码
     */
    public Long getPreviousPage() {
        return hasPrevious ? current - 1 : null;
    }

    /**
     * 获取下一页页码
     */
    public Long getNextPage() {
        return hasNext ? current + 1 : null;
    }

    /**
     * 获取起始记录位置（从1开始）
     */
    public Long getStartRow() {
        return total == 0 ? 0 : (current - 1) * size + 1;
    }

    /**
     * 获取结束记录位置
     */
    public Long getEndRow() {
        return total == 0 ? 0 : Math.min(current * size, total);
    }
} 