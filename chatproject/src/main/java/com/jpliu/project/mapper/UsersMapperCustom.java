package com.jpliu.project.mapper;

import com.jpliu.project.pojo.Users;
import com.jpliu.project.pojo.vo.FriendRequestVO;
import com.jpliu.project.pojo.vo.MyFriendsVO;
import com.jpliu.project.utils.MyMapper;

import java.util.List;

public interface UsersMapperCustom extends MyMapper<Users> {

    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    public List<MyFriendsVO> queryMyFriends(String userId);

    public void batchUpdateMsgSigned(List<String> msgIdList);
}