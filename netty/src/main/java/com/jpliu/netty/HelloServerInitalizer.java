package com.jpliu.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;


/**
 * @Description: 初始化器： channel注册后，会执行里面相应的初始化方法
 */
public class HelloServerInitalizer extends ChannelInitializer<SocketChannel> {


    @Override protected void initChannel(SocketChannel channel) throws Exception {
        //通过channel获得对应的pipeline
        ChannelPipeline channelPipeline = channel.pipeline();

        //通过pipeline添加handler
        //HttpServerCodec()是由netty自己提供的助手类，可以理解为拦截器
        //当请求到服务端，我们需要做编解码，响应到客户端做编码，在服务端解码
        channelPipeline.addLast("HttpServerCodec", new HttpServerCodec());

        //添加自定义的助手类返回hello netty的字符串
        channelPipeline.addLast("customHandler", new CustomHandler());
    }
}
