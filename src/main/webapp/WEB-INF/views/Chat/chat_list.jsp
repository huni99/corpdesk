<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>채팅 목록</title>
	<c:import url="/WEB-INF/views/include/head.jsp"/>
</head>

<c:import url="/WEB-INF/views/include/body_wrapper_start.jsp"/> 

	<c:import url="/WEB-INF/views/include/sidebar.jsp"/>

	<c:import url="/WEB-INF/views/include/page_wrapper_start.jsp"/>

		<c:import url="/WEB-INF/views/include/header.jsp"/>
	
		<c:import url="/WEB-INF/views/include/content_wrapper_start.jsp"/>
			<!-- 내용 시작 -->
			<sec:authentication property="principal.username" var="user" />
			
		
			<p>연락처 목록 </p>
			<table class= "contact">
					<tr>
						<th>부서</th>&nbsp;
						<th>직위</th>&nbsp;
						<th>이름</th>
					</tr>
					<c:forEach items="${employeeList}" var="employee">
						<tr class="employeeListOne" data-employee="${employee.username }" style="cursor:pointer;">
								<td>${employee.departmentName } &nbsp;</td>
								
								<td>${employee.positionName} &nbsp;</td>
								
								<td>${employee.name }</td>
						</tr>
						
					</c:forEach>
				</table>
				<br>
			<br><br><br>
			
						
				<br>
				<button type="button" id="createRoomBtn">채팅방 생성하기</button>

<!-- 채팅방 목록 -->
<div class="col-lg-7 col-xxl-5">
	<div class="card card-default chat-left-sidebar">
		<ul class="card-body px-0 chatList" data-simplebar style="height: 630px;">
			<c:forEach items="${roomList}" var="room">
				<li class="mb-4 px-5 py-2 chatListOne"
					data-roomId="${room.chatRoomId }"
					data-unreadCount="${room.unreadCount }">
					<div class="media media-message">
						<div class="position-relative mr-3">
							<img class="rounded-circle" src="/images/default_profile.jpg"
								alt="User Image" style="width: 70px;">
						</div>

						<div class="media-body">
							<div class="message-contents">
								<span
									class="d-flex justify-content-between align-items-center mb-1">
									<span class="username text-dark">${room.chatRoomTitle}</span> <span
									class=""> <c:choose>
											<c:when test="${room.unreadCount ne 0 }">
												<span class="badge badge-secondary unreadCount">
													${room.unreadCount } </span>
											</c:when>
											<c:otherwise>
												<span class="unreadCount"> </span>
											</c:otherwise>
										</c:choose> <span class="state text-smoke last-msg-time"
										data-lastMessageTime="${room.lastMessageTime}"><em></em></span>
								</span>
								</span>

								<p class="last-msg text-smoke">${room.chatRoomLastMessage}</p>
							</div>
						</div>
							<div class="dropdown kebab-menu" style="margin-left:10px;">
								<a class="dropdown-toggle icon-burger-mini kebab-menu" href="#"
									role="button" id="dropdownMenuLink" data-toggle="dropdown"
									aria-haspopup="true" aria-expanded="false"> </a>

								<div class="dropdown-menu  dropdown-menu-right"
									aria-labelledby="dropdownMenuLink">
									<a class="dropdown-item room-out" href="javascript:void(0)">채팅방 나가기</a>
								</div>
							</div>
					</div>
				</li>
			</c:forEach>
		</ul>
	</div>
</div>

<!-- 사원 목록 모달 창 -->
				<div id="createRoomModal" class="modal" style="display: none;">
					<div class="modal-content">
						<span class="close">&times;</span>
						<h2>채팅방 생성</h2>
				
						<label>방 제목</label> <input type="text" id="roomTitle" /> <label>참여자
							선택</label>
						<div id="participantList">
							<!-- 사원 목록을 체크박스로 뿌려줌 -->
							<label><input type="checkbox" value="wjdrlfgns2"> wjdrlfgns2</label><br />
							<label><input type="checkbox" value="wjdrlfgns1"> wjdrlfgns1</label><br />
						</div>
				
						<button id="createRoomConfirmBtn">생성</button>
					</div>
				</div>
				
				<script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
  			    <script src="https://cdn.jsdelivr.net/npm/stompjs/lib/stomp.min.js"></script>
				<script src="/js/chat/chatList.js" ></script>
				
				
			
			
			
			<!-- 내용 끝 -->
		<c:import url="/WEB-INF/views/include/content_wrapper_end.jsp"/>
	
	<c:import url="/WEB-INF/views/include/page_wrapper_end.jsp"/>
	
<c:import url="/WEB-INF/views/include/body_wrapper_end.jsp"/>
</html>