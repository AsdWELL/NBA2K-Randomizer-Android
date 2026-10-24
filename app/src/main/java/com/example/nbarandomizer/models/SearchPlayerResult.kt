package com.example.nbarandomizer.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchPlayerResult(
    override val id: Int = 0,
    override val name: String,
    override val team: String,
    override val overall: Rating,
    override val url: String,
    override val photoUrl: String
) : IPlayerBase
