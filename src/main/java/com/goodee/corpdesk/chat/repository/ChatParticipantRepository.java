package com.goodee.corpdesk.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.goodee.corpdesk.chat.entity.ChatParticipant;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long>{

	List<ChatParticipant> findAllByChatRoomId(Long chatRoomId);
	boolean existsByChatRoomIdAndEmployeeUsernameAndUseYnTrue(Long chatRoomId , String username);
	@Modifying
	@Query("UPDATE ChatParticipant c SET lastCheckMessageId = :lastMessage "+
			   "WHERE c.employeeUsername = :username AND c.chatRoomId =:roomId")
		void updateLastMessage(@Param("username") String username, @Param("roomId") Long roomId,@Param("lastMessage") Long lastMessage);
		ChatParticipant findByChatRoomIdAndEmployeeUsername(Long roomId, String username);
		
	@Query("SELECT COUNT(*) FROM ChatMessage "+
			"WHERE messageId > :lastMessageId AND "+
			"chatRoomId =:chatRoomId")
	Long count(@Param("lastMessageId") Long getLastCheckMessageId ,@Param("chatRoomId")  Long chatRoomId);
	
	@Query("SELECT COUNT(*) FROM ChatMessage "+
			"WHERE chatRoomId =:chatRoomId")
	Long countAll(@Param("chatRoomId") Long roomId);
	//방에 참여중인 사람만 
	List<ChatParticipant> findAllByChatRoomIdAndUseYnTrue(Long roomId); 
	
	//기존 방을 나갔다가 다시 열경우
	@Transactional
	@Modifying
	@Query("UPDATE ChatParticipant c SET useYn = true "+
			   "WHERE c.employeeUsername = :username AND c.chatRoomId =:roomId")
		void updateRoomUseYnTrue(@Param("username") String username, @Param("roomId") Long roomId);
	//채팅방을 나갈 경우
		@Transactional
		@Modifying
		@Query("UPDATE ChatParticipant c SET useYn = false "+
				   "WHERE c.employeeUsername = :username AND c.chatRoomId =:roomId")
			void updateRoomUseYnFalse(@Param("username") String username, @Param("roomId") Long roomId);
		
		
		boolean existsByChatRoomIdAndEmployeeUsername(Long chatRoomId, String employeeUsername);
	
	
}
