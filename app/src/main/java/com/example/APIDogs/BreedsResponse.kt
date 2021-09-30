package com.example.APIDogs

import com.google.gson.annotations.SerializedName
import java.util.*

data class BreedsResponse(
    @SerializedName("status") var status: String,
    @SerializedName("message") var breeds: Object
)