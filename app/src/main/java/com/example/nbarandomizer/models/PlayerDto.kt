package com.example.nbarandomizer.models

data class PlayerDto (
    override val id: Int,
    override val name: String,
    override val team: String,
    override val overall: Rating,
    val height: Int,
    val position: String,
    override val url: String,
    override val photoUrl: String
) : IPlayerBase