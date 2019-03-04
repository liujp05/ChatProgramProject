package com.jpliu.project.netty;

import com.jpliu.project.SpringUtil;
import com.jpliu.project.enums.MsgActionEnum;
import com.jpliu.project.service.UserService;
import com.jpliu.project.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 处理消息的handler
 * TextWebSocketFrame： 在netty中，是用于为websocket专门文本的对象，frame是消息的载体
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    //用于记录和管理所有客户端的channel
    private static final ChannelGroup USERS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                          TextWebSocketFrame textWebSocketFrame) throws Exception {
        //获取客户端传输过来的消息
        String content = textWebSocketFrame.text();
        Channel currentChannel = channelHandlerContext.channel();

        // 1. 获取客户端发来的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        Integer action = dataContent.getAction();
        // 2. 判断消息的类型，根据不同的类型来处理不同的业务

        if (action == MsgActionEnum.CONNECT.type) {
            //  2.1 当websocket第一次open的时候 初始化channel， 把用的channel和userid关联起来
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRelationship.put(senderId, currentChannel);

            //测试
            for (Channel c : USERS) {
                System.out.println(c.id().asLongText());
            }
            UserChannelRelationship.output();


        } else if (action == MsgActionEnum.CHAT.type) {
            //  2.2 聊天类型的消息，把聊天记录保存到数据库， 现实中是需要加密 和解密的， 标记消息的签收状态【未签收】
            ChatMsg chatMsg = dataContent.getChatMsg();
            String msgText = chatMsg.getMsg();
            String receiverId = chatMsg.getReceiverId();
            String senderId = chatMsg.getSenderId();

            //保存消息到数据库，并且标记为未签收
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);

            DataContent dataContent1Msg = new DataContent();
            dataContent1Msg.setChatMsg(chatMsg);
            // 发送消息
            // 从全局用户Channel关系中获取接收方的channel
            Channel receiverChannel = UserChannelRelationship.get(receiverId);
            if (receiverChannel == null) {
                // TODO channel为空 代表用户离线，推送消息 （JPush， 个推， 小米推送）
            } else {
                // 当receiverChannel不为空的时候， 从channelGroup去查找对应的channel是否存在
                Channel findChannel = USERS.find(receiverChannel.id());
                if (findChannel != null) {
                    // 用户在线
                    receiverChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent1Msg)));

                } else {
                    // 用户离线 TODO 推送
                }
            }

        } else if (action == MsgActionEnum.SIGNED.type) {
            //  2.3 签收消息类型， 针对具体的消息进行签收，修改数据库中对应消息的签收状态【已签收】
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            //扩展字段在send类型的消息中，代表需要去签收的消息id， 逗号间隔
            String msgIdStr = dataContent.getExtand();
            String[] msgIds = msgIdStr.split(",");
            List<String> msgIdList = new ArrayList<String>();
            for (String mid : msgIds) {
                if (StringUtils.isNotBlank(mid)) {
                    msgIdList.add(mid);
                }
            }
            System.out.println(msgIdList.toString());

            if (msgIdList != null && !msgIdList.isEmpty() && msgIdList.size() > 0) {
                userService.updateMsgSigned(msgIdList);
            }
        } else if (action == MsgActionEnum.KEEPALIVE.type) {
            //  2.4 心跳类型， 一种是netty， 另一种的是 前端提供的

        }

    }

    /**
     * 当客户端连接服务端之后（打开连接）
     * 获取客户端的channel， 并且放到ChannelGroup中去进行管理
     * @param ctx
     * @throws Exception
     */
    @Override public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        USERS.add(ctx.channel());
    }

    /**
     * 当触发handler remove
     * ChannelGroup会自动移除对应客户端的channel
     * @param ctx
     * @throws Exception
     */
    @Override public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        System.out.println("客户端被移除， Channel ID 为：" + channelId);
        // 当触发handlerremoved， channelGroup会自动移除客户端的channel
        USERS.remove(ctx.channel());
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //发生异常之后 关闭channel， 随后从ChannelGroup中移除
        ctx.channel().close();
        USERS.remove(ctx.channel());
    }
}
