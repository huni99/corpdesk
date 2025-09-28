package com.goodee.corpdesk.chat.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodee.corpdesk.chat.entity.ChatMessage;
import com.goodee.corpdesk.chat.entity.ChatParticipant;
import com.goodee.corpdesk.chat.repository.ChatMessageRepository;
import com.goodee.corpdesk.chat.repository.ChatParticipantRepository;

@Service
public class ChatParticipantService {

	@Autowired
	private ChatParticipantRepository chatParticipantRepository;
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	
	public List<ChatParticipant> participantList(Long chatRoomId) {
		List<ChatParticipant> list = chatParticipantRepository.findAllByChatRoomIdAndUseYnTrue(chatRoomId);
		return list;
	}

	public boolean isRoomParticipant(ChatParticipant chatParticipant) {
		boolean result = chatParticipantRepository.
				existsByChatRoomIdAndEmployeeUsernameAndUseYnTrue(chatParticipant.getChatRoomId(),chatParticipant.getEmployeeUsername())
				;
		return result;
	}
	
	  @Transactional
	// 채팅방 닫을 때 제일 최신 메세지ID를 저장 
	  public void updateLastMessage(String username, Long roomId) {
		ChatMessage chatMessage =  chatMessageRepository.findTopByChatRoomIdOrderByMessageIdDesc(roomId);
		if(chatMessage != null) {
			chatParticipantRepository.updateLastMessage(username,roomId,chatMessage.getMessageId());
		}
		
			
		}
}
