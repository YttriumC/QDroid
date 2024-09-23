package ng.i.sav.qdroid.infra.model

import com.fasterxml.jackson.annotation.JsonProperty


/**
 * @property channelId    string	子频道 id
 * @property userId    string	用户 id 或 身份组 id，只会返回其中之一
 * @property roleId    string	用户 id 或 身份组 id，只会返回其中之一
 * @property permissions    string	用户拥有的子频道权限
 * */
data class ChannelPermissions(
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("user_id")
    val userId: String?,
    @JsonProperty("role_id")
    val roleId: String?,
    @JsonProperty("permissions")
    val permissions: Int
)

/**
 * 权限是QQ频道管理频道成员的一种方式，管理员可以对不同的人、不同的子频道设置特定的权限。用户的权限包括个人权限和身份组权限两部分，最终生效是取两种权限的并集。
 *
 * 注意：不能设置ID为1的身份组权限。逻辑上未获得任何身份组权限的普通用户被归到"普通用户"身份组（ID=1）。
 *
 * 权限使用位图表示，传递时序列化为十进制数值字符串。如权限值为0x6FFF，会被序列化为十进制"28671"。
 * */
enum class Permissions(
    val permission: Int,
    val permissionName: String
) {
    READ(0x0000000001, "可查看子频道"),
    ADMIN(0x0000000002, "可管理子频道"),
    SPEAK(0x0000000004, "可发言子频道"),
    LIVE(0x0000000008, "可直播子频道");

    fun hasPermission(permissions: Int): Boolean {
        return permissions.and(this.permission) > 0
    }

    operator fun plus(other: Permissions): Int {
        return this.permission.or(other.permission)
    }
}

operator fun Int.plus(other: Permissions): Int {
    return this.or(other.permission)
}
