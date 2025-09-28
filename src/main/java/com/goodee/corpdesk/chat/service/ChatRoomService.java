package com.goodee.corpdesk.chat.service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.goodee.corpdesk.chat.dto.RoomData;
import com.goodee.corpdesk.chat.entity.ChatMessage;
import com.goodee.corpdesk.chat.entity.ChatParticipant;
import com.goodee.corpdesk.chat.entity.ChatRoom;
import com.goodee.corpdesk.chat.repository.ChatMessageRepository;
import com.goodee.corpdesk.chat.repository.ChatParticipantRepository;
import com.goodee.corpdesk.chat.repository.ChatRoomRepository;
import com.goodee.corpdesk.department.entity.Department;
import com.goodee.corpdesk.employee.Employee;
import com.goodee.corpdesk.employee.EmployeeService;
import com.goodee.corpdesk.position.entity.Position;

@Service
public class ChatRoomService {

	@Autowired
	private ChatRoomRepository chatRoomRepository;
	
	@Autowired
	private ChatParticipantRepository chatParticipantRepository;
	@Autowired
	EmployeeService employeeService;
	@Autowired
	ChatMessageRepository chatMessageRepository;

	//해당 유저의 모든 채팅방 목록을 불러옴
	public List<RoomData> getChatRoomList(String username) {
		
		//해당 유저의 모든 채팅방 목록을 가져옴
		List<ChatRoom> listE= chatRoomRepository.findAllByUsername(username);
		List<RoomData> list = new ArrayList<>();
		listE.forEach(le->{
			RoomData data = new RoomData();
			data.setChatRoomId(le.getChatRoomId());
			
			data.setChatRoomType(le.getChatRoomType());
			data.setChatRoomTitle(le.getChatRoomTitle());
			data.setUnreadCount(le.getUnreadCount());		
			
			
			// 해당 방의 마지막 메세지 내용, 시간을 가져옴
			
			ChatMessage chatMessage = chatMessageRepository.findTopByChatRoomIdOrderByMessageIdDesc(data.getChatRoomId());
			if(chatMessage==null) {
				data.setLastMessageTime(null);
				data.setChatRoomLastMessage(" ");			
				
			}else {
				data.setLastMessageTime(chatMessage.getSent_at());
				data.setChatRoomLastMessage(chatMessage.getMessageContent());			
				
			}
			
			list.add(data);
		});
		
		//각 채팅방 별로 어떤 메세지 까지 읽었는 지를 확인 후 계산해서 list에 다시 넣고 반환
		//이 때 처음 생성되서 lastCheck가 null인경우 해당 방의 모든 메세지의 개수를 가져옴
		list.forEach(l->{
			Long roomId = l.getChatRoomId();
			ChatParticipant cp= chatParticipantRepository.findByChatRoomIdAndEmployeeUsername(roomId, username);
			
			//1대1채팅일경우 채팅방 제목을 상대방 이름으로 설정
			if(l.getChatRoomType().equals("direct")) {
				List<ChatParticipant> cps = chatParticipantRepository.findAllByChatRoomId(roomId);
				if(cps.size()==1) {
					Map<String, Object> map;
					map =employeeService.detail(username);
					Employee emp = (Employee)map.get("employee");
					Department department = (Department)map.get("department");
					Position position = (Position)map.get("position");
					String roomtitle = department.getDepartmentName()+" "+position.getPositionName()+" "+emp.getName();
					l.setChatRoomTitle(roomtitle);
				}else {
					for(ChatParticipant c : cps) {
						
						if(!c.getEmployeeUsername().equals(username)) {
							//TODO 추후 해당 username으로 사원정보를 불러와서 넣어주면됨		
							Map<String, Object> map;
							map =employeeService.detail(c.getEmployeeUsername());
							Employee emp = (Employee)map.get("employee");
							Department department = (Department)map.get("department");
							Position position = (Position)map.get("position");
							String roomtitle = department.getDepartmentName()+" "+position.getPositionName()+" "+emp.getName();
							l.setChatRoomTitle(roomtitle);
							break;
						}
					}
				}
			}
			
			if(cp.getLastCheckMessageId()==null) {
				l.setUnreadCount((chatParticipantRepository.countAll(roomId)));
			}
			else {
				l.setUnreadCount(chatParticipantRepository.count(cp.getLastCheckMessageId(),roomId));
			}
		});
		
		
		return list;
	}

	
	
	//채팅방 생성
	public Long createRoom(RoomData roomdata , Principal principal ) {
		List<String> usernames = roomdata.getUsernames();
		ChatRoom chatroom = new ChatRoom();
		chatroom.setUseYn(true);
		ChatParticipant chatParticipant = new ChatParticipant();
		
		//1대1 채팅방 생성
		if(usernames.size()==1) {
			//중복먼저 확인
			//채팅방 상대랑 , 사용자를 넣고 확인해옴
			Optional<ChatRoom> directRoom;
			//대상이 자기 자신인 경우
			if(usernames.getFirst().equals(principal.getName())) {
				directRoom = chatRoomRepository.findDuplicatedRoomOwn(principal.getName());
				
				if(!directRoom.isEmpty()) {
					return directRoom.get().getChatRoomId();
				}
				else {
					//채팅방 타입 설정
					chatroom.setChatRoomType(roomdata.getChatRoomType());
					chatroom = chatRoomRepository.save(chatroom);
					
					//사용자 저장
					chatParticipant = new ChatParticipant();
					chatParticipant.setChatRoomId(chatroom.getChatRoomId());
					chatParticipant.setEmployeeUsername(principal.getName());
					chatParticipant.setUseYn(false);
					chatParticipantRepository.save(chatParticipant);
				}
				
			// 상대방이 있는 경우
			}else {
				directRoom =chatRoomRepository.findDuplicatedRoom(principal.getName(),usernames.getFirst());
				if(!directRoom.isEmpty()) {
					
					return directRoom.get().getChatRoomId();
				}
				else {
					//채팅방 타입 설정
					chatroom.setChatRoomType(roomdata.getChatRoomType());
					chatroom = chatRoomRepository.save(chatroom);
					
					//상대방 저장
					chatParticipant.setEmployeeUsername(usernames.getFirst());
					chatParticipant.setChatRoomId(chatroom.getChatRoomId());
					chatParticipant.setUseYn(false);
					chatParticipantRepository.save(chatParticipant);
					
					//사용자 저장
					chatParticipant = new ChatParticipant();
					chatParticipant.setChatRoomId(chatroom.getChatRoomId());
					chatParticipant.setEmployeeUsername(principal.getName());
					chatParticipant.setUseYn(false);
					chatParticipantRepository.save(chatParticipant);
				}
				
			}
			
			
		}
		//그룹채팅 생성
		else {
			chatroom.setChatRoomTitle(roomdata.getChatRoomTitle());
			chatroom.setChatRoomType(roomdata.getChatRoomType());
			chatroom = chatRoomRepository.save(chatroom);
			
			//사용자 저장
			chatParticipant.setChatRoomId(chatroom.getChatRoomId());
			chatParticipant.setEmployeeUsername(principal.getName());
			chatParticipant.setUseYn(false);
			chatParticipantRepository.save(chatParticipant);
			
			for(String user : roomdata.getUsernames()) {
				chatParticipant = new ChatParticipant();
				chatParticipant.setChatRoomId(chatroom.getChatRoomId());
				chatParticipant.setEmployeeUsername(user);
				chatParticipant.setUseYn(false);
				chatParticipantRepository.save(chatParticipant);
			}
			
		}
		
		return chatroom.getChatRoomId();
		
	}



	public boolean outRoom(Long roomId, Principal principal) {
		boolean result =false;
		if(chatParticipantRepository.existsByChatRoomIdAndEmployeeUsernameAndUseYnTrue(roomId, principal.getName())) {
			chatParticipantRepository.updateRoomUseYnFalse(principal.getName(), roomId);
			result =true;
		}
		return result;
	}



	public String getChatRoomType(Long chatRoomId) {
		ChatRoom chatRoom =chatRoomRepository.findByChatRoomId(chatRoomId).get();
		return chatRoom.getChatRoomType();
	}



	public String getRoomTitle(Long chatRoomId) {
		return chatRoomRepository.findByChatRoomId(chatRoomId).get().getChatRoomTitle();
		
	}
	
	
	public Long getRoomUnreadCount(Long chatRoomId) {
		return chatRoomRepository.findByChatRoomId(chatRoomId).get().getUnreadCount();
		
	}

	

}
