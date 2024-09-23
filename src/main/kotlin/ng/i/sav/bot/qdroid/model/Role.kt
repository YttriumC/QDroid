package ng.i.sav.bot.qdroid.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @property id    string	身份组ID
 * @property name    string	名称
 * @property color    uint32	ARGB的HEX十六进制颜色值转换后的十进制数值
 * @property hoist    uint32	是否在成员列表中单独展示: 0-否, 1-是
 * @property number    uint32	人数
 * @property memberLimit    uint32	成员上限
 * */
data class Role(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("color")
    val color: UInt,
    @JsonProperty("hoist")
    val hoist: Int,
    @JsonProperty("number")
    val number: Int,
    @JsonProperty("member_limit")
    val memberLimit: UInt
) {
}

/**
 * @property guildId    string	频道 ID
 * @property roles    [Role] 对象数组	一组频道身份组对象
 * @property roleNumLimit    string	默认分组上限
 * */
data class RolesListResp(
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("roles")
    val roles: List<Role>,
    @JsonProperty("role_num_limit")
    val roleNumLimit: String
)

/**
 * @property roleId    string	身份组 ID
 * @property role    [Role] 对象	所创建的频道身份组对象
 * */
data class CreatedRole(
    @JsonProperty("role_id")
    val roleId: String,
    @JsonProperty("role")
    val role: Role
)

/**
 * @property guildId    string	频道 ID
 * @property roleId    string	身份组 ID
 * @property role    [Role] 对象	修改后的频道身份组对象
 * */
data class ChangedRole(
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("role_id")
    val roleId: String,
    @JsonProperty("role")
    val role: Role
)
