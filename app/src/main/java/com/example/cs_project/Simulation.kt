package com.example.cs_project

import kotlinx.serialization.Serializable

@Serializable
data class Simulation (
    val name: String,
    val description: String
)