package smalltalk.backend.config.websocket

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.messaging.WebSocketStompClient


@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig (
    private val stompHandler: StompHandler
): WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // /rooms/something 주소를 구독하는 클라이언트에게 메시지를 보낼 수 있게 브로커 활성화
        config.enableSimpleBroker("/rooms")
        // /rooms/chat/something 과 같은 주소로 메세지를 보낼 수 있음, @MessageMapping 에선 /somthing 만 mapping 하면 됨
        config.setApplicationDestinationPrefixes("/rooms/chat")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // ws://domain/ws-connect 주소로 웹 소켓 연결이 가능
        registry.addEndpoint("/ws-connect").setAllowedOrigins("*")
        registry.addEndpoint("/ws-connect").setAllowedOrigins("*").withSockJS()
    }

    // 메시지가 클라이언트로부터 들어올 때 호출
    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(stompHandler)
    }

    @Bean
    fun webSocketClient() : WebSocketStompClient =
        WebSocketStompClient(StandardWebSocketClient())
}

