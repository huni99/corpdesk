package com.goodee.corpdesk.chat.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.goodee.corpdesk.common.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table
public class ChatMessage extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long messageId;
	
	private String employeeUsername;
	private Long chatRoomId;
	private String messageContent;
	private String messageType;
	
	@CreationTimestamp
	private LocalDateTime sent_at;
	
	@Transient
	private String notificationType;
	@Transient
	private boolean focused;
	
}
