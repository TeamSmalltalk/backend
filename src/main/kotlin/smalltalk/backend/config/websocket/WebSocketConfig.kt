package smalltalk.backend.config.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer


@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig (
    private val inboundChannelInterceptor: InboundChannelInterceptor,
    private val outboundChannelInterceptor: OutboundChannelInterceptor
): WebSocketMessageBrokerConfigurer {
    companion object {
        const val SEND_DESTINATION_PREFIX = "/rooms/chat/"
        const val SUBSCRIBE_ROOM_DESTINATION_PREFIX = "/rooms/"
        const val SUBSCRIBE_SYSTEM_DESTINATION_PREFIX = "/alarm"
        const val STOMP_ENDPOINT = "/ws-connect"
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry
            .setApplicationDestinationPrefixes(SEND_DESTINATION_PREFIX)
            .enableSimpleBroker(
                SUBSCRIBE_ROOM_DESTINATION_PREFIX,
                SUBSCRIBE_SYSTEM_DESTINATION_PREFIX
            )
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.apply {
            addEndpoint(STOMP_ENDPOINT)
                .setAllowedOrigins("*")
        }
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(inboundChannelInterceptor)
    }


    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
        registration.interceptors(outboundChannelInterceptor)
    }
}