package com.goodee.corpdesk.chat.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.goodee.corpdesk.chat.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.goodee.corpdesk.chat.dto.ChatSessionTracker;
import com.goodee.corpdesk.chat.dto.FocusMessage;
import com.goodee.corpdesk.chat.entity.ChatMessage;
import com.goodee.corpdesk.chat.entity.ChatParticipant;
import com.goodee.corpdesk.chat.service.ChatMessageService;

@Controller
@RequestMapping("/chat/message/**")
public class ChatMessageController {

    private final ChatRoomService chatRoomService;

	
	@Autowired
	// Spring에서 서버가 클라이언트(STOMP 구독자)에게 메시지를 푸시하기 위해 제공하는 템플릿 객체
	private SimpMessagingTemplate messagingTemplate;
	
	@Autowired
	private ChatMessageService chatMessageService;
	@Autowired
	private ChatSessionTracker chatSessionTracker;

    ChatMessageController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

	// websocket 요청에 대한 매핑 위의 requestMapping과 관련없고 websocket config에서 지정해준 prefix 사용
	@MessageMapping("/chat/message")
	public void chatsendMessage(ChatMessage msg) {
		chatMessageService.messageSave(msg);
		msg.setNotificationType("message");
		String chatRoomType =chatRoomService.getChatRoomType(msg.getChatRoomId());
		messagingTemplate.convertAndSend("/sub/chat/room/" + msg.getChatRoomId(), msg);
		
		//채팅방의 모든 개인 구독 알림 전송
		List<ChatParticipant> list;
		//개인 채팅일경우
		if(chatRoomType.equals("direct")) {
			
			list = chatMessageService.participantOnetoOneByRoom(msg.getChatRoomId());
			
			
		}
		//그룹채팅일경우
		else {
			
			list = chatMessageService.participantListByRoom(msg.getChatRoomId());
		}
		list.forEach(l->{
			String username =l.getEmployeeUsername();
			Long chatRoomId = l.getChatRoomId();
			
			//현재 방을 포커스하고있는 사람은 알림은가는데 카운트는 제외 채팅목록 메세지만 바꾸게해야함
				if(!chatSessionTracker.isUserFocused(chatRoomId, username)) {
					msg.setFocused(false);
					messagingTemplate.convertAndSendToUser(username, "/queue/notifications",msg );
				}else {
					msg.setFocused(true);
					messagingTemplate.convertAndSendToUser(username, "/queue/notifications",msg );
				}
			
		});
		

	}
	@MessageMapping("/chat/focus")
	 public void updateFocus(FocusMessage message, Principal principal, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        chatSessionTracker.setFocus(sessionId, message.isFocused());
        
        //포커스되면 읽음을 목록 jsp에도 보내줘야함
       if(message.isFocused()) {
    	  message.setNotificationType("read");
       	messagingTemplate.convertAndSendToUser(principal.getName(),"/queue/notifications",message);
       }
	}
	
	@GetMapping("list/{chatRoomId}/{messageNo}/{size}")
	@ResponseBody
	public Map<String , Object> chatMessageList(@PathVariable(value = "chatRoomId")Long chatRoomId , 
			@PathVariable(value = "messageNo")Long lastMessageNo, @PathVariable(value = "size")int size){
		
		List<ChatMessage> list = chatMessageService.chatMessageList(chatRoomId,lastMessageNo,size);
		Map<String, Object> map = new HashMap<>();
		map.put("size", list.size());
		map.put("messages",list);
		
		return map;
		
	}
	
	
	
	
}
