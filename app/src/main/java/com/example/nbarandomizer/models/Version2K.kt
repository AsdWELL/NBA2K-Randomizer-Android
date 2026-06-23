package com.example.nbarandomizer.models

enum class Version2K {
    /*_2K21,
    _2K22,
    _2K23,
    _2K24,*/
    _2K25,
    _2K26,
    _2K27;

    override fun toString(): String {
        return super.toString().removePrefix("_")
    }

    companion object {
        fun latest() = Version2K.entries.last()
    }
}