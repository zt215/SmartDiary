package org.example.back.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.back.pojo.UserBan;

public interface UserBanMapper {

    int insert(UserBan ban);

    UserBan findActiveByUserId(@Param("userId") Integer userId);

    int deleteActiveByUserId(@Param("userId") Integer userId);

    int updateEndTime(@Param("id") Integer id, @Param("endTime") java.util.Date endTime);
}
