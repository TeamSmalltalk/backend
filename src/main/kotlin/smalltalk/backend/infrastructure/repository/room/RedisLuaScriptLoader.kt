package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component

@Component
class RedisLuaScriptLoader(private val redisson: RedissonClient) {
    companion object {
        const val ROOM_SCRIPT_KEY = "room:script"
        const val ROOM_ADD_MEMBER_KEY = "enter"
        const val ROOM_DELETE_MEMBER_KEY = "exit"
    }
    private val logger = KotlinLogging.logger { }
    private val shaCodeStorage = mutableMapOf<String, String>()
    private val addMemberLua = """
        local value = redis.call("get", KEYS[1])
        if not value then
            return "601"
        end
        local room = cjson.decode(value)
        if room.numberOfMember == tonumber(ARGV[1]) then
            return "602"
        end
        local memberId = room.numberOfMember + 1
        room.numberOfMember = memberId
        redis.call("set", KEYS[1], cjson.encode(room))
        if redis.call("llen", KEYS[2]) ~= 0 then
            return redis.call("lpop", KEYS[2])
        end
        return tostring(memberId)
    """
    private val deleteMemberLua = """
        local value = redis.call("get", KEYS[1])
        if not value then
            return "601"
        end
        local room = cjson.decode(value)
        if room.numberOfMember == 1 then
            redis.call("del", KEYS[1], KEYS[2])
            return nil
        end
        room.numberOfMember = room.numberOfMember - 1
        redis.call("rpush", KEYS[2], ARGV[1])
        redis.call("set", KEYS[1], cjson.encode(room))
        return cjson.encode(room)
    """

    @PostConstruct
    private fun load() {
        redisson.script.run {
            shaCodeStorage.putAll(mapOf(
                ROOM_ADD_MEMBER_KEY to scriptLoad(addMemberLua),
                ROOM_DELETE_MEMBER_KEY to scriptLoad(deleteMemberLua)
            ))
            redisson.getMap<String, String>(ROOM_SCRIPT_KEY).putAll(shaCodeStorage)
        }
    }

    fun getShaCode(key: String) = shaCodeStorage[key]
}