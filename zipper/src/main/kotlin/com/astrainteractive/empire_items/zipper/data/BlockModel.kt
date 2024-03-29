package com.astrainteractive.empire_items.zipper.data

import com.google.gson.annotations.SerializedName

data class BlockModel(
    val multipart:MutableList<Multipart> = mutableListOf()
)
data class Multipart(
    @SerializedName("when")
    val _when:Map<String,Boolean>,
    val apply: Apply
)
data class Apply(val model:String)