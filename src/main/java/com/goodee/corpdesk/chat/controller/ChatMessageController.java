package com.goodee.corpdesk.chat.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.goodee.corpdesk.chat.service.ChatRoomService;
import com.goodee.corpdesk.department.entity.Department;
import com.goodee.corpdesk.employee.Employee;
import com.goodee.corpdesk.employee.EmployeeService;
import com.goodee.corpdesk.position.entity.Position;

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
import com.goodee.corpdesk.chat.dto.RoomData;
import com.goodee.corpdesk.chat.entity.ChatMessage;
import com.goodee.corpdesk.chat.entity.ChatParticipant;
import com.goodee.corpdesk.chat.entity.ChatRoom;
import com.goodee.corpdesk.chat.service.ChatMessageService;
import com.goodee.corpdesk.chat.service.ChatParticipantService;

@Controller
@RequestMapping("/chat/message/**")
public class ChatMessageController {

	@Autowired
	// Spring에서 서버가 클라이언트(STOMP 구독자)에게 메시지를 푸시하기 위해 제공하는 템플릿 객체
	private SimpMessagingTemplate messagingTemplate;
	
	@Autowired
	private ChatMessageService chatMessageService;
	@Autowired
	private ChatSessionTracker chatSessionTracker;

	@Autowired
	private ChatRoomService chatRoomService;
	
	@Autowired
	private EmployeeService employeeService;
  
	// websocket 요청에 대한 매핑 위의 requestMapping과 관련없고 websocket config에서 지정해준 prefix 사용
	@MessageMapping("/chat/message")
	public void chatsendMessage(ChatMessage msg , Principal principal) {
		  //  메시지 저장
		ChatMessage saveMsg = chatMessageService.messageSave(msg);
	    saveMsg.setNotificationType("message");

	    // 2) 방 타입 조회
	    String chatRoomType = chatRoomService.getChatRoomType(msg.getChatRoomId());

	    // 3) 방 참가자 처리
	    List<ChatParticipant> participants;
	    RoomData chatRoom = new RoomData();
	    chatRoom.setChatRoomId(msg.getChatRoomId());
        chatRoom.setChatRoomLastMessage(saveMsg.getMessageContent());
        chatRoom.setLastMessageTime(saveMsg.getSent_at());
        chatRoom.setNotificationType("room");
        chatRoom.setUnreadCount(0L);
        
	    if (chatRoomType.equals("direct")) {
	        // 1대1이면 나간 사람도 다시 참여 처리
	        participants = chatMessageService.participantOnetoOneByRoom(msg.getChatRoomId());

	        // 상대방 이름으로 방제목 세팅
	        Map<String, Object> map = employeeService.detail(principal.getName());
	        Employee emp = (Employee) map.get("employee");
	        Department department = (Department) map.get("department");
	        Position position = (Position) map.get("position");
	        String roomtitle = department.getDepartmentName() + " " + position.getPositionName() + " " + emp.getName();
	        chatRoom.setChatRoomTitle(roomtitle);
	        
	        

	    } else {
	        // 그룹 채팅은 나간 사람한테 안보냄
	        participants = chatMessageService.participantListByRoom(saveMsg.getChatRoomId());
	        chatRoom.setChatRoomTitle(chatRoomService.getRoomTitle(saveMsg.getChatRoomId()));
	        chatRoom.setNotificationType("room");
	    }
	    

	    // 4) 방 전체 브로드캐스트
	    messagingTemplate.convertAndSend("/sub/chat/room/" + msg.getChatRoomId(), saveMsg);

	    // 5) 개인 알림 전송
	    for (ChatParticipant p : participants) {
	        String username = p.getEmployeeUsername();
	        Long chatRoomId = p.getChatRoomId();

	        if (!chatSessionTracker.isUserFocused(chatRoomId, username)) {
	        	saveMsg.setFocused(false);
	        } else {
	        	saveMsg.setFocused(true);
	        }
	        
	        if(chatRoomType.equals("direct") && username.equals(saveMsg.getEmployeeUsername())) {
	      
	        	List<ChatParticipant> list = chatMessageService.participantOnetoOneByRoom(chatRoomId);
	        	 RoomData chatRoomMe = new RoomData();
	        	 chatRoomMe.setChatRoomId(msg.getChatRoomId());
	        	 chatRoomMe.setChatRoomLastMessage(saveMsg.getMessageContent());
	        	 chatRoomMe.setLastMessageTime(saveMsg.getSent_at());
	        	 chatRoomMe.setNotificationType("room");
	        	 chatRoomMe.setUnreadCount(0L);
	        	 chatRoomMe.setChatRoomTitle(chatRoom.getChatRoomTitle());
	        	list.forEach(l->{
	        		if(!l.getEmployeeUsername().equals(username)) {
	        			 Map<String, Object> map = employeeService.detail(l.getEmployeeUsername());
	        		        Employee emp = (Employee) map.get("employee");
	        		        Department department = (Department) map.get("department");
	        		        Position position = (Position) map.get("position");
	        		        String roomtitle = department.getDepartmentName() + " " + position.getPositionName() + " " + emp.getName();
	        		        chatRoomMe.setChatRoomTitle(roomtitle);
	        		}
	        		
	        	});
	        	
	        	
	        	messagingTemplate.convertAndSendToUser(username, "/queue/notifications", chatRoomMe);
	 	        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", saveMsg);
	        	
	        		continue;
	        }
	    
	        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", chatRoom);
	        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", saveMsg);
	    }
	    
	    // 방을 생성한사람한테 상대방 이름 전송
	    
	    
	    
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
