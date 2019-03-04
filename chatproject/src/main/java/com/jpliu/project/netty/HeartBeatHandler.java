package com.jpliu.project.netty;

import com.jpliu.project.SpringUtil;
import com.jpliu.project.enums.MsgActionEnum;
import com.jpliu.project.service.UserService;
import com.jpliu.project.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于检测 channel 的心跳 handler
 * 继承了ChannelInboundHandlerAdapter， 从而不需要实现ChannelRead0 方法
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        /**
         * 判断evt是否是IdleStateEvent（用于触发用户事件，包含读空闲/写空闲/读写空闲）
         */
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt; // 强制类型转换


            if (event.state() == IdleState.READER_IDLE) {
                //不需要处理
                System.out.println("进入读空闲");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                //不需要处理
                System.out.println("进入写空闲");
            } else if (event.state() == IdleState.ALL_IDLE) {
                Channel channel = ctx.channel();
                //关闭无用的channel 以防止资源浪费
                channel.close();
                // 测试时 可以将 ChatHandler里面的USERS改成public 这样可以在这里打印了
            }
        }
    }
}
