package com.jpliu.project;

import com.jpliu.project.netty.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class NettyBooter implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(NettyBooter.class);

    @Override public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
            try {
                WebSocketServer.getInstance().start();
            } catch (Exception e) {
                logger.error("Netty启动失败 " + e.getMessage());
            }
        }
    }
}
