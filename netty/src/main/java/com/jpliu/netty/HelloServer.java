package com.jpliu.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Description Netty : 客户端发送一个请求， 服务器返回一个hello netty
 *
 */
public class HelloServer {

//    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    public static void main(String[] args) throws Exception {

        /*
        定义一堆线程组
         */
        //主线程组, 用于接收客户端的连接，但是不做任何处理，和老板一样，不做事情
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //从线程组，老板线程组会把任务丢给工人，让工人线程组去做任务
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //netty 服务器的创建， serverBootstrap 是一个启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //任务分配 netty会自己处理， 设置处从线程组
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)  //设置nio的双向通道
                    .childHandler(new HelloServerInitalizer());                    //子处理器, 用于处理workergroup

            //启动server 并且设置8088 为端口号，同时启动方式为同步
            ChannelFuture channelFuture = serverBootstrap.bind(8088).sync();

            //用于监听关闭的channel设置为同步方式
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            //关闭主从线程组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
