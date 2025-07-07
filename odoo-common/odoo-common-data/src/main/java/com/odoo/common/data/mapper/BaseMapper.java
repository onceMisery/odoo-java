package com.odoo.common.data.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 通用Mapper基类
 * 提供基础的CRUD操作
 *
 * @param <T> 实体类型
 * @author odoo
 */
public interface BaseMapper<T> {

    /**
     * 根据ID查询
     */
    T selectById(@Param("id") Long id);

    /**
     * 根据条件查询单个对象
     */
    T selectOne(@Param("params") Map<String, Object> params);

    /**
     * 根据条件查询列表
     */
    List<T> selectList(@Param("params") Map<String, Object> params);

    /**
     * 根据条件查询总数
     */
    Long selectCount(@Param("params") Map<String, Object> params);

    /**
     * 插入记录
     */
    int insert(T entity);

    /**
     * 批量插入
     */
    int insertBatch(@Param("list") List<T> list);

    /**
     * 根据ID更新
     */
    int updateById(T entity);

    /**
     * 根据条件更新
     */
    int updateByParams(@Param("entity") T entity, @Param("params") Map<String, Object> params);

    /**
     * 根据ID删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID批量删除
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据条件删除
     */
    int deleteByParams(@Param("params") Map<String, Object> params);

    /**
     * 根据ID逻辑删除
     */
    int logicDeleteById(@Param("id") Long id, @Param("updateBy") Long updateBy);

    /**
     * 根据ID批量逻辑删除
     */
    int logicDeleteByIds(@Param("ids") List<Long> ids, @Param("updateBy") Long updateBy);

    /**
     * 检查记录是否存在
     */
    boolean exists(@Param("params") Map<String, Object> params);

    /**
     * 检查除了指定ID外是否存在相同记录
     */
    boolean existsExcludeId(@Param("params") Map<String, Object> params, @Param("excludeId") Long excludeId);
} 