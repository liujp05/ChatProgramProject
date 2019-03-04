package com.jpliu.project.service;

import com.jpliu.project.netty.ChatMsg;
import com.jpliu.project.pojo.Users;
import com.jpliu.project.pojo.vo.FriendRequestVO;
import com.jpliu.project.pojo.vo.MyFriendsVO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService {


    /**
     * 判断用户名是否存在
     * @param username
     * @return
     */
    public boolean queryUsernameIsExist(String username);

    /**
     * 查询用户是否存在
     * @param username
     * @param password
     * @return
     */
    public Users queryUserForLogin(String username, String password);


    /**
     * 用户注册
     * @param user
     * @return
     */
    public Users saveUser(Users user);

    /**
     * 修改用户记录
     * @param user
     */
    public Users updateUserInfo(Users user);

    /**
     * 搜索朋友的前置条件
     * @param myUserId
     * @param friendUsername
     * @return
     */
    public Integer preconditionSearchFriends(String myUserId, String friendUsername);

    public Users queryUserInfoByUsername(String username);

    /**
     * 添加好友记录， 保存到数据库
     * @param myUserId
     * @param friendUsername
     */
    public void sendFriendRequest(String myUserId, String friendUsername);

    /**
     * 查询好友请求
     * @param acceptUserId
     * @return
     */
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);


    /**
     * 删除好友请求记录
     * @param sendUserId
     * @param acceptUserId
     */
    public void deleteFriendRequest(String sendUserId, String acceptUserId);

    /**
     * 通过好友请求
     *  1. 保存好友
     *  2. 逆向保存好友
     *  3. 删除好友请求记录
     * @param sendUserId
     * @param acceptUserId
     */
    public void passFriendRequest(String sendUserId, String acceptUserId);


    /**
     * 查询好友列表
     * @param userId
     * @return
     */
    public List<MyFriendsVO> queryMyFriends(String userId);

    /**
     * 保存聊天消息到数据库
     * @param chatMsg
     * @return
     */
    public String saveMsg(ChatMsg chatMsg);

    /**
     * 批量签收消息
     * @param msgIdList
     */
    public void updateMsgSigned(List<String> msgIdList);

    /**
     * 获取未签收消息列表
     * @param acceptUserId
     * @return
     */
    public List<com.jpliu.project.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);
}
