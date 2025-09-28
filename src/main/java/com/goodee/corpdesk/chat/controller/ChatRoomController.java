package com.goodee.corpdesk.chat.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.goodee.corpdesk.chat.dto.RoomData;
import com.goodee.corpdesk.chat.entity.ChatRoom;
import com.goodee.corpdesk.chat.service.ChatRoomService;
import com.goodee.corpdesk.department.entity.Department;
import com.goodee.corpdesk.employee.Employee;
import com.goodee.corpdesk.employee.EmployeeService;
import com.goodee.corpdesk.position.entity.Position;

@Controller
@RequestMapping("/chat/room/**")
public class ChatRoomController {

	@Autowired
	ChatRoomService chatRoomService;
	@Autowired
	EmployeeService employeeService;
	@Autowired
	SimpMessagingTemplate simpMessagingTemplate;
	@GetMapping("list")
	public String chatRoomList(Principal principal ,Model model) {
			String username= principal.getName();
			List<RoomData> roomList= chatRoomService.getChatRoomList(username);
			List<Employee> employeeList = employeeService.getActiveEmployees();
			model.addAttribute("roomList", roomList);
			model.addAttribute("employeeList",employeeList);
		
		return "Chat/chat_list";
	}
	
	
	@GetMapping("detail/{roomId}")
	public String chatRoomDetail(@PathVariable(value = "roomId") Long roomId , Model model) {
		model.addAttribute("roomId",roomId);
		
		return "Chat/chat_page";
	}
	
	@ResponseBody
	@PostMapping("createRoom")
	public RoomData createRoom(@RequestBody RoomData roomdata , Principal principal) {
		//채팅방 생성 
		Long roomId =chatRoomService.createRoom(roomdata, principal);
		RoomData chatRoom = new RoomData();
		chatRoom.setChatRoomId(roomId);
//		chatRoom.setNotificationType("room");
//		//1대1 채팅일경우 채팅방 이름을 상대 이름으로 설정
//		if(roomdata.getChatRoomTitle()==null) {
//			//부서 직위 이름 꺼내오기
//			Map<String, Object> map;
//			map =employeeService.detail(principal.getName());
//			Employee emp = (Employee)map.get("employee");
//			Department department = (Department)map.get("department");
//			Position position = (Position)map.get("position");
//			String roomtitle = department.getDepartmentName()+" "+position.getPositionName()+" "+emp.getName();
//			
//			chatRoom.setChatRoomTitle(roomtitle);
//			chatRoom.setUnreadCount(0L);
//
//		}
//		
//		//상대방 구독 알림 보냄
//		for(int i = 0; i <roomdata.getUsernames().size();i++) {
//			simpMessagingTemplate.convertAndSendToUser(roomdata.getUsernames().get(i),"/queue/notifications" , chatRoom);
//		}
//		
//		//본인한테 구독 알림 보냄
//		if(roomdata.getUsernames().size()==1) {
//			Map<String, Object> map;
//			map =employeeService.detail(roomdata.getUsernames().getFirst());
//			Employee emp = (Employee)map.get("employee");
//			Department department = (Department)map.get("department");
//			Position position = (Position)map.get("position");
//			String roomtitle = department.getDepartmentName()+" "+position.getPositionName()+" "+emp.getName();
//			chatRoom.setChatRoomTitle(roomtitle);
//			simpMessagingTemplate.convertAndSendToUser(principal.getName(),"/queue/notifications" , chatRoom);
//		}

		return chatRoom;
	}
	@GetMapping("out/{roomId}")
	@ResponseBody
	public boolean chatRoomOut(@PathVariable(value = "roomId") Long roomId,Principal principal) {
		boolean result = chatRoomService.outRoom(roomId,principal);
		return result;
	}
}
