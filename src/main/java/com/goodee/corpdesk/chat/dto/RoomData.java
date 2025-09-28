package com.goodee.corpdesk.chat.dto;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.goodee.corpdesk.chat.entity.ChatRoom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Component
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomData {
	private String chatRoomTitle;
	private List<String> usernames;
	private String chatRoomType;
	private Long chatRoomId;
	private Long unreadCount;
	private LocalDateTime lastMessageTime;
	private String chatRoomLastMessage;
	private String notificationType;
}
