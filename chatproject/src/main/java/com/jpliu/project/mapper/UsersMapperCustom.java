package com.jpliu.project.mapper;

import com.jpliu.project.pojo.Users;
import com.jpliu.project.utils.MyMapper;

public interface UsersMapper extends MyMapper<Users> {
    //自己测试使用
    public void updateUserImageFace(Users user);

    public Users selectUserById(String userId);
}