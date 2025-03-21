package com.example.cs_project

import android.content.Context
import android.graphics.Color as colors
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import katex.hourglass.`in`.mathlib.MathView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matheclipse.core.expression.F
import org.matheclipse.core.interfaces.IExpr
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.text.contains

object MathViewPool {
    private val pool = mutableListOf<MathView>()

    fun get(context: Context): MathView {
        return if (pool.isNotEmpty()) {
            pool.removeAt(0)
        } else {
            MathView(context).apply {
                setTextColor(colors.BLACK)
                setViewBackgroundColor(colors.TRANSPARENT)
                setTextSize(16)
                isClickable = true
            }
        }
    }

    fun release(mathView: MathView) {
        pool.add(mathView)
    }
}

@Composable
fun FormulaList(
    modifier: Modifier = Modifier,
    formulaViewModel: FormulaViewModel,
    navigateToFormulaViewPage: (String, Int) -> Unit
) {
    val formulas by formulaViewModel.formulas.collectAsStateWithLifecycle(initialValue = emptyMap())
    var searchTerm by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var selectedTopic by remember { mutableStateOf("No Filter") }
    var selectedSort by remember { mutableStateOf("A to Z") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            }
    ) {
        OutlinedTextField(
            value = searchTerm,
            onValueChange = { searchTerm = it },
            label = { Text("Search name or tags") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        searchTerm = ""
                        focusManager.clearFocus()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text("Topic: ")
                MyDropdown(
                    formulas.keys.toMutableList().apply { add("No Filter") },
                    selectedOption = selectedTopic,
                    onOptionSelected = { selectedTopic = it }
                )
            }
            Row(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                SortByDropdown(
                    selectedOption = selectedSort,
                    onOptionSelected = { selectedSort = it }
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        val filteredFormulas = formulas.flatMap { (topic, formulaList) ->
            formulaList.filter { formula ->
                (selectedTopic == "No Filter" || selectedTopic == topic) &&
                        (searchTerm.isEmpty() || formula.name.contains(
                            searchTerm,
                            ignoreCase = true
                        ) ||
                                formula.tags.any { it.contains(searchTerm, ignoreCase = true) })
            }.map { topic to it }
        }

        val sortedFormulas = when (selectedSort) {
            "A to Z" -> filteredFormulas.sortedBy { it.second.name }
            "By Difficulty" -> filteredFormulas.sortedWith(
                compareBy(
                    { it.second.difficulty },
                    { it.second.name })
            )

            "By Topic" -> filteredFormulas.sortedWith(compareBy({ it.first }, { it.second.name }))
            else -> filteredFormulas
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            items(sortedFormulas, key = { it.second.formula }) { (topic, formula) ->
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
                    colors = CardColors(
                        containerColor = difficultyColorMap[formula.difficulty]!!,
                        contentColor = Color.Black,
                        disabledContainerColor = difficultyColorMap[formula.difficulty]!!,
                        disabledContentColor = Color.Black,
                    ),
                    onClick = {
                        navigateToFormulaViewPage(topic, formulas[topic]!!.indexOf(formula))
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .background(difficultyColorMap[formula.difficulty]!!)
                    ) {
                        Text(
                            text = formula.name,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = difficultyStringMap[formula.difficulty]!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.DarkGray
                        )

                        val expression by remember { mutableStateOf(formula.latexFormula) }

                        AndroidView(
                            factory = { context -> MathViewPool.get(context) },
                            update = { mathView -> mathView.setDisplayText("$$${expression}$$") },
                            onRelease = { mathView -> MathViewPool.release(mathView) },
                            modifier = Modifier.fillMaxWidth()
                        )

                    }
                }
                Spacer(Modifier.height(16.dp))
            }

        }
    }
}

@Composable
fun FormulaView(
    modifier: Modifier = Modifier,
    formulaViewModel: FormulaViewModel,
    topic: String,
    index: Int,
    navigateToCalculatorViewPage: () -> Unit
) {
    val formulas by formulaViewModel.formulas.collectAsStateWithLifecycle(initialValue = emptyMap())
    val formula = formulas[topic]?.get(index)
    val scrollState = rememberScrollState()
    var variablesShowing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        formula?.let { f ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = difficultyColorMap[f.difficulty]!!,
                    contentColor = Color.Black
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AndroidView(
                        factory = { context ->
                            MathView(context).apply {
                                setTextColor(colors.BLACK)
                                setDisplayText("$$${f.latexFormula}$$")
                                setViewBackgroundColor(colors.TRANSPARENT)
                                setTextSize(16)
                                isClickable = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(topic, Modifier.align(Alignment.CenterStart))
                Text(
                    "Difficulty: ${difficultyStringMap[f.difficulty]}",
                    Modifier.align(Alignment.CenterEnd)
                )
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text(f.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.clickable {
                    variablesShowing = !variablesShowing
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Variables")
                Spacer(Modifier.width(5.dp))
                Icon(
                    painter = if (!variablesShowing) painterResource(R.drawable.arrow_drop_down) else painterResource(R.drawable.arrow_drop_up),
                    contentDescription = null
                )
            }
            if (variablesShowing) {
                Spacer(modifier = Modifier.height(5.dp))
                Column {
                    f.latexVariables?.forEachIndexed { index, variable ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(50.dp)
                        ) {
                            AndroidView(
                                factory = { context ->
                                    MathView(context).apply {
                                        setTextColor(colors.WHITE)
                                        setViewBackgroundColor(colors.TRANSPARENT)
                                        setTextSize(16)
                                        isClickable = true
                                    }
                                },
                                update = { mathView -> mathView.setDisplayText("$$${variable}$$") },
                                modifier = Modifier.width(50.dp)
                            )
                            Spacer(modifier.width(10.dp))
                            Text(f.variableNames!![index], fontSize = 14.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("Description:")
            Spacer(modifier = Modifier.height(5.dp))
            Text(f.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    navigateToCalculatorViewPage()
                }
            ) {
                Text("Calculate")
            }
        }
    }
}

@Composable
fun CalculatorView(
    modifier: Modifier = Modifier,
    formulaViewModel: FormulaViewModel,
    topic: String,
    index: Int,
    snackbarHostState: SnackbarHostState
) {
    val formulas by formulaViewModel.formulas.collectAsStateWithLifecycle(initialValue = emptyMap())
    val formula = formulas[topic]?.get(index)
    val scrollState = rememberScrollState()
    var varIndex by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var variablesShowing by remember { mutableStateOf(false) }
    val calculationValues = remember { mutableStateListOf<String>() }
    val latexResults = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    if (formulas.isNotEmpty()) {
        calculationValues.addAll(List(formula?.variables!!.size) {"0"})
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            }
    ) {
        formula?.let { f ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = difficultyColorMap[f.difficulty]!!,
                    contentColor = Color.Black
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AndroidView(
                        factory = { context ->
                            MathView(context).apply {
                                setTextColor(colors.BLACK)
                                setDisplayText("$$${f.latexFormula}$$")
                                setViewBackgroundColor(colors.TRANSPARENT)
                                setTextSize(16)
                                isClickable = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(topic, Modifier.align(Alignment.CenterStart))
                Text(
                    "Difficulty: ${difficultyStringMap[f.difficulty]}",
                    Modifier.align(Alignment.CenterEnd)
                )
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(f.name, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.clickable {
                    variablesShowing = !variablesShowing
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Variables")
                Spacer(Modifier.width(5.dp))
                Icon(
                    painter = if (!variablesShowing) painterResource(R.drawable.arrow_drop_down) else painterResource(R.drawable.arrow_drop_up),
                    contentDescription = null
                )
            }
            if (variablesShowing) {
                Spacer(modifier = Modifier.height(5.dp))
                Column {
                    f.latexVariables?.forEachIndexed { index, variable ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(50.dp)
                        ) {
                            AndroidView(
                                factory = { context ->
                                    MathView(context).apply {
                                        setTextColor(colors.WHITE)
                                        setDisplayText("$$${formula.latexFormula}$$")
                                        setViewBackgroundColor(colors.TRANSPARENT)
                                        setTextSize(16)
                                        isClickable = true
                                    }
                                },
                                update = { mathView -> mathView.setDisplayText("$$${variable}$$") },
                                modifier = Modifier.width(50.dp)
                            )
                            Spacer(modifier.width(10.dp))
                            Text(f.variableNames!![index], fontSize = 14.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text("Variable to solve for: ")
                Box {
                    var selectedVarAsLatex by remember { mutableStateOf(f.latexVariables!![varIndex]) }
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.clickable {
                            expanded = true
                        }
                    ) {
                        AndroidView(
                            factory = { context ->
                                MathView(context).apply {
                                    setTextColor(colors.WHITE)
                                    setViewBackgroundColor(colors.TRANSPARENT)
                                    setTextSize(16)
                                    isClickable = true
                                }
                            },
                            update = { mathView ->
                                mathView.setDisplayText("$$${selectedVarAsLatex}$$")
                            },
                            modifier = Modifier.size(50.dp, 50.dp),
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        })
                    {
                        f.latexVariables?.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = { Text(text = option) },
                                onClick = {
                                    expanded = false
                                    varIndex = index
                                    selectedVarAsLatex = f.latexVariables[varIndex]
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                f.latexVariables?.forEachIndexed { index, variable ->
                    if (index != varIndex) {
                        var invalid by remember { mutableStateOf(false) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(50.dp)
                        ) {
                            AndroidView(
                                factory = { context ->
                                    MathView(context).apply {
                                        setTextColor(colors.WHITE)
                                        setViewBackgroundColor(colors.TRANSPARENT)
                                        setTextSize(16)
                                        isClickable = true
                                    }
                                },
                                update = { mathView -> mathView.setDisplayText("$$${variable}$$") },
                                modifier = Modifier.width(50.dp)
                            )
                            Text(":")
                            Spacer(modifier.width(10.dp))
                            BasicTextField(
                                value = calculationValues[index],
                                onValueChange = {
                                    invalid = try {
                                        evaluator.parse("ToExpression(\"${it}\", TeXForm)")
                                        false
                                    } catch (_: Exception) {
                                        true
                                    }
                                    calculationValues[index] = it
                                },
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (!invalid) Color.Gray else Color.Red,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(4.dp)
                                    .height(30.dp)
                                    .fillMaxWidth(),
                                textStyle = TextStyle(fontSize = 14.sp, color = if (isSystemInDarkTheme()) Color.White else Color.Black),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        innerTextField()
                                    }
                                },
                                cursorBrush = if (isSystemInDarkTheme()) SolidColor(Color.White) else SolidColor(Color.Black)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row {
                Button(
                    onClick = {
                        var valid = true
                        for ((i, value) in calculationValues.withIndex()) {
                            if (i != varIndex) {
                                try {
                                    evaluator.eval("ToExpression(\"${value}\", TeXForm)")
                                } catch (ex: Exception) {
                                    valid = false
                                    break
                                }
                            }
                        }
                        if (valid) {
                            scope.launch {
                                try {
                                    val result = withContext(Dispatchers.Default) {
                                        evaluator.eval()

                                        latexResults.clear()

                                        val solveExpr = f.parsed!!
                                        val rules = mutableListOf<String>()
                                        val subChar = evaluator.eval("\"\\.A3\"")

                                        f.parsedVariables!!.forEachIndexed { index, variable ->
                                            if (index != varIndex) {
                                                rules.add("${variable}->${evaluator.eval("ToExpression(\"${calculationValues[index]}\", TeXForm)")}")
                                            } else {
                                                rules.add("${variable}->${subChar}")
                                            }
                                        }

                                        val rulesString = "{${rules.joinToString(",")}}"
                                        Log.d("Symja Solver", rulesString)
                                        var newExpr = evaluator.eval("ReplaceAll(HoldForm(${solveExpr}), ${rulesString})")
                                        Log.d("Symja Solver", newExpr.toString())
                                        val solutions = mutableListOf<IExpr>()

                                        val solution = evaluator.eval("Flatten(Solve(${newExpr}, ${subChar}))")
                                        Log.d("Symja Solver", solution.toString())

                                        if (solution.isList) {
                                            for (i in 1 until solution.size()) {
                                                val value = solution.getAt(i)
                                                val replacedAns = evaluator.eval("ReplaceAll(${value}, ${F.Rule(subChar, f.parsedVariables[varIndex])})")
                                                solutions.add(replacedAns)
                                            }
                                        }

                                        Log.d("Symja Solver", solutions.toString())


                                        if (solutions.isEmpty()) {
                                            "No solution found."
                                        } else {
                                            val newResults = mutableListOf<String>()
                                            for (soln in solutions) {
                                                val texExpr = convertToLatex(soln)
                                                newResults.add(texExpr)
                                            }

                                            latexResults.addAll(newResults)
                                            "Success"
                                        }
                                    }
                                    snackbarHostState.showSnackbar(result, duration = SnackbarDuration.Short)
                                } catch (_: Exception) {
                                    snackbarHostState.showSnackbar(
                                        "An error occurred.",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Syntax Error. Please check your input.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                ) {
                    Text("Go")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {

                    }
                ) {
                    Text("Export")
                }
            }
            Spacer(Modifier.height(8.dp))
            if (!latexResults.isEmpty()) {
                Text("Solutions:")
                Spacer(Modifier.height(8.dp))
            }

            var columnHeight by remember { mutableIntStateOf(0) }

            Column(
                modifier = Modifier.onGloballyPositioned {
                    columnHeight = it.size.height
                }
            ) {
                for (latex in latexResults) {
                    AndroidView(
                        factory = { context ->
                            MathView(context).apply {
                                setTextColor(colors.WHITE)
                                setViewBackgroundColor(colors.TRANSPARENT)
                                setDisplayText("$$${latex}$$")
                                isClickable = false
                                settings.apply {
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    useWideViewPort = true
                                    setSupportZoom(true)
                                }
                                isFocusableInTouchMode = true
                            }
                        },
                        update = { mathView ->
                            mathView.setDisplayText("$$${latex}$$")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            LaunchedEffect(columnHeight) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}