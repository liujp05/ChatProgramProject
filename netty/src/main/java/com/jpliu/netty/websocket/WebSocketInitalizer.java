package com.jpliu.netty.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketInitalizer extends ChannelInitializer<SocketChannel> {

    @Override protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline channelPipeline = channel.pipeline();

        //websocket基于http协议，所以需要http的编解码器
        channelPipeline.addLast(new HttpServerCodec());
        //对写大数据流的支持
        channelPipeline.addLast(new ChunkedWriteHandler());
        //对httpMessage进行聚合， 聚合成FulHttpRequest或者FulHTTPResponse
        //几乎在netty中的编程， 都会使用到此handler
        channelPipeline.addLast(new HttpObjectAggregator(1024 * 64));

        //=======================以上是用于支持http协议===================================

        /**
         *  websocket 服务器处理的协议， 用于指定给客户端访问的路由： /ws
         *  本handler会帮你处理一些繁重的复杂的事情
         *  会帮你处握手动作：handshaking（close(关闭)， ping（请求）， pong（响应）） ping + pong = 心跳
         *  对于websocket来讲， 都是以frame进行传输的，不同的数据类型对应的frame也不同
         */
        channelPipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        //自定义的handler
        channelPipeline.addLast(new ChatHandler());


    }
}
