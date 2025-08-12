package com.example.nbarandomizer.models

data class SearchPlayerResult(
    val name: String,
    val team: String,
    val overall: Rating,
    val url: String,
    val photoUrl: String
)