package com.procrastinationcollaboration.miraunicornledlamp.services.dto

import com.squareup.moshi.Json

data class ModesDto(@Json(name = "modes") val modes: Array<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ModesDto

        if (!modes.contentEquals(other.modes)) return false
        return true
    }

    override fun hashCode(): Int {
        return modes.contentHashCode()
    }
}
