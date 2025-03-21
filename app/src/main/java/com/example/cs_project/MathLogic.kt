package com.example.cs_project

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.matheclipse.core.eval.ExprEvaluator
import org.matheclipse.core.interfaces.IExpr
import kotlin.math.exp

val evaluator = ExprEvaluator(false, 100)

suspend fun solveForExpr(equation: IExpr, solveFor: IExpr):List<IExpr> = withContext(Dispatchers.Default) {
    Log.d("Symja Solver", solveFor.toString())
    val temp = evaluator.eval("\"\\:20AC\"")

    val newEqn = equation.toString().replace("(?<![a-zA-Z])${Regex.escape(solveFor.toString())}(?![a-zA-Z])".toRegex(), temp.toString())

    Log.d("Symja Solver", "Solve(${newEqn}, ${temp})")
    val solution = evaluator.eval("Solve(${newEqn}, ${temp})")
    val solutions = mutableListOf<IExpr>()

    if (solution.isList) {
        for (i in 0 until solution.size()) {
            val solutionItem = solution.getAt(i)

            if (solutionItem.isList && solutionItem.size() > 1) {
                val value = solutionItem.getAt(1)
                val solnString = value.toString().replace(temp.toString(), solveFor.toString())
                solutions.add(evaluator.parse(solnString))
            }
        }
    }

    Log.d("Symja Solver", solutions.toString())
    solutions
}

fun fixString(input: String): String {
    var result = input.replace("Δ", "€")
    val regex = """([a-zA-Z0-9_]+)_\{([a-zA-Z0-9_]+)\}""".toRegex()
    result = regex.replace(result) { matchResult ->
        val part1 = matchResult.groupValues[1]
        val part2 = matchResult.groupValues[2]
        "Subscript($part1, $part2)"
    }
    return result
}

suspend fun convertToLatex(expression: IExpr): String =
    withContext(Dispatchers.Default) {
        val texCommand = "TeXForm(HoldForm($expression))"
        val latexString: String = evaluator.eval(texCommand).toString()

        latexString
            .replace("€", "Δ")
            .replace("==", "=")
            .replace(Regex("\\\\text\\{vec\\}\\((.*?)\\)")) { matchResult ->
                "\\vec{${matchResult.groupValues[1]}}"
            }
            .replace(Regex("""(\\vec\{[^}]*\}\s*\\cdot\s*\\vec\{[^}]*\})|\\cdot""")) { match ->
                match.groupValues[1].ifEmpty { " " }
            }

    }