package ng.i.sav.qdroid.infra.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 频道消息频率设置对象
 * @property disableCreateDm    string	是否允许创建私信
 * @property disablePushMsg    string	是否允许发主动消息
 * @property channelIds    string 数组	子频道 id 数组
 * @property channelPushMaxNum    uint32	每个子频道允许主动推送消息最大消息条数
 * */
data class MessageSetting(
    @JsonProperty("disable_create_dm")
    val disableCreateDm: String,
    @JsonProperty("disable_push_msg")
    val disablePushMsg: String,
    @JsonProperty("channel_ids")
    val channelIds: List<String>,
    @JsonProperty("channel_push_max_num")
    val channelPushMaxNum: Int,
)
