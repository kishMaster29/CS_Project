package com.example.cs_project

import kotlinx.serialization.Serializable
import org.matheclipse.core.interfaces.IExpr

@Serializable
data class Formula(
    val name: String,
    val formula: String,
    val variables: Map<String, String>,
    val tags: List<String>,
    val difficulty: Int,
    val description: String,
    val parsed: IExpr? = null,
    val parsedVariables: List<IExpr>? = null,
    val latexVariables: List<String>? = null,
    val variableNames: List<String>? = null
)