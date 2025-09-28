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
		<ul class="card-body px-0 list-group chatList" data-simplebar style="height: 630px;">
			<c:forEach items="${roomList}" var="room">
				<li class="mb-4 px-5 py-2 chatListOne list-group-item-action "
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


<!-- 채팅방 생성하기 버튼 -->
<button type="button" class="btn btn-primary mb-3" data-toggle="modal" data-target="#createRoomStep1">
    채팅방 생성하기
</button>

<!-- Step 1: 참여자 선택 모달 -->
<div class="modal fade" id="createRoomStep1" tabindex="-1" role="dialog" aria-labelledby="step1Label" aria-hidden="true">
  <div class="modal-dialog modal-dialog-scrollable" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="step1Label">대화상대 선택</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="닫기">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <!-- 검색창 -->
        <div class="input-group mb-3">
          <input type="text" class="form-control" id="searchUserInput" placeholder="이름 검색">
          <div class="input-group-append">
            <button class="btn btn-outline-secondary" type="button">검색</button>
          </div>
        </div>

        <!-- 사원 목록 (스크롤 가능) -->
        <ul class="list-group" id="participantList" style="max-height: 300px; overflow-y: auto;">
          <c:forEach items="${employeeList}" var="employee">
            <li class="list-group-item d-flex align-items-center">
              <img src="/images/default_profile.jpg" class="rounded-circle mr-3" style="width:40px; height:40px;">
              <div class="flex-fill">
                <strong>${employee.name}</strong><br>
                <small>${employee.departmentName} ${employee.positionName}</small>
              </div>
              <input type="checkbox" value="${employee.username}" class="participant-checkbox">
            </li>
          </c:forEach>
        </ul>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary" id="nextStepBtn">다음</button>
      </div>
    </div>
  </div>
</div>

<!-- Step 2: 방 제목 입력 모달 -->
<div class="modal fade" id="createRoomStep2" tabindex="-1" role="dialog" aria-labelledby="step2Label" aria-hidden="true">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="step2Label">채팅방 제목 설정</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="닫기">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <label for="roomTitle">채팅방 제목</label>
        <input type="text" id="roomTitle" class="form-control" placeholder="채팅방 이름 입력">
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal">취소</button>
        <button type="button" class="btn btn-success" id="createRoomConfirmBtn">생성</button>
      </div>
    </div>
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