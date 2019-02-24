package com.jpliu.netty.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;

/**
 * @Description: 处理消息的handler
 * TextWebSocketFrame： 在netty中，是用于为websocket专门文本的对象，frame是消息的载体
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    //用于记录和管理所有客户端的channel
    private static final ChannelGroup CLIENTS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                          TextWebSocketFrame textWebSocketFrame) throws Exception {
        //获取客户端传输过来的消息
        String content = textWebSocketFrame.text();
        System.out.println("接收到的数据" + content);

//        for (Channel channel : CLIENTS) {
//            channel.writeAndFlush(new TextWebSocketFrame("[服务器在： ]"
//                    + LocalDateTime.now() + " 接收到消息, 消息为： " + content));
//        }
        //下面这个方法和上面的for循环一致
        CLIENTS.writeAndFlush(new TextWebSocketFrame("[服务器在： ]"
                + LocalDateTime.now() + " 接收到消息, 消息为： " + content));

    }

    /**
     * 当客户端连接服务端之后（打开连接）
     * 获取客户端的channel， 并且放到ChannelGroup中去进行管理
     * @param ctx
     * @throws Exception
     */
    @Override public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        CLIENTS.add(ctx.channel());
    }

    /**
     * 当触发handler remove
     * ChannelGroup会自动移除对应客户端的channel
     * @param ctx
     * @throws Exception
     */
    @Override public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
//        CLIENTS.remove(ctx.channel());
        System.out.println("客户端断开， channel对应的长id为 " + ctx.channel().id().asLongText());
        System.out.println("客户端断开， channel对应的短id为 " + ctx.channel().id().asShortText());
    }

}
