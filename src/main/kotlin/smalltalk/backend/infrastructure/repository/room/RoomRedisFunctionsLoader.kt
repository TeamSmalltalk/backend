package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.redisson.api.RedissonClient
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class RoomRedisFunctionsLoader(
    private val redisson: RedissonClient,
    private val environment: Environment,
    properties: RoomProperties
) {
    private val logger = KotlinLogging.logger { }
    private val libraryNameOfAddMember = properties.getLibraryNameOfAddMember()
    private val libraryNameOfDeleteMember = properties.getLibraryNameOfDeleteMember()
    private val keyOfAddMember = "'${properties.getLibraryFunctionKeyOfAddMember()}'"
    private val keyofDeleteMember = "'${properties.getLibraryFunctionKeyOfDeleteMember()}'"
//    private val addMemberLua = """
//        local value = redis.call("get", KEYS[1])
//        if not value then
//            return "601"
//        end
//        local room = cjson.decode(value)
//        if room.numberOfMember == tonumber(ARGV[1]) then
//            return "602"
//        end
//        local memberId = room.numberOfMember + 1
//        room.numberOfMember = memberId
//        redis.call("set", KEYS[1], cjson.encode(room))
//        if redis.call("llen", KEYS[2]) ~= 0 then
//            return redis.call("lpop", KEYS[2])
//        end
//        return tostring(memberId)
//    """
//    private val deleteMemberLua = """
//        local value = redis.call("get", KEYS[1])
//        if not value then
//            return "601"
//        end
//        local room = cjson.decode(value)
//        if room.numberOfMember == 1 then
//            redis.call("del", KEYS[1], KEYS[2])
//            return nil
//        end
//        room.numberOfMember = room.numberOfMember - 1
//        redis.call("rpush", KEYS[2], ARGV[1])
//        redis.call("set", KEYS[1], cjson.encode(room))
//        return cjson.encode(room)
//    """
    private val functionOfAddMember = """
        function(keys, args)
            local value = redis.call("get", keys[1])
            if not value then
                return "601"
            end
            local room = cjson.decode(value)
            if room.numberOfMember == tonumber(args[1]) then
                return "602"
            end
            local memberId = room.numberOfMember + 1
            room.numberOfMember = memberId
            redis.call("set", keys[1], cjson.encode(room))
            if redis.call("llen", keys[2]) ~= 0 then
                return redis.call("lpop", keys[2])
            end
            return tostring(memberId)
        end
    """
    private val functionOfDeleteMember = """
        function(keys, args)
            local value = redis.call("get", keys[1])
            if not value then
                return "601"
            end
            local room = cjson.decode(value)
            if room.numberOfMember == 1 then
                redis.call("del", keys[1], keys[2])
                return nil
            end
            room.numberOfMember = room.numberOfMember - 1
            redis.call("rpush", keys[2], args[1])
            redis.call("set", keys[1], cjson.encode(room))
            return cjson.encode(room)
        end
    """

    @PostConstruct
    private fun load() {
        redisson.function.run {
            load(libraryNameOfAddMember, getLibrary(keyOfAddMember, functionOfAddMember))
            load(libraryNameOfDeleteMember, getLibrary(keyofDeleteMember, functionOfDeleteMember))
        }
    }

    @PreDestroy
    private fun unload() {
        takeIf { environment.matchesProfiles("test") }
            ?: redisson.function.run {
                delete(libraryNameOfAddMember)
                delete(libraryNameOfDeleteMember)
            }
    }

    private fun getLibrary(key: String, function: String) = "redis.register_function($key, $function)"
}