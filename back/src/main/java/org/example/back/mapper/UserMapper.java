package org.example.back.mapper;

import org.example.back.pojo.AdminUserRow;
import org.apache.ibatis.annotations.Param;
import org.example.back.pojo.User;
import java.util.List;


public interface UserMapper {
    
    /**
     * 插入用户信息
     */
    int insert(User user);
    
    /**
     * 根据用户名查询用户
     */
    User findByName(String name);
    
    /**
     * 根据手机号查询用户
     */
    User findByPhone(String phone);
    
    /**
     * 根据ID查询用户
     */
    User findById(Integer id);
    
    /**
     * 更新用户信息
     */
    int update(User user);
    
    /**
     * 删除用户
     */
    int deleteById(Integer id);
    
    /**
     * 查询所有用户
     */
    List<User> findAll();

    Long selectMaxUid();

    User findByEmail(String email);

    User findByUid(Long uid);

    List<AdminUserRow> selectAllForAdmin();

    int anonymizeUser(@Param("id") Integer id, @Param("invalidPassword") String invalidPassword);

    int updateByAdmin(User user);
}