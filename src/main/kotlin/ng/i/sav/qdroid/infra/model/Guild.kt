package ng.i.sav.qdroid.infra.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * id	string	频道ID
 * name	string	频道名称
 * icon	string	频道头像地址
 * owner_id	string	创建人用户ID
 * owner	bool	当前人是否是创建人
 * member_count	int	成员数
 * max_members	int	最大成员数
 * description	string	描述
 * joined_at	string	加入时间
 * */
open class Guild(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("icon") val icon: String,
    @JsonProperty("owner_id") val ownerId: String,
    @JsonProperty("owner") val owner: String,
    @JsonProperty("member_count") val memberCount: Int,
    @JsonProperty("max_members") val maxMembers: Int,
    @JsonProperty("description") val description: String,
    @JsonProperty("joined_at") val joinedAt: String
)
