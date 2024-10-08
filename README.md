## 요구사항
익명 N : N 채팅 API 서버입니다.
- Spring-boot over 3
- Kotlin & JDK 21
- API 개수 최소화
<br>

## Rest API
### POST /rooms
- 채팅방을 생성하는 요청
- 생성할 채팅방의 이름을 포함하여 요청
- 생성된 채팅방, 멤버 정보 응답
- 채팅방에 참여하고 있는 유저는 생성 불가능
- 인원 제한은 10명
<br>

### GET /rooms
- 개설되어 있는 모든 채팅방 목록을 조회하는 요청
- 모든 채팅방 정보 응답
- 개설된 채팅방이 없을 경우 비어있는 리스트 전송
<br>

### POST /rooms/{id}
- 해당 채팅방에 입장하는 요청
- 생성된 멤버 정보 응답
- 채팅방이 가득찼다면 (현재 요구 사항은 10명 제한) 입장 불가능
<br>

## WebSocket API
### /rooms/{id}
- 해당 채팅방에 입장하는 요청
- 해당 유저와 함께 채팅방 모든 멤버에게 (참여하고 있는 멤버 수, 닉네임) 등을 포함한 메시지 전송
- 입장에 실패했다면 원인을 포함한 에러 메시지 해당 유저에게만 전송
<br>

### /rooms/chat/{id}
- 해당 채팅방에 메시지를 전송하는 요청
