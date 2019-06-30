package com.nowcoder.dao;

import com.nowcoder.model.Question;
import com.nowcoder.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionDAO {

    @Insert("insert into question(title,content,created_date,user_id,comment_count) " +
            "values(#{title},#{content},#{createdDate},#{userId},#{commentCount})")
    int addQuestion(Question question);


    List<Question> selectLatestQuestions(@Param("userId") int userId,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);



}
