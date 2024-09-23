package ng.i.sav.bot.qdroid.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @property id    string	用户 id
 * @property username    string	用户名
 * @property avatar    string	用户头像地址
 * @property bot    bool	是否是机器人
 * @property unionOpenid    string	特殊关联应用的 openid，需要特殊申请并配置后才会返回。如需申请，请联系平台运营人员。
 * @property unionUserAccount    string	机器人关联的互联应用的用户信息，与union_openid关联的应用是同一个。如需申请，请联系平台运营人员。
 * */
data class User(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("username")
    val username: String,
    @JsonProperty("avatar")
    val avatar: String?,
    @JsonProperty("bot")
    val bot: Boolean,
    @JsonProperty("union_openid")
    val unionOpenid: String?,
    @JsonProperty("union_user_account")
    val unionUserAccount: String?,
    @JsonProperty("status")
    val status: Int,
)
