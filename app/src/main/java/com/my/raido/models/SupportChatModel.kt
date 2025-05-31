package com.my.raido.models

import java.io.Serializable

data class SupportChatModel(val chatId: String, val msg: String, val type: String, val attachment: String, val date: String ):
    Serializable {
}