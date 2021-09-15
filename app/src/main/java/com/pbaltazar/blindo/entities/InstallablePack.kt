package com.pbaltazar.blindo.entities

import com.pbaltazar.blindo.graphql.type.SupportedScreenreadersEnum
import org.json.JSONObject

data class InstallablePack(
    val pack: Pack = Pack(),
    val targetScreenreaders: SupportedScreenreadersEnum = SupportedScreenreadersEnum.TALKBACK,
    val translateTo: String? = null,
    val installable: JSONObject = JSONObject()
)
