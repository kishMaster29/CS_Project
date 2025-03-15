package com.example.cs_project

import FormulaViewModel
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.cs_project.ui.theme.CS_ProjectTheme
import com.example.cs_project.ui.theme.easyColor
import com.example.cs_project.ui.theme.hardColor
import com.example.cs_project.ui.theme.mediumColor
import katex.hourglass.`in`.mathlib.MathView
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.matheclipse.core.basic.AndroidLoggerFix
import org.matheclipse.core.expression.F
import org.matheclipse.core.interfaces.IAST
import org.matheclipse.core.interfaces.IAssociation
import org.matheclipse.core.interfaces.IExpr
import org.matheclipse.core.parser.ExprParser

import java.io.Writer
import android.graphics.Color as colors


@Serializable
object FormulaListPage

@Serializable
object BookmarkedPage

@Serializable
object SimulationListPage

@Serializable
object ConstantsListPage

@Serializable
object UnitsConverterPage

@Serializable
object AboutPage

@Serializable
object HelpPage

@Serializable
object SettingsPage

@Serializable
data class FormulaViewPage(val topic: String, val index: Int)

@Serializable
data class CalculatorViewPage(val topic: String, val index: Int)

val difficultyStringMap = mapOf(
    0 to "Easy",
    1 to "Medium",
    2 to "Hard"
)

val difficultyColorMap = mapOf(
    0 to easyColor,
    1 to mediumColor,
    2 to hardColor
)

class MainActivity : ComponentActivity() {

    private val formulaViewModel: FormulaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AndroidLoggerFix.fix()
        setContent {
            CS_ProjectTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                LaunchedEffect(Unit) {
                    formulaViewModel.loadFormulasFromAssets()
                }
                PhyFlowScaffold(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    content = { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = FormulaListPage
                        ) {
                            composable<FormulaListPage> {
                                FormulaList(
                                    modifier = Modifier.padding(innerPadding),
                                    formulaViewModel = formulaViewModel,
                                    navigateToFormulaViewPage = { topic, index ->
                                        navController.navigate(FormulaViewPage(topic, index))
                                    }
                                )
                            }

                            composable<FormulaViewPage> { backStackEntry ->
                                val formulaViewPage = backStackEntry.toRoute<FormulaViewPage>()
                                FormulaView(
                                    modifier = Modifier.padding(innerPadding),
                                    formulaViewModel = formulaViewModel,
                                    topic = formulaViewPage.topic,
                                    index = formulaViewPage.index,
                                    navigateToCalculatorViewPage = {
                                        navController.navigate(
                                            CalculatorViewPage(
                                                formulaViewPage.topic,
                                                formulaViewPage.index
                                            )
                                        )
                                    }
                                )
                            }

                            composable<CalculatorViewPage> { backStackEntry ->
                                val calculatorViewPage =
                                    backStackEntry.toRoute<CalculatorViewPage>()
                                CalculatorView(
                                    modifier = Modifier.padding(innerPadding),
                                    formulaViewModel = formulaViewModel,
                                    topic = calculatorViewPage.topic,
                                    index = calculatorViewPage.index,
                                    snackbarHostState = snackbarHostState
                                )
                            }

                            composable<BookmarkedPage> {
                                Bookmarked(
                                    modifier = Modifier.padding(innerPadding),
                                )
                            }

                            composable<SimulationListPage> {
                                SimulationList(
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }

                            composable<ConstantsListPage> {
                                ConstantsList(
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }

                            composable<UnitsConverterPage> {
                                UnitConverter(
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }

                            composable<AboutPage> {
                                About(
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }

                            composable<HelpPage> {
                                Help(
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }

                            composable<SettingsPage> {
                                Settings(
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }

                        }
                    },
                    navigateToBookmarked = {
                        navController.navigate(BookmarkedPage)
                    },
                    navigateToFormulaList = {
                        navController.navigate(FormulaListPage)
                    },
                    navigateToSimulationList = {
                        navController.navigate(SimulationListPage)
                    },
                    navigateToUnitsConverter = {
                        navController.navigate(UnitsConverterPage)
                    },
                    navigateToConstantsList = {
                        navController.navigate(ConstantsListPage)
                    },
                    navigateToAbout = {
                        navController.navigate(AboutPage)
                    },
                    navigateToHelp = {
                        navController.navigate(HelpPage)
                    },
                    navigateToSettings = {
                        navController.navigate(SettingsPage)
                    }
                )
            }
        }
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
            items(sortedFormulas) { (topic, formula) ->
                var isMathViewLoaded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (isMathViewLoaded) 1f else 0f),
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

                        AndroidView(
                            factory = { context ->
                                MathView(context).apply {
                                    setTextSize(20)
                                    setTextColor(colors.BLACK)
                                    setViewBackgroundColor(colors.TRANSPARENT)
                                    setDisplayText("$$${formula.formula}$$")
                                    isClickable = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged { size ->
                                    if (size.width > 0 && size.height > 0) {
                                        isMathViewLoaded = true
                                    }
                                }
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
    var alphaState by remember { mutableFloatStateOf(0f) }
    var variablesShowing by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = alphaState,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(Unit) {
        alphaState = 1f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
            .alpha(alpha)
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
                                setTextSize(20)
                                setTextColor(colors.BLACK)
                                setViewBackgroundColor(colors.TRANSPARENT)
                                setDisplayText("$$${f.formula}$$")
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
                                        setTextSize(14)
                                        setTextColor(colors.WHITE)
                                        setViewBackgroundColor(colors.TRANSPARENT)
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
    var alphaState by remember { mutableFloatStateOf(0f) }
    var varIndex by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var variablesShowing by remember { mutableStateOf(false) }
    val calculationValues = remember { mutableStateListOf<String>() }
    val latexResults = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var showPopup by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = alphaState,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(Unit) {
        alphaState = 1f
    }

    if (formulas.isNotEmpty()) {
        calculationValues.addAll(List(formula?.variables!!.size) {"0"})
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
            .alpha(alpha)
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
                                setTextSize(20)
                                setTextColor(colors.BLACK)
                                setViewBackgroundColor(colors.TRANSPARENT)
                                setDisplayText("$$${f.formula}$$")
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
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(f.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterStart))
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = {
                            showPopup = true
                        }
                    ) { Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                    ) }

                    if (showPopup) {
                        Dialog(
                            onDismissRequest = { showPopup = false }
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(230.dp)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Text(
                                    text = "Choose a variable to solve for. Then enter any algebraic or numerical expression to input for the other variables. Vectors are allowed with {}",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp)
                                        .wrapContentSize(Alignment.Center),
                                )
                            }
                        }
                    }
                }
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
                                        setTextSize(14)
                                        setTextColor(colors.WHITE)
                                        setViewBackgroundColor(colors.TRANSPARENT)
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
                                    setTextSize(20)
                                    setTextColor(colors.WHITE)
                                    setViewBackgroundColor(colors.TRANSPARENT)
                                    setDisplayText("$$${selectedVarAsLatex}$$")
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
                                        setTextSize(14)
                                        setTextColor(colors.WHITE)
                                        setViewBackgroundColor(colors.TRANSPARENT)
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
                                        evaluator.parse(it)
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
                                    evaluator.eval(value)
                                } catch (ex: Exception) {
                                    valid = false
                                    break
                                }
                            }
                        }
                        if (valid) {
                            scope.launch {
                                try {
                                    latexResults.clear()

                                    val solveExpr = f.parsed!!
                                    val rules = mutableListOf<IAST>()
                                    val subChar = evaluator.eval("\"\\:20AC\"")

                                    f.parsedVariables!!.forEachIndexed { index, variable ->
                                        if (index != varIndex) {
                                            Log.d("Symja", F.Rule(variable, evaluator.eval("ToExpression(\"${calculationValues[index]}\", TeXForm)")).toString())
                                            rules.add(F.Rule(variable, evaluator.eval("ToExpression(\"${calculationValues[index]}\", TeXForm)")))
                                        } else {
                                            rules.add(F.Rule(variable, subChar))
                                        }
                                    }

                                    val rulesString = "{${rules.joinToString(",")}}"

                                    val newExpr = evaluator.eval("ReplaceAll(${solveExpr}, ${rulesString})")
                                    Log.d("Symja Solver", newExpr.toString())
                                    val solution = evaluator.eval("Flatten(Solve(${newExpr}, ${subChar}))")
                                    val solutions = mutableListOf<IExpr>()

                                    if (solution.isList) {
                                        for (i in 1 until solution.size()) {
                                            val value = solution.getAt(i)
                                            val replacedAns = evaluator.eval("ReplaceAll(${value}, ${F.Rule(subChar, f.parsedVariables[varIndex])})")
                                            solutions.add(replacedAns)
                                        }
                                    }

                                    if (solutions.isEmpty()) {
                                        snackbarHostState.showSnackbar(
                                            "No solution found.",
                                            duration = SnackbarDuration.Short
                                        )
                                    } else {
                                        val texResults = convertToLatex(solutions, replaceBrackets = false)
                                        val newResults = mutableListOf<String>()
                                        for (res in texResults) {
                                            newResults.add(res.replace(Regex("^[^=]+"), Regex.escapeReplacement(formula.latexVariables!![varIndex])))
                                        }
                                        latexResults.addAll(newResults)
                                    }
                                } catch (ex: Exception) {
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

@Composable
fun Bookmarked(modifier: Modifier = Modifier) {

}

@Composable
fun SimulationList(modifier: Modifier = Modifier) {

}

@Composable
fun ConstantsList(modifier: Modifier = Modifier) {

}

@Composable
fun UnitConverter(modifier: Modifier = Modifier) {

}

@Composable
fun About(modifier: Modifier = Modifier) {

}

@Composable
fun Help(modifier: Modifier = Modifier) {

}

@Composable
fun Settings(modifier: Modifier = Modifier) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhyFlowScaffold(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    navigateToBookmarked: () -> Unit,
    navigateToFormulaList: () -> Unit,
    navigateToSimulationList: () -> Unit,
    navigateToUnitsConverter: () -> Unit,
    navigateToConstantsList: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToHelp: () -> Unit,
    navigateToSettings: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val titleText = when (currentDestination?.route?.substringBefore("/")) {
        FormulaListPage::class.qualifiedName -> "Formulae"
        BookmarkedPage::class.qualifiedName -> "Bookmarked"
        SimulationListPage::class.qualifiedName -> "Simulations"
        ConstantsListPage::class.qualifiedName -> "Constants"
        UnitsConverterPage::class.qualifiedName -> "Unit Converter"
        AboutPage::class.qualifiedName -> "About"
        HelpPage::class.qualifiedName -> "Help"
        SettingsPage::class.qualifiedName -> "Settings"
        else -> ""
    }

    val selectedItemIndex = when (currentDestination?.route?.substringBefore("/")) {
        BookmarkedPage::class.qualifiedName -> 0
        FormulaListPage::class.qualifiedName -> 1
        SimulationListPage::class.qualifiedName -> 2
        UnitsConverterPage::class.qualifiedName -> 3
        ConstantsListPage::class.qualifiedName -> 4
        AboutPage::class.qualifiedName -> 5
        HelpPage::class.qualifiedName -> 6
        SettingsPage::class.qualifiedName -> 7
        else -> -1
    }

    val drawerFirstSection = listOf(
        Triple("Bookmarked", R.drawable.bookmark, navigateToBookmarked),
        Triple("Formulae", R.drawable.sigma, navigateToFormulaList),
        Triple("Simulations", R.drawable.monitor, navigateToSimulationList),
        Triple("Units Converter", R.drawable.convert, navigateToUnitsConverter),
        Triple("Constants Viewer", R.drawable.constant, navigateToConstantsList)
    )
    val drawerSecondSection = listOf(
        Triple("About", R.drawable.about, navigateToAbout),
        Triple("Help", R.drawable.help, navigateToHelp),
        Triple("Settings", R.drawable.settings, navigateToSettings)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet{
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var index = 0

                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.atom_svgrepo_com),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            "PhyFlow",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    for (item in drawerFirstSection) {
                        NavigationDrawerItem(
                            label = { Text(item.first) },
                            icon = {
                                Icon(
                                    painter = painterResource(item.second),
                                    contentDescription = null
                                )
                            },
                            selected = index == selectedItemIndex,
                            onClick = {
                                scope.launch {
                                    drawerState.apply { close() }
                                }
                                item.third()
                            }
                        )
                        index++
                    }

                    HorizontalDivider()

                    for (item in drawerSecondSection) {
                        NavigationDrawerItem(
                            label = { Text(item.first) },
                            icon = {
                                Icon(
                                    painter = painterResource(item.second),
                                    contentDescription = null
                                )
                            },
                            selected = index == selectedItemIndex,
                            onClick = {
                                scope.launch {
                                    drawerState.apply { close() }
                                }
                                item.third()
                            }
                        )
                        index++
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(titleText)
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
            content = content,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        )
    }
}

@Composable
fun MyDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Box {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    expanded = true
                }
            ) {
                Text(text = selectedOption)
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
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            expanded = false
                            onOptionSelected(option)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun SortByDropdown(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val options = listOf(
        "A to Z",
        "By Difficulty",
        "By Topic"
    )

    var expanded by remember { mutableStateOf(false) }

    Column {
        Box {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    expanded = true
                }
            ) {
                Text(text = selectedOption)
                Spacer(Modifier.width(6.dp))
                Icon(
                    painter = painterResource(R.drawable.sort),
                    contentDescription = null
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                })
            {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            expanded = false
                            onOptionSelected(option)
                        }
                    )
                }
            }
        }
    }
}

