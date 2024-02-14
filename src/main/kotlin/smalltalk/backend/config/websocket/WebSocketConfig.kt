package smalltalk.backend.config.websocket

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.MappingJackson2MessageConverter
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
    private val inboundChannelInterceptor: InboundChannelInterceptor,
    private val outboundChannelInterceptor: OutboundChannelInterceptor
): WebSocketMessageBrokerConfigurer {

    companion object {
        const val SEND_DESTINATION_PREFIX = "/rooms/chat/"
        const val SUBSCRIBE_DESTINATION_PREFIX = "/rooms/"
        const val STOMP_ENDPOINT = "/ws-connect"
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry
            .setApplicationDestinationPrefixes(SEND_DESTINATION_PREFIX)
            .enableSimpleBroker(SUBSCRIBE_DESTINATION_PREFIX)
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.apply {
            addEndpoint(STOMP_ENDPOINT)
                .setAllowedOrigins("*")
            addEndpoint(STOMP_ENDPOINT)
                .setAllowedOrigins("*")
                .withSockJS()
        }
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(inboundChannelInterceptor)
    }


    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
        registration.interceptors(outboundChannelInterceptor)
    }

    @Bean
    fun webSocketClient() : WebSocketStompClient =
        WebSocketStompClient(StandardWebSocketClient()).apply {
            messageConverter = MappingJackson2MessageConverter()
        }
}

