package com.example.cs_project

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.matheclipse.core.eval.ExprEvaluator
import org.matheclipse.core.interfaces.IExpr
import kotlin.collections.iterator

class FormulaViewModel(application: Application) : AndroidViewModel(application) {

    private val _formulas = MutableStateFlow<Map<String, List<Formula>>>(emptyMap())
    val formulas: StateFlow<Map<String, List<Formula>>> get() = _formulas.asStateFlow()

    private val jsonFormat = Json { prettyPrint = true }
    private val evaluator = ExprEvaluator()

    fun loadFormulasFromAssets() {
        viewModelScope.launch {
            try {
                val jsonContent = getApplication<Application>().assets.open("formulas.json")
                    .bufferedReader().use { it.readText() }

                val formulasMap: Map<String, List<Formula>> =
                    jsonFormat.decodeFromString(jsonContent)

                val parsedFormulasMap: Map<String, List<Formula>> =
                    formulasMap.mapValues { (topic, formulas) ->
                        formulas.map { formula ->
                            try {
                                val parsedVarList = mutableListOf<IExpr>()
                                val varNameList = mutableListOf<String>()
                                val latexVariables = mutableListOf<String>()

                                for (entry in formula.variables) {
                                    val parsedVariable = evaluator.parse(fixString(entry.key))
                                    val texVariable = convertToLatex(parsedVariable)
                                    parsedVarList.add(parsedVariable)
                                    varNameList.add(entry.value)
                                    latexVariables.add(texVariable)
                                    evaluator.eval("Symbol(\"${fixString(entry.key)}\")")
                                }

                                val parsed = evaluator.eval("Unevaluated(${fixString(formula.formula)})")
                                val latexFormula = convertToLatex(parsed)
                                Log.d("Symja", "Parsed formula: $parsed")

                                formula.copy(
                                    parsed = parsed,
                                    parsedVariables = parsedVarList,
                                    latexVariables = latexVariables,
                                    variableNames = varNameList,
                                    latexFormula = latexFormula
                                )
                            } catch (e: Exception) {
                                Log.e(
                                    "Symja",
                                    "Parsing Failed for formula: ${formula.name} - ${e.message}"
                                )
                                formula.copy(parsed = null)
                            }
                        }
                    }

                _formulas.value = parsedFormulasMap
                Log.d("JSON", "Loaded and parsed formulas from assets: $parsedFormulasMap")

            } catch (e: Exception) {
                Log.e("JSON", "Error loading formulas from assets", e)
            }
        }
    }
}