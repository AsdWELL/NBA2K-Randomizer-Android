package com.example.nbarandomizer.models

import kotlinx.serialization.Serializable

enum class Position {
    PG,
    SG,
    SF,
    PF,
    C
}

enum class Epoch {
    Current,
    AllTime
}

@Serializable
data class Player (
    override val id: Int,
    override var name: String,
    override val team: String,
    override val overall: Rating,
    val threePointRating : Rating,
    val dunkRating: Rating,
    val height: Int,
    val position: String,
    override val url: String,
    override val photoUrl: String
) : IPlayerBase