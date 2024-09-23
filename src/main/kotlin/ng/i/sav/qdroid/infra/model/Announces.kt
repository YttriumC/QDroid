package ng.i.sav.qdroid.infra.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 公告对象
 *
 * @property guildId    String	频道 id
 * @property channelId    String	子频道 id
 * @property messageId    String	消息 id
 * @property announcesType    uint32	公告类别 0:成员公告 1:欢迎公告，默认成员公告
 * @property recommendChannels    RecommendChannel 数组	推荐子频道详情列表
 * */
data class Announces(
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("message_id")
    val messageId: String,
    @JsonProperty("announces_type")
    val announcesType: Int = 0,
    @JsonProperty("recommend_channels")
    val recommendChannels: List<RecommendChannel>,
)

/**
 * 推荐子频道对象
 *
 * @property channelId    String	子频道 id
 * @property introduce    String	推荐语
 * */
data class RecommendChannel(
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("introduce")
    val introduce: String,
)
