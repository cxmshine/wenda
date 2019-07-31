package com.nowcoder.dao;

import com.nowcoder.model.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentDAO {

    @Insert("insert into comment(user_id,content,created_date,entity_id,entity_type,status) " +
            "values(#{userId},#{content},#{createdDate},#{entityId},#{entityType},#{status})")
    int addComment(Comment comment);

    @Select("select id,user_id,content,created_date,entity_id,entity_type,status from comment " +
            "where entity_id=#{entityId} and entity_type=#{entityType}")
    List<Comment> selectCommentByEntity(@Param("entityId") int entityId,
                                        @Param("entityType") int entityType);

    @Select("select count(id) from comment where entity_id=#{entityId} and " +
            "entity_type=#{entityType}")
    int getCommentCount(@Param("entityId") int entityId,
                        @Param("entityType") int entityType);

    @Update("update comment set status=#{status} where id=#{id}")
    int updateStatus(@Param("id") int id,@Param("status") int status);

    @Select("select id,user_id,content,created_date,entity_id,entity_type,status from comment where id=#{id}")
    Comment getCommentById(int id);

    @Select({"select count(id) from comment where user_id=#{userId}"})
    int getUserCommentCount(int userId);
}
