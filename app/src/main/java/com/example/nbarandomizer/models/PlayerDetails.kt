package com.example.nbarandomizer.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AttributeRatings (
    val rating: Rating,
    val parameters: List<Rating>
)

@Serializable
data class Badge (
    val name: String,
    val type: String,
    val photoUrl: String,
    val description: String)

@Serializable
data class PlayerDetails (
    override val id: Int,
    override val name: String,
    override val team: String,
    override val overall: Rating,
    val height: Int,
    val position: String,
    override val photoUrl: String,
    val attributes: List<AttributeRatings>,
    val badges: List<Badge>
) : IPlayerBase {
    @Transient
    override lateinit var url: String
}
