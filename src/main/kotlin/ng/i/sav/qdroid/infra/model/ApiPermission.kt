package ng.i.sav.qdroid.infra.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 接口权限对象 (APIPermission)
 * @property path    String    API 接口名，例如 /guilds/{guild_id}/members/{user_id}
 * @property method    String    请求方法，例如 GET
 * @property desc    String    API 接口名称，例如 获取频道信
 * @property authStatus    Int    授权状态，auth_stats 为 1 时已授权
 */
data class APIPermission(
    @JsonProperty("path")
    val path: String,
    @JsonProperty("method")
    val method: String,
    @JsonProperty("desc")
    val desc: String,
    @JsonProperty("auth_status")
    val authStatus: Int
)

/**
 * 接口权限需求对象（APIPermissionDemand）
 * @property guildId    String    申请接口权限的频道 id
 * @property channelId    String    接口权限需求授权链接发送的子频道 id
 * @property apiIdentify    APIPermissionDemandIdentify    权限接口唯一标识
 * @property title    String    接口权限链接中的接口权限描述信息
 * @property desc    String    接口权限链接中的机器人可使用功能的描述信息
 */
data class APIPermissionDemand(
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("api_identify")
    val apiIdentify: APIPermissionDemandIdentify,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("desc")
    val desc: String
)

/**
 * 接口权限需求标识对象（APIPermissionDemandIdentify）
 * @property path    String    API 接口名，例如 /guilds/{guild_id}/members/{user_id}
 * @property method    String    请求方法，例如 GET
 */
data class APIPermissionDemandIdentify(
    @JsonProperty("path")
    val path: String,
    @JsonProperty("method")
    val method: String
){
    companion object{

    }
}
