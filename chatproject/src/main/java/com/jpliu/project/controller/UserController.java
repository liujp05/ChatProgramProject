package com.jpliu.project.controller;

import com.jpliu.project.NettyBooter;
import com.jpliu.project.enums.OperatorFriendRequestTypeEnum;
import com.jpliu.project.enums.SearchFriendsStatusEnum;
import com.jpliu.project.pojo.ChatMsg;
import com.jpliu.project.pojo.Users;
import com.jpliu.project.pojo.bo.UsersBO;
import com.jpliu.project.pojo.vo.MyFriendsVO;
import com.jpliu.project.pojo.vo.UsersVO;
import com.jpliu.project.service.UserService;
import com.jpliu.project.utils.FastDFSClient;
import com.jpliu.project.utils.FileUtils;
import com.jpliu.project.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.JpliuJSONResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@RestController
@RequestMapping("u")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(NettyBooter.class);

    @Autowired
    private UserService userService;

    @Autowired
    private FastDFSClient fastDFSClient;

    @PostMapping("/registOrLogin")
    public JpliuJSONResult registOrLogin(@RequestBody Users users) {
        //0. 判断用户名和密码不能为空
        if (StringUtils.isBlank(users.getUsername())
                || StringUtils.isBlank(users.getPassword())) {
            return JpliuJSONResult.errorMsg("用户名或密码不能为空...");
        }

        //1. 判断用户名是否存在 如果存在就登录，如果不存在就注册
        boolean usernameIsExist = userService.queryUsernameIsExist(users.getUsername());
        Users userResult = null;


        if (usernameIsExist) {
            try {
                //1,1 登录
                userResult = userService.queryUserForLogin(users.getUsername(), MD5Utils.getMD5Str(users.getPassword()));
                if (userResult == null) {
                    return JpliuJSONResult.errorMsg("用户名或者密码不正确...");
                }
            } catch (Exception e) {
                logger.error("用户登录失败..." + e.getMessage());
            }
        } else {
            try {
                //1.2 注册
                users.setNickname(users.getUsername());
                users.setFaceImage("");
                users.setFaceImageBig("");
                users.setPassword(MD5Utils.getMD5Str(users.getPassword()));
                userResult = userService.saveUser(users);

            } catch (Exception e) {
                logger.error("用户创建失败..." + e.getMessage());
            }
        }

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult, usersVO);

        return JpliuJSONResult.ok(usersVO);
    }


    @PostMapping("/uploadFaceBase64")
    public JpliuJSONResult uploadFaceBase64(@RequestBody UsersBO userBO) throws Exception {
        // 获取前端传过来的base64字符串, 然后转换为文件对象再上传
        String base64Data = userBO.getFaceData();
        String userFacePath = "Untitled//Users//jpliu//Downloads//img//" + userBO.getUserId() + "userface64.png";
        FileUtils.base64ToFile(userFacePath, base64Data);

        // 上传文件到fastdfs
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(faceFile);
        System.out.println(url);

        // 获取缩略图的url
        String thump = "_80x80.";
        String arr[] = url.split("\\.");
        String thumpImgUrl = arr[0] + thump + arr[1];

        // 更细用户头像
        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);

        Users result = userService.updateUserInfo(user);

        return JpliuJSONResult.ok(result);


    }

    @PostMapping("/setNickname")
    public JpliuJSONResult setNickname(@RequestBody UsersBO userBO) throws Exception {
        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setNickname(userBO.getNickname());

        Users result = userService.updateUserInfo(user);

        return JpliuJSONResult.ok(result);
    }

    /**
     * 搜索好友接口, 根据账号做匹配查询 而不是模糊查询
     * @return
     * @throws Exception
     */
    @PostMapping("/search")
    public JpliuJSONResult searchUser(String myUserId, String friendUsername) throws Exception {

        // 0. 判断myUserId 和 friendUsername 不能为空
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)) {
            return JpliuJSONResult.errorMsg("");
        }
        // 1. 搜索的用户 如果不存在 那么返回无此用户
        // 2. 搜索的用户 是你自己， 返回不能添加此用户
        // 3. 搜索的用户 已经是你的好友了， 那么返回该用户已经是你i的好友
        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);

        if (status == SearchFriendsStatusEnum.SUCCESS.status) {
            Users user = userService.queryUserInfoByUsername(friendUsername);
            UsersVO usersVO = new UsersVO();
            BeanUtils.copyProperties(user, usersVO);
            return JpliuJSONResult.ok(usersVO);

        } else {
            String errorMessage = SearchFriendsStatusEnum.getMsgByKey(status);
            return JpliuJSONResult.errorMsg(errorMessage);
        }
    }

    /**
     * 发送添加好友的请求
     * @return
     * @throws Exception
     */
    @PostMapping("/addFriendRequest")
    public JpliuJSONResult addFriendRequest(String myUserId, String friendUsername) throws Exception {

        // 0. 判断myUserId 和 friendUsername 不能为空
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)) {
            return JpliuJSONResult.errorMsg("");
        }
        // 1. 搜索的用户 如果不存在 那么返回无此用户
        // 2. 搜索的用户 是你自己， 返回不能添加此用户
        // 3. 搜索的用户 已经是你的好友了， 那么返回该用户已经是你i的好友
        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);

        if (status == SearchFriendsStatusEnum.SUCCESS.status) {
            userService.sendFriendRequest(myUserId, friendUsername);
        } else {
            String errorMessage = SearchFriendsStatusEnum.getMsgByKey(status);
            return JpliuJSONResult.errorMsg(errorMessage);
        }

        return JpliuJSONResult.ok();
    }

    @PostMapping("/queryFriendRequests")
    public JpliuJSONResult queryFriendRequests(String userId) throws Exception {

        //判断userId是否为空
        if (StringUtils.isBlank(userId)) {
            return JpliuJSONResult.errorMsg("userId 不能为空");
        }
        //查询用户接受到的朋友申请
        return JpliuJSONResult.ok(userService.queryFriendRequestList(userId));
    }


    /**
     * 接收方 通过或者忽略朋友请求
     * @param acceptUserId
     * @param sendUserId
     * @param operType
     * @return
     * @throws Exception
     */
    @PostMapping("/operFriendRequest")
    public JpliuJSONResult operFriendRequest(String acceptUserId, String sendUserId, Integer operType) throws Exception {

        //判断是否为空
        if (StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(sendUserId) || operType == null) {
            return JpliuJSONResult.errorMsg("userId 不能为空");
        }

        //如果operType 没有对应的枚举值， 则直接抛出空错误信息
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))) {
            return JpliuJSONResult.errorMsg("userId 不能为空");
        }

        if (operType == OperatorFriendRequestTypeEnum.IGNORE.type) {
            // 2. 判断如果忽略好友请求，则直接删除好友请求的数据记录
            userService.deleteFriendRequest(sendUserId, acceptUserId);
        } else if (operType == OperatorFriendRequestTypeEnum.PASS.type) {
            // 3. 判断如果是通过好友请求， 则互相增加好友记录到数据库对应的表
            // 然后删除好友请求的数据库记录

            userService.passFriendRequest(sendUserId, acceptUserId);
        }
        //查询用户接受到的朋友申请
        //1.数据库查询好友列表
        List<MyFriendsVO> myFriends = userService.queryMyFriends(acceptUserId);

        return JpliuJSONResult.ok(myFriends);
    }

    /**
     * 查询我的好友列表
     * @param userId
     * @return
     * @throws Exception
     */
    @PostMapping("/myFriends")
    public JpliuJSONResult myFriends(String userId) throws Exception {

        if (StringUtils.isBlank(userId)) {
            return JpliuJSONResult.errorMsg("不能为空");
        }

        //1.数据库查询好友列表
        List<MyFriendsVO> myFriends = userService.queryMyFriends(userId);
        return JpliuJSONResult.ok(myFriends);
    }

    /**
     * 用户手机端获取为签收的消息列表
     * @param acceptUserId
     * @return
     * @throws Exception
     */
    @PostMapping("/getUnReadMsgList")
    public JpliuJSONResult getUnReadMsgList(String acceptUserId) throws Exception {

        if (StringUtils.isBlank(acceptUserId)) {
            return JpliuJSONResult.errorMsg("不能为空");
        }

        //1.数据库查询列表
        List<com.jpliu.project.pojo.ChatMsg> unReadMsgList = userService.getUnReadMsgList(acceptUserId);
        return JpliuJSONResult.ok(unReadMsgList);
    }
}
