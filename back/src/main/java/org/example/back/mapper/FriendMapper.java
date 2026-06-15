package org.example.back.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.back.pojo.FriendRequest;
import org.example.back.pojo.FriendUserBrief;

import java.util.List;

public interface FriendMapper {

    int insert(FriendRequest row);

    FriendRequest findById(@Param("id") Integer id);

    FriendRequest findByFromAndTo(@Param("fromUserId") Integer fromUserId, @Param("toUserId") Integer toUserId);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    List<FriendUserBrief> listIncomingPending(@Param("userId") Integer userId);

    List<FriendUserBrief> listOutgoingPending(@Param("userId") Integer userId);

    List<FriendUserBrief> listAcceptedFriends(@Param("userId") Integer userId);

    int deleteById(@Param("id") Integer id);

    int deleteAcceptedBetween(@Param("a") Integer a, @Param("b") Integer b);

    int countAcceptedBetweenUsers(@Param("userId") Integer userId,
                                  @Param("friendUserId") Integer friendUserId);

    int deleteByUserId(@Param("userId") Integer userId);
}
