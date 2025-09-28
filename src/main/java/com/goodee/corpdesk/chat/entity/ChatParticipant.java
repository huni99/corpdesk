package com.goodee.corpdesk.chat.entity;

import java.time.LocalDateTime;

import com.goodee.corpdesk.common.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table
@Getter
@Setter
@ToString
public class ChatParticipant extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long participantId;
	private String employeeUsername;
	private Long chatRoomId;
	private Long lastCheckMessageId;

	
}
