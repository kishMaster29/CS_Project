package com.example.cs_project

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
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
import androidx.compose.material3.Slider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.matheclipse.core.basic.AndroidLoggerFix
import tech.units.indriya.quantity.Quantities
import javax.measure.Quantity
import kotlin.math.cos
import kotlin.math.sin
import kotlin.text.contains
import javax.measure.Unit as unit

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
data class SimulationViewPage(val topic: String, val index: Int)

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
    private val simulationViewModel: SimulationViewModel by viewModels()

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
                    simulationViewModel.loadSimulationsFromAssets()
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
                                    modifier = Modifier.padding(innerPadding),
                                    simulationViewModel = simulationViewModel,
                                    navigateToSimulationView = { topic, index ->
                                        navController.navigate(SimulationViewPage(topic, index))
                                    }
                                )
                            }

                            composable<SimulationViewPage> { backStackEntry ->
                                val simViewPage = backStackEntry.toRoute<SimulationViewPage>()
                                SimulationView(
                                    modifier = Modifier.padding(innerPadding),
                                    simulationViewModel = simulationViewModel,
                                    topic = simViewPage.topic,
                                    index = simViewPage.index,
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
fun Bookmarked(modifier: Modifier = Modifier) {

}

@Composable
fun ConstantsList(modifier: Modifier = Modifier) {

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
            ModalDrawerSheet {
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
                    },
                    actions = {
                        if (currentDestination?.route?.substringBefore("/") == CalculatorViewPage::class.qualifiedName) {
                            var showPopup by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        showPopup = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }

                                if (showPopup) {
                                    Dialog(
                                        onDismissRequest = { showPopup = false }
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(270.dp)
                                                .padding(16.dp),
                                            shape = RoundedCornerShape(16.dp),
                                        ) {
                                            Text(
                                                text = "Choose a variable to solve for. Then enter any algebraic or numerical expression to input for the other variables. Vectors are allowed with {}. Note that definite integrals will not be performed if the integral does not converge.",
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

fun <Q : Quantity<Q>> convertUnit(value: Double, sourceUnit: unit<*>, targetUnit: unit<*>): Double {
    val sourceUnitCast = sourceUnit as unit<Q>
    val targetUnitCast = targetUnit as unit<Q>

    val qty: Quantity<Q> = Quantities.getQuantity(value, sourceUnitCast)
    val converted: Quantity<Q> = qty.to(targetUnitCast)
    return converted.value.toDouble()
}

