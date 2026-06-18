package org.example.back.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.back.pojo.Enforcer;

public interface EnforcerMapper {

    Enforcer findById(@Param("id") Integer id);

    Enforcer findByPhone(@Param("phone") String phone);

    Enforcer findByEmail(@Param("email") String email);

    Enforcer findByUid(@Param("uid") Long uid);

   int updatePassword(@Param("id") Integer id, @Param("password") String password);

    java.util.List<Enforcer> findAll();

    int insert(Enforcer enforcer);

    int deleteById(@Param("id") Integer id);

    Enforcer findByUidExcludingId(@Param("uid") Long uid, @Param("excludeId") Integer excludeId);
}
