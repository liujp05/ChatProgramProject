window.app = {
	
	/**
	 * netty服务后端发布的URL地址
	 */
	nettyServerUrl : 'ws://192.168.31.162:8088/ws',
	/**
	 * 后端服务发布的URL地址
	 */
	serverUrl: 'http://192.168.31.162:8080',
	
	/**
	 * 图片服务器的URL地址
	 */
	imgServerUrl: 'http://192.168.31.18:88/jpliu/',
	
	
	/**
	 * 判断字符串是否为空 
	 * @param {Object} str
	 * true 代表不为空， false 代表为空
	 */
	isNotNull: function(str) {
		if (str != null && str != "" && str != undefined) {
			return true;
		}
		return false;
	},
	
	/**
	 * 封装消息提示框，默认mui的不支持居中和自定义icon，所以使用h5+ 
	 * @param {Object} msg
	 * @param {Object} type
	 */
	showToast: function(msg, type) {
		plus.nativeUI.toast(msg, {icon: "image/" + type + ".png", verticalAlign: "center"})	
	},
	
	/**
	 * 保存用户的全局对象 
	 * @param {Object} user
	 */
	setUserGlobalInfo: function(user) {
		var userInfoStr = JSON.stringify(user);
		plus.storage.setItem("userInfo", userInfoStr);
	},
	
	/**
	 * 获取用户的全局对象
	 */
	getUserGlobalInfo: function() {
		var userInfoStr = plus.storage.getItem("userInfo");
		return JSON.parse(userInfoStr);
	},
	/**
	 *  登出后移除 全局对象
	 */
	userLogout: function() {
		plus.storage.removeItem("userInfo");
	},
	
	/**
	 * 保存用户的联系人列表
	 * @param {Object} contactList
	 */
	setContactList: function(contactList) {
		var contactListStr = JSON.stringify(contactList);
		plus.storage.setItem("contactList", contactListStr);
	},
	
	/**
	 * 获取本地缓存中的联系人
	 */
	getContactList: function() {
		var contactListStr = plus.storage.getItem("contactList");
		if (!this.isNotNull(contactListStr)) {
			return [];
		}
		return JSON.parse(contactListStr);
	},
	
	/**
	 * 根据用户Id，从本地缓存中获取朋友的信息 
	 * @param {Object} friendId
	 */
	getFriendFromContactList: function(friendId) {
		var contactListStr = plus.storage.getItem("contactList");
		
		//判断是否为空
		if (this.isNotNull(contactListStr)) {
			// 不为空 把用户信息返回
			var contactList = JSON.parse(contactListStr);
			for (var i = 0; i < contactList.length; i++) {
				var friend = contactList[i];
				if (friend.friendUserId == friendId) {
					return friend;
					break;
				}
			}
		} else {
			// 为空， 直接返回空
			return null;
		}
	},
	/**
	 * 用于保存用户的聊天记录 
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} flag 判断本条消息是我发送的 还是朋友发送的 1： 我 2： 朋友
	 */
	saveUserChatHistory: function(myId, friendId, msg, flag) {
		var me = this;
		var chatKey = "chat-" + myId + "-" + friendId;
		// 从本地缓存获取聊天记录是否存在
		var chatHistoryListStr = plus.storage.getItem(chatKey);
		var chatHistoryList;
		if (me.isNotNull(chatHistoryListStr)) {
			chatHistoryList = JSON.parse(chatHistoryListStr);
		} else {
			// 如果为空， 赋值一个空的list
			chatHistoryList = [];
		}
		//构建聊天记录对象
		var singleMsg = new me.ChatHistory(myId, friendId, msg, flag);
		// 向list中追加msg对象
		chatHistoryList.push(singleMsg);
		
		plus.storage.setItem(chatKey, JSON.stringify(chatHistoryList));
	}, 
	
	/**
	 * 获取用户聊天记录 
	 * @param {Object} myId
	 * @param {Object} friendId
	 */
	getUserChatHistory: function(myId, friendId) {
		var me = this;
		var chatKey = "chat-" + myId + "-" + friendId;
		var chatHistoryListStr = plus.storage.getItem(chatKey);
		var chatHistoryList;
		if (me.isNotNull(chatHistoryListStr)) {
			chatHistoryList = JSON.parse(chatHistoryListStr);
		} else {
			// 如果为空， 赋值一个空的list
			chatHistoryList = [];
		}
		return chatHistoryList;
	},
	
	/**
	 * 删除我和朋友的聊天记录 
	 * @param {Object} myId
	 * @param {Object} friendId
	 */
	deleteUserChatHistory: function(myId, friendId) {
		var chatKey = "chat-" + myId + "-" + friendId;
		plus.storage.removeItem(chatKey);
	},
	
	/**
	 * 聊天记录的快照, 仅仅保存每次和朋友聊天的最后一条消息 
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} isRead
	 */
	saveUserChatSnapshot: function(myId, friendId, msg, isRead) {
		var me = this;
		var chatKey = "chat-snapshot" + myId;
		// 从本地缓存获取聊天快照的List
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		var chatSnapshotList;
		if (me.isNotNull(chatSnapshotListStr)) {
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			// 循环快照list， 并且判断每个元素是否包含（匹配）friendId， 如果匹配，则删除
			for (var i = 0; i < chatSnapshotList.length; i++ ) {
				if (chatSnapshotList[i].friendId == friendId) {
					// 删除已经存在的friendID 所对应的快照对象
					chatSnapshotList.splice(i, 1);
					break;
				}
			}
		} else {
			// 如果为空， 赋值一个空的list
			chatSnapshotList = [];
		}
		//构建聊天快照对象
		var snapshot = new me.ChatSnapshot(myId, friendId, msg, isRead);
		// 向list中追加msg对象
		chatSnapshotList.unshift(snapshot);
		
		plus.storage.setItem(chatKey, JSON.stringify(chatSnapshotList));
	},
	
	/**
	 * 获取用户快照记录 列表
	 * @param {Object} myId
	 */
	getUserChatSnapshot: function(myId) {
		var me = this;
		var chatKey = "chat-snapshot" + myId;
		// 从本地缓存获取聊天快照的List
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		var chatSnapshotList;
		if (me.isNotNull(chatSnapshotListStr)) {
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
		} else {
			// 如果为空， 赋值一个空的list
			chatSnapshotList = [];
		}
		return chatSnapshotList;
	},
	
	/**
	 * 删除本地的聊天快照记录
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} isRead
	 */
	deleteUserChatSnapshot: function(myId, friendId) {
		var me = this;
		var chatKey = "chat-snapshot" + myId;
		// 从本地缓存获取聊天快照的List
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		var chatSnapshotList;
		if (me.isNotNull(chatSnapshotListStr)) {
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			// 循环快照list， 并且判断每个元素是否包含（匹配）friendId， 如果匹配，则删除
			for (var i = 0; i < chatSnapshotList.length; i++ ) {
				if (chatSnapshotList[i].friendId == friendId) {
					// 删除已经存在的friendID 所对应的快照对象
					chatSnapshotList.splice(i, 1);
					break;
				}
			}
		} else {
			// 如果为空， 不做处理
			return;
		}

		plus.storage.setItem(chatKey, JSON.stringify(chatSnapshotList));
	},
	/**
	 * 标记未读消息为已读状态
	 * @param {Object} myId
	 * @param {Object} friendId
	 */
	readUserChatSnapshot: function(myId, friendId) {
		var me = this;
		var chatKey = "chat-snapshot" + myId;
		// 从本地缓存获取聊天快照的List
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		var chatSnapshotList;
		if (me.isNotNull(chatSnapshotListStr)) {
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			// 循环这个list，判断是否存在好友，比对friendId 如果有在list中的原有的位置删除该快照对象
			// 然后重新放入一个已读的对象
			for (var i = 0; i < chatSnapshotList.length; i++ ) {
				var item = chatSnapshotList[i];
				if (item.friendId == friendId) {
					item.isRead = true;	//标记为已读
					chatSnapshotList.splice(i, 1, item); //替换原有的快照
					break;
				}
			}
			// 替换原有的快照列表
			plus.storage.setItem(chatKey, JSON.stringify(chatSnapshotList));
		} else {
			// 如果为空
			return;
		}
	},
	/**
	 * 和后端的枚举 对应
	 */
	CONNECT: 1, 		//"第一次(或重连)初始化连接"),
	CHAT: 2, 		// "聊天消息"),	
	SIGNED: 3, 		//"消息签收"),
	KEEPALIVE: 4,	// "客户端保持心跳"),
	PULL_FRIEND: 5, //"拉取好友");
	
	/**
	 * 和后端的ChatMsg聊天模型保持一致 
	 * @param {Object} senderId
	 * @param {Object} receiverId
	 * @param {Object} msg
	 * @param {Object} msgId
	 */
	ChatMsg: function(senderId, receiverId, msg, msgId) {
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.msg = msg;
		this.msgId = msgId;
	},
	
	/**
	 * 构建消息模型对象 
	 * @param {Object} action
	 * @param {Object} chatMsg
	 * @param {Object} extand
	 */
	DataContent: function(action, chatMsg, extand) {
		this.action = action;
		this.chatMsg = chatMsg;
		this.extand = extand;
	},
	/**
	 * 单个聊天记录的对象 
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} flag
	 */
	ChatHistory: function(myId, friendId, msg, flag) {
		this.myId = myId;
		this.friendId = friendId;
		this.msg = msg;
		this.flag = flag;
	},
	/**
	 * 快照对象
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} isRead 用于判断消息是已读还是未读
	 */
	ChatSnapshot: function(myId, friendId, msg, isRead) {
		this.myId = myId;
		this.friendId = friendId;
		this.msg = msg;
		this.isRead = isRead;
	}
	
}
