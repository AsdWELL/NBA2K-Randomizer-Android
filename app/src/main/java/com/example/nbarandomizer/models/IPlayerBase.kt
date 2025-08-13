package com.example.nbarandomizer.models

interface IPlayerBase {
    val id: Int
    val name: String
    val team: String
    val overall: Rating
    val url: String
    val photoUrl: String
}