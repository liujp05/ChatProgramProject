package com.jpliu.project.service.impl;

import com.jpliu.project.enums.MsgActionEnum;
import com.jpliu.project.enums.MsgSignFlagEnum;
import com.jpliu.project.enums.SearchFriendsStatusEnum;
import com.jpliu.project.mapper.*;
import com.jpliu.project.netty.ChatMsg;
import com.jpliu.project.netty.DataContent;
import com.jpliu.project.netty.UserChannelRelationship;
import com.jpliu.project.pojo.FriendsRequest;
import com.jpliu.project.pojo.MyFriends;
import com.jpliu.project.pojo.Users;
import com.jpliu.project.pojo.vo.FriendRequestVO;
import com.jpliu.project.pojo.vo.MyFriendsVO;
import com.jpliu.project.service.UserService;
import com.jpliu.project.utils.FastDFSClient;
import com.jpliu.project.utils.FileUtils;
import com.jpliu.project.utils.JsonUtils;
import com.jpliu.project.utils.QRCodeUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.beans.Transient;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Sid sid;//id 工具

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private MyFriendsMapper myFriendsMapper;

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Autowired
    private FriendsRequestMapper friendsRequestMapper;

    @Autowired
    private UsersMapperCustom usersMapperCustom;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override public boolean queryUsernameIsExist(String username) {

        Users user = new Users();
        user.setUsername(username);//username 前端传过来的

        Users userResult = usersMapper.selectOne(user);

        return userResult != null ? true : false;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override public Users queryUserForLogin(String username, String password) {

        //使用Example 可以省去 写query 语句
        Example userExample = new Example(Users.class);//和users 相关联的 user example
        Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", password);

        Users result = usersMapper.selectOneByExample(userExample);

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override public Users saveUser(Users user) {

        String userId = sid.nextShort();
        //test_qrcode:[username]
        String qrCodePath = "Jpliu\\Users\\jpliu\\Downloads\\img\\" + userId + "qrcode.png";

        //为每个用户生成一个二维码
        qrCodeUtils.createQRCode(qrCodePath, "test_qrcode:" + user.getUsername());
        MultipartFile qrCodeFile = FileUtils.fileToMultipart(qrCodePath);
        String qrCodeUrl = "";
        try {
            qrCodeUrl = fastDFSClient.uploadQRCode(qrCodeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        user.setQrcode(qrCodeUrl);

        user.setId(userId);

        usersMapper.insert(user);
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override public Users updateUserInfo(Users user) {
        usersMapper.updateByPrimaryKeySelective(user);
        Users result = queryUserById(user.getId());
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private Users queryUserById(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override public Integer preconditionSearchFriends(String myUserId, String friendUsername) {
        Users user = queryUserInfoByUsername(friendUsername);

        // 1. 搜索的用户 如果不存在 那么返回无此用户
        if (user == null) {
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }

        // 2. 搜索的用户 是你自己， 返回不能添加此用户
        if (user.getId().equals(myUserId)) {
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }

        // 3. 搜索的用户 已经是你的好友了， 那么返回该用户已经是你i的好友
        Example myFriendExample = new Example(MyFriends.class);
        Criteria mfc = myFriendExample.createCriteria();
        mfc.andEqualTo("myUserId", myUserId);
        mfc.andEqualTo("myFriendUserId", user.getId());


        MyFriends myFriendsRelationship = myFriendsMapper.selectOneByExample(myFriendExample);

        if (myFriendsRelationship != null) {
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }

        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    /**
     * 根据用户名查询用户对象
     * @param username
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override public Users queryUserInfoByUsername(String username) {
        Example userExample = new Example(Users.class);
        Example.Criteria uc = userExample.createCriteria();
        uc.andEqualTo("username", username);
        return usersMapper.selectOneByExample(userExample);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override public void sendFriendRequest(String myUserId, String friendUsername) {

        //根据用户名把你想添加的朋友的信息 拿出来
        Users friend = queryUserInfoByUsername(friendUsername);

        Example friendRequestExample = new Example(FriendsRequest.class);
        Example.Criteria frc = friendRequestExample.createCriteria();
        frc.andEqualTo("sendUserId", myUserId);
        frc.andEqualTo("acceptUserId", friend.getId());


        FriendsRequest friendsRequest = friendsRequestMapper.selectOneByExample(friendRequestExample);
        if (friendsRequest == null) {
            //如果不是你的还有 并且好友记录没有添加， 则新增好友请求记录
            String requestId = sid.nextShort();
            FriendsRequest request = new FriendsRequest();
            request.setId(requestId);
            request.setSendUserId(myUserId);
            request.setAcceptUserId(friend.getId());
            request.setRequestDateTime(new Date());
            friendsRequestMapper.insert(request);
        }

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
        return usersMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override public void deleteFriendRequest(String sendUserId, String acceptUserId) {
        Example friendRequestExample = new Example(FriendsRequest.class);
        Example.Criteria frc = friendRequestExample.createCriteria();
        frc.andEqualTo("sendUserId", sendUserId);
        frc.andEqualTo("acceptUserId", acceptUserId);

        friendsRequestMapper.deleteByExample(friendRequestExample);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override public void passFriendRequest(String sendUserId, String acceptUserId) {
        saveFriends(sendUserId, acceptUserId);
        saveFriends(acceptUserId, sendUserId);
        deleteFriendRequest(sendUserId, acceptUserId);


        Channel sendChannel = UserChannelRelationship.get(sendUserId);
        if (sendChannel != null) {
            // 使用websocket主动推送消息到请求发起者， 更新他的通讯录列表为最新
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);

            sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override public List<MyFriendsVO> queryMyFriends(String userId) {
        List<MyFriendsVO> myFriends = usersMapperCustom.queryMyFriends(userId);
        return myFriends;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override public String saveMsg(ChatMsg chatMsg) {
        com.jpliu.project.pojo.ChatMsg msgDB = new com.jpliu.project.pojo.ChatMsg();
        String msgId = sid.nextShort();
        msgDB.setId(msgId);
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(chatMsg.getMsg());

        chatMsgMapper.insert(msgDB);

        return msgId;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override public void updateMsgSigned(List<String> msgIdList) {
        usersMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override public List<com.jpliu.project.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {
        Example chatExample = new Example(com.jpliu.project.pojo.ChatMsg.class);
        Example.Criteria chatCriteria = chatExample.createCriteria();
        chatCriteria.andEqualTo("signFlag", 0);
        chatCriteria.andEqualTo("acceptUserId", acceptUserId);
        List<com.jpliu.project.pojo.ChatMsg> result = chatMsgMapper.selectByExample(chatExample);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void saveFriends(String sendUserId, String acceptUserId) {

        MyFriends myFriends = new MyFriends();
        String recordId = sid.nextShort();
        myFriends.setId(recordId);
        myFriends.setMyUserId(sendUserId);
        myFriends.setMyFriendUserId(acceptUserId);

        myFriendsMapper.insert(myFriends);
    }





}
