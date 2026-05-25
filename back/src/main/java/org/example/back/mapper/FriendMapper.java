package org.example.back.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.back.pojo.FriendRequest;
import org.example.back.pojo.FriendUserBrief;

import java.util.List;

@Mapper
public interface FriendMapper {

    @Insert("INSERT INTO friend_request(from_user_id, to_user_id, status) VALUES(#{fromUserId}, #{toUserId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FriendRequest row);

    @Select("SELECT * FROM friend_request WHERE id = #{id}")
    @Results(id = "FriendRequestMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "fromUserId", column = "from_user_id"),
            @Result(property = "toUserId", column = "to_user_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    FriendRequest findById(@Param("id") Integer id);

    @Select("SELECT * FROM friend_request WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId}")
    @ResultMap("FriendRequestMap")
    FriendRequest findByFromAndTo(@Param("fromUserId") Integer fromUserId, @Param("toUserId") Integer toUserId);

    @Update("UPDATE friend_request SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    @Select("""
            SELECT fr.id AS requestId,
                   u.id AS id,
                   u.name AS name,
                   CASE WHEN u.hide_phone = 1 THEN NULL ELSE u.phone END AS phone,
                   u.avatar AS avatar
            FROM friend_request fr
            JOIN user u ON u.id = fr.from_user_id
            WHERE fr.to_user_id = #{userId} AND fr.status = 0
            ORDER BY fr.create_time DESC
            """)
    List<FriendUserBrief> listIncomingPending(@Param("userId") Integer userId);

    @Select("""
            SELECT fr.id AS requestId,
                   u.id AS id,
                   u.name AS name,
                   CASE WHEN u.hide_phone = 1 THEN NULL ELSE u.phone END AS phone,
                   u.avatar AS avatar
            FROM friend_request fr
            JOIN user u ON u.id = fr.to_user_id
            WHERE fr.from_user_id = #{userId} AND fr.status = 0
            ORDER BY fr.create_time DESC
            """)
    List<FriendUserBrief> listOutgoingPending(@Param("userId") Integer userId);

    @Select("""
            SELECT fr.id AS requestId,
                   CASE WHEN fr.from_user_id = #{userId} THEN fr.to_user_id ELSE fr.from_user_id END AS id,
                   u.name AS name,
                   CASE WHEN u.hide_phone = 1 THEN NULL ELSE u.phone END AS phone,
                   u.avatar AS avatar,
                   (SELECT MAX(dc.create_time)
                    FROM diary_circle dc
                    WHERE dc.user_id = CASE WHEN fr.from_user_id = #{userId} THEN fr.to_user_id ELSE fr.from_user_id END
                   ) AS latestDiaryTime
            FROM friend_request fr
            JOIN user u ON u.id = CASE WHEN fr.from_user_id = #{userId} THEN fr.to_user_id ELSE fr.from_user_id END
            WHERE fr.status = 1 AND (fr.from_user_id = #{userId} OR fr.to_user_id = #{userId})
            ORDER BY fr.update_time DESC
            """)
    List<FriendUserBrief> listAcceptedFriends(@Param("userId") Integer userId);

    @Delete("DELETE FROM friend_request WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Delete("""
            DELETE FROM friend_request
            WHERE status = 1
              AND ((from_user_id = #{a} AND to_user_id = #{b}) OR (from_user_id = #{b} AND to_user_id = #{a}))
            """)
    int deleteAcceptedBetween(@Param("a") Integer a, @Param("b") Integer b);

    @Select("""
            SELECT COUNT(1) FROM friend_request
            WHERE status = 1
              AND ((from_user_id = #{userId} AND to_user_id = #{friendUserId})
                OR (from_user_id = #{friendUserId} AND to_user_id = #{userId}))
            """)
    int countAcceptedBetweenUsers(@Param("userId") Integer userId,
                                  @Param("friendUserId") Integer friendUserId);
}
