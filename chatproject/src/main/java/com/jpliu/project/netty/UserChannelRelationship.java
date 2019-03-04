package com.jpliu.project.netty;

import io.netty.channel.Channel;

import java.util.HashMap;

/**
 * 这是用户ID 和 channel关联关系处理
 */
public class UserChannelRelationship {

    private static HashMap<String, Channel> manager = new HashMap<String, Channel>();

    public static void put(String senderId, Channel channel) {
        manager.put(senderId, channel);
    }

    public static Channel get(String senderId) {
        return manager.get(senderId);
    }

    public static void output() {
        for (HashMap.Entry<String, Channel> entry : manager.entrySet()) {
            System.out.println("UserId: " + entry.getKey() + ", ChannelId: " + entry.getValue().id().asLongText());
        }
    }
}
