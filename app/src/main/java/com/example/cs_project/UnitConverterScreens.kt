package com.example.cs_project

import android.content.Context
import android.graphics.Color as colors
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import katex.hourglass.`in`.mathlib.MathView
import tech.units.indriya.unit.Units
import javax.measure.MetricPrefix
import javax.measure.Quantity
import javax.measure.Unit as unit
import javax.measure.quantity.Acceleration
import javax.measure.quantity.AmountOfSubstance
import javax.measure.quantity.Angle
import javax.measure.quantity.Area
import javax.measure.quantity.CatalyticActivity
import javax.measure.quantity.ElectricCapacitance
import javax.measure.quantity.ElectricCharge
import javax.measure.quantity.ElectricConductance
import javax.measure.quantity.ElectricCurrent
import javax.measure.quantity.ElectricInductance
import javax.measure.quantity.ElectricPotential
import javax.measure.quantity.ElectricResistance
import javax.measure.quantity.Energy
import javax.measure.quantity.Force
import javax.measure.quantity.Frequency
import javax.measure.quantity.Illuminance
import javax.measure.quantity.Length
import javax.measure.quantity.LuminousFlux
import javax.measure.quantity.LuminousIntensity
import javax.measure.quantity.MagneticFlux
import javax.measure.quantity.MagneticFluxDensity
import javax.measure.quantity.Mass
import javax.measure.quantity.Power
import javax.measure.quantity.Pressure
import javax.measure.quantity.RadiationDoseAbsorbed
import javax.measure.quantity.RadiationDoseEffective
import javax.measure.quantity.Radioactivity
import javax.measure.quantity.SolidAngle
import javax.measure.quantity.Speed
import javax.measure.quantity.Temperature
import javax.measure.quantity.Time
import javax.measure.quantity.Volume

object MathViewUnitsPool {
    private val pool = mutableListOf<MathView>()

    fun get(context: Context): MathView {
        return if (pool.isNotEmpty()) {
            pool.removeAt(0)
        } else {
            MathView(context).apply {
                setTextColor(colors.WHITE)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverter(modifier: Modifier = Modifier) {
    var curUnits = remember { mutableStateListOf<unit<out Quantity<*>>>() }
    var selectedQty by remember { mutableStateOf<Class<out Quantity<*>>>(Length::class.java) }
    var sourceUnit by remember { mutableStateOf<unit<*>>(Units.METRE_PER_SECOND) }
    var targetUnit by remember { mutableStateOf<unit<*>>(Units.KILOMETRE_PER_HOUR) }
    var inputValue by remember { mutableStateOf("") }
    var convertedValue by remember { mutableStateOf("") }
    var showDrawer by remember { mutableStateOf(false) }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState()

    val quantityToUnits: Map<Class<out Quantity<*>>, List<unit<out Quantity<*>>>> = mapOf(
        Acceleration::class.java to listOf(Units.METRE_PER_SQUARE_SECOND),
        AmountOfSubstance::class.java to listOf(Units.MOLE),
        Angle::class.java to listOf(Units.RADIAN),
        Area::class.java to listOf(Units.SQUARE_METRE),
        CatalyticActivity::class.java to listOf(Units.KATAL),
        ElectricCapacitance::class.java to listOf(Units.FARAD),
        ElectricCharge::class.java to listOf(Units.COULOMB),
        ElectricConductance::class.java to listOf(Units.SIEMENS),
        ElectricCurrent::class.java to listOf(Units.AMPERE),
        ElectricInductance::class.java to listOf(Units.HENRY),
        ElectricPotential::class.java to listOf(Units.VOLT),
        ElectricResistance::class.java to listOf(Units.OHM),
        Energy::class.java to listOf(Units.JOULE),
        Force::class.java to listOf(Units.NEWTON),
        Frequency::class.java to listOf(Units.HERTZ),
        Illuminance::class.java to listOf(Units.LUX),
        Length::class.java to listOf(Units.METRE),
        LuminousFlux::class.java to listOf(Units.LUMEN),
        LuminousIntensity::class.java to listOf(Units.CANDELA),
        MagneticFlux::class.java to listOf(Units.WEBER),
        MagneticFluxDensity::class.java to listOf(Units.TESLA),
        Mass::class.java to listOf(Units.GRAM),
        Power::class.java to listOf(Units.WATT),
        Pressure::class.java to listOf(Units.PASCAL),
        RadiationDoseAbsorbed::class.java to listOf(Units.GRAY),
        RadiationDoseEffective::class.java to listOf(Units.SIEVERT),
        Radioactivity::class.java to listOf(Units.BECQUEREL),
        SolidAngle::class.java to listOf(Units.STERADIAN),
        Speed::class.java to listOf(Units.METRE_PER_SECOND, Units.KILOMETRE_PER_HOUR),
        Temperature::class.java to listOf(Units.CELSIUS, Units.KELVIN),
        Time::class.java to listOf(Units.SECOND, Units.MINUTE, Units.HOUR, Units.YEAR),
        Volume::class.java to listOf(Units.CUBIC_METRE, Units.LITRE)
    )

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp).pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    focusManager.clearFocus()
                }
            )
        }
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        searchQuery = ""
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
        LazyVerticalGrid (
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val filteredOptions = quantityToUnits.keys.filter {
                it.simpleName.replace(Regex("([a-z])([A-Z])"), "$1 $2")
                    .contains(searchQuery, ignoreCase = true)
            }
            items(filteredOptions) { option ->
                Card(
                    modifier = Modifier.aspectRatio(1f),
                    onClick = {
                        curUnits.clear()
                        for (x in quantityToUnits[option]!!) {
                            curUnits.add(x)
                            curUnits.add(MetricPrefix.NANO(x))
                            curUnits.add(MetricPrefix.MICRO(x))
                            curUnits.add(MetricPrefix.MILLI(x))
                            curUnits.add(MetricPrefix.CENTI(x))
                            curUnits.add(MetricPrefix.DECI(x))
                            curUnits.add(MetricPrefix.DEKA(x))
                            curUnits.add(MetricPrefix.HECTO(x))
                            curUnits.add(MetricPrefix.KILO(x))
                            curUnits.add(MetricPrefix.MEGA(x))
                            curUnits.add(MetricPrefix.GIGA(x))
                        }
                        sourceUnit = curUnits[0]
                        targetUnit = curUnits[1]
                        selectedQty = option
                        showDrawer = true
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            var expression by remember { mutableStateOf(quantityToUnits[option]?.get(0).toString()) }

                            AndroidView(
                                factory = { context -> MathViewUnitsPool.get(context) },
                                update = { mathView -> mathView.setDisplayText("$$${expression}$$") },
                                onRelease = { mathView -> MathViewUnitsPool.release(mathView) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(option.simpleName.replace(Regex("([a-z])([A-Z])"), "$1 $2"), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        if (showDrawer) {
            ModalBottomSheet(
                onDismissRequest = {
                    showDrawer = false
                },
                sheetState = sheetState,
            ) {
                val keyboardController = LocalSoftwareKeyboardController.current

                Column(
                    modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    }
                ) {
                    LaunchedEffect(inputValue) {
                        try {
                            val input = inputValue.toDouble()
                            val ans = convertUnit(input, sourceUnit, targetUnit)
                            convertedValue = ans.toString()
                        } catch (_: Exception) {}
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = inputValue,
                            onValueChange = {
                                inputValue = it
                            },
                            label = { Text("Enter Value") }
                        )
                        Spacer(Modifier.height(16.dp))
                        Row {
                            Text("Unit: ")
                            Box(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .clickable { fromExpanded = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = sourceUnit.toString())
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }

                                DropdownMenu(
                                    expanded = fromExpanded,
                                    onDismissRequest = { fromExpanded = false },
                                    modifier = Modifier.requiredHeightIn(max = 300.dp)
                                ) {
                                    Log.d("Symja", curUnits.toString())
                                    curUnits.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.toString()) },
                                            onClick = {
                                                sourceUnit = option
                                                fromExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = convertedValue,
                            onValueChange = {
                                convertedValue = it
                            },
                            label = { Text("Enter Value") }
                        )
                        Spacer(Modifier.height(16.dp))
                        Row {
                            Text("Unit: ")
                            Box(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .clickable { toExpanded = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = targetUnit.toString())
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }

                                DropdownMenu(
                                    expanded = toExpanded,
                                    onDismissRequest = { toExpanded = false },
                                    modifier = Modifier.requiredHeightIn(max = 300.dp)
                                ) {
                                    curUnits.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.toString()) },
                                            onClick = {
                                                targetUnit = option
                                                toExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}