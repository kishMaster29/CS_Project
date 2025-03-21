package com.example.cs_project

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SimulationList(
    modifier: Modifier = Modifier,
    simulationViewModel: SimulationViewModel,
    navigateToSimulationView: (String, Int) -> Unit
) {
    val simulations by simulationViewModel.simulations.collectAsStateWithLifecycle(initialValue = emptyMap())
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
            label = { Text("Search name") },
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
                    simulations.keys.toMutableList().apply { add("No Filter") },
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

        val filteredFormulas = simulations.flatMap { (topic, simList) ->
            simList.filter { sim ->
                (selectedTopic == "No Filter" || selectedTopic == topic) && (searchTerm.isEmpty() || sim.name.contains(searchTerm, ignoreCase = true))
            }.map { topic to it }
        }

        val sortedSims = when (selectedSort) {
            "A to Z" -> filteredFormulas.sortedBy { it.second.name }
            "By Topic" -> filteredFormulas.sortedWith(compareBy({ it.first }, { it.second.name }))
            else -> filteredFormulas
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            items(sortedSims) { (topic, sim) ->
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
                    colors = CardColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.Black,
                    ),
                    onClick = {
                        navigateToSimulationView(topic, simulations[topic]!!.indexOf(sim))
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = sim.name,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = topic,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.DarkGray
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = sim.description,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SimulationView(
    modifier: Modifier = Modifier,
    simulationViewModel: SimulationViewModel,
    topic: String,
    index: Int,
) {
    val simulations by simulationViewModel.simulations.collectAsStateWithLifecycle(initialValue = emptyMap())
    val sim = simulations[topic]?.get(index)

    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val scope = rememberCoroutineScope()
        sim?.let { simulation ->
            var deltaT by remember { mutableStateOf(0.1f) }
            var isRunning by remember { mutableStateOf(false) }
            var isPaused by remember { mutableStateOf(false) }

            if (simulation.name == "Projectile Motion") {
                var velocity by remember { mutableStateOf(40f) }
                var angle by remember { mutableStateOf(45f) }
                var gravity by remember { mutableStateOf(9.81f) }
                val radianAngle = Math.toRadians(angle.toDouble()).toFloat()
                val trajectory = remember { mutableStateListOf<Offset>() }

                val x = remember { Animatable(50f) }
                val y = remember { Animatable(50f) }
                var vx = remember { mutableFloatStateOf(0f) }
                var vy = remember { mutableFloatStateOf(0f) }

                val canvasSize = remember { mutableStateOf(Size.Zero) }

                LaunchedEffect(isRunning, isPaused) {
                    if (isRunning) {
                        Log.d("Sim Proj", "hi")
                        while (isRunning && !isPaused) {
                            val newX = vx.floatValue * deltaT + x.value
                            val newY = vy.floatValue * deltaT - 0.5f * gravity * deltaT * deltaT + y.value
                            vy.floatValue -= gravity * deltaT
                            if (newY < 0 || newX > canvasSize.value.width) {
                                isRunning = false
                                break
                            }
                            trajectory.add(Offset(newX, newY))
                            x.snapTo(newX)
                            y.snapTo(newY)
                            delay(16L)
                        }
                    } else {
                        x.snapTo(50f)
                        y.snapTo(50f)
                    }
                }

                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(300.dp).border(3.dp, Color.White)) {
                        canvasSize.value = size
                        if (trajectory.isNotEmpty()) {
                            drawPath(
                                path = Path().apply {
                                    trajectory.forEachIndexed { index, point ->
                                        if (index == 0) moveTo(point.x, size.height - point.y)
                                        else lineTo(point.x, size.height - point.y)
                                    }
                                },
                                color = Color.Yellow,
                                style = Stroke(width = 2f)
                            )
                        }
                        drawCircle(Color.Red, radius = 10f, center = Offset(x.value, size.height - y.value))
                        val scaleFactor = 2f

                        if (isRunning) {
                            drawLine(
                                color = Color.Cyan,
                                strokeWidth = 4f,
                                start = Offset(x.value, size.height - y.value),
                                end = Offset(x.value + vx.floatValue * scaleFactor, size.height - (y.value + vy.floatValue * scaleFactor))
                            )
                        } else {
                            val vx = velocity * cos(radianAngle)
                            val vy = velocity * sin(radianAngle)

                            drawLine(
                                color = Color.Cyan,
                                strokeWidth = 4f,
                                start = Offset(x.value, size.height - y.value),
                                end = Offset(x.value + vx * scaleFactor, size.height - (y.value + vy * scaleFactor))
                            )
                        }
                    }

                    Slider(value = velocity, onValueChange = { velocity = it }, valueRange = 20f..100f, modifier = Modifier.padding(4.dp))
                    Text("Velocity: ${"%.1f".format(velocity)} m/s")

                    Slider(value = angle, onValueChange = { angle = it }, valueRange = 0f..90f, modifier = Modifier.padding(4.dp))
                    Text("Angle: ${"%.1f".format(angle)}°")

                    Slider(value = gravity, onValueChange = { gravity = it }, valueRange = 2f..20f, modifier = Modifier.padding(4.dp))
                    Text("Gravity: ${"%.1f".format(gravity)} m/s^2")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        if (!isPaused) {
                            Button(
                                onClick = {
                                    isRunning = !isRunning
                                    if (isRunning) {
                                        trajectory.clear()
                                        vx.floatValue = velocity * cos(radianAngle)
                                        vy.floatValue = velocity * sin(radianAngle)
                                    }
                                }
                            ) {
                                if (isRunning) Text("Stop")
                                else Text("Start")
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                        if (isRunning) Button(onClick = { isPaused = !isPaused }) { Text(if (isPaused) "Resume" else "Pause") }
                    }
                }
            }
            else if (simulation.name == "Pendulum") {
                var angle by remember { mutableStateOf(45f) }
                var gravity by remember { mutableStateOf(9.81f) }
                var length by remember { mutableStateOf(2f) }
                val radianAngle = Math.toRadians(angle.toDouble()).toFloat()
                val dampingFactor = 0.05f
                val trajectory = remember { mutableStateListOf<Offset>() }

                val theta = remember { Animatable(radianAngle) }
                val omega = remember { mutableStateOf(0f) }
                val alpha = remember { mutableStateOf(0f) }

                val canvasSize = remember { mutableStateOf(Size.Zero) }

                LaunchedEffect(isRunning, isPaused) {
                    if (isRunning) {
                        while (isRunning && !isPaused) {
                            alpha.value = -gravity / length * sin(theta.value) - dampingFactor * omega.value
                            omega.value += alpha.value * deltaT
                            theta.snapTo(theta.value + omega.value * deltaT)

                            trajectory.add(Offset(
                                x = (length * sin(theta.value) * 100f) + canvasSize.value.width / 2,
                                y = (length * cos(theta.value) * 100f) + 100f
                            ))

                            if (abs(omega.value) < 0.01f && abs(alpha.value) < 0.01f) {
                                isRunning = false
                                break
                            }

                            delay(16L)
                        }
                    } else {
                        theta.snapTo(radianAngle)
                        omega.value = 0f
                    }
                }

                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(300.dp).border(3.dp, Color.White)) {
                        canvasSize.value = size

                        if (trajectory.isNotEmpty()) {
                            drawPath(
                                path = Path().apply {
                                    trajectory.forEachIndexed { index, point ->
                                        if (index == 0) moveTo(point.x, point.y)
                                        else lineTo(point.x, point.y)
                                    }
                                },
                                color = Color.Yellow,
                                style = Stroke(width = 2f)
                            )
                        }

                        val pendulumX: Float
                        val pendulumY: Float

                        if (isRunning) {
                            pendulumX = (length * sin(theta.value) * 100f) + size.width / 2
                            pendulumY = (length * cos(theta.value) * 100f) + 100f
                        } else {
                            pendulumX = ((length * sin(Math.toRadians(angle.toDouble())) * 100f) + size.width / 2).toFloat()
                            pendulumY = ((length * cos(Math.toRadians(angle.toDouble())) * 100f) + 100f).toFloat()
                        }

                        drawCircle(Color.Red, radius = 10f, center = Offset(pendulumX, pendulumY))

                        drawLine(
                            color = Color.White,
                            strokeWidth = 4f,
                            start = Offset(size.width / 2, 100f),
                            end = Offset(pendulumX, pendulumY)
                        )
                    }

                    Slider(value = angle, onValueChange = { angle = it }, valueRange = 0f..90f, modifier = Modifier.padding(4.dp))
                    Text("Initial Angle: ${"%.1f".format(angle)}°")
                    Slider(value = gravity, onValueChange = { gravity = it }, valueRange = 2f..20f, modifier = Modifier.padding(4.dp))
                    Text("Gravity: ${"%.1f".format(gravity)} m/s^2")
                    Slider(value = length, onValueChange = { length = it }, valueRange = 1f..3.8f, modifier = Modifier.padding(4.dp))
                    Text("Length: ${"%.1f".format(length)} m")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        if (!isPaused) {
                            Button(
                                onClick = {
                                    if (!isRunning) {
                                        trajectory.clear()
                                        scope.launch {
                                            theta.snapTo(Math.toRadians(angle.toDouble()).toFloat())
                                        }
                                    }
                                    isRunning = !isRunning
                                }
                            ) {
                                if (isRunning) Text("Stop")
                                else Text("Start")
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                        if (isRunning) Button(onClick = { isPaused = !isPaused }) { Text(if (isPaused) "Resume" else "Pause") }
                    }
                }
            }
            else if (simulation.name == "Planetary Motion") {
                val G = 5f
                var initialDistance by remember { mutableStateOf(100f) }
                var starMass by remember { mutableStateOf(1e3f) }
                var planetMass by remember { mutableStateOf(1e3f) }
                var planetVX by remember { mutableStateOf(0f) }
                var planetVY by remember { mutableStateOf(0f) }
                var starVX by remember { mutableStateOf(0f) }
                var starVY by remember { mutableStateOf(0f) }
                var initialVel by remember { mutableStateOf(0f) }
                val canvasSize = remember { mutableStateOf(Size.Zero) }

                var starX by remember { mutableStateOf(0f) }
                var starY by remember { mutableStateOf(0f) }

                var planetX by remember { mutableStateOf(0f) }
                var planetY by remember { mutableStateOf(0f) }

                val planetTrajectory = remember { mutableStateListOf<Offset>() }
                val starTrajectory = remember { mutableStateListOf<Offset>() }

                LaunchedEffect(isRunning, isPaused) {
                    if (isRunning) {
                        while (isRunning && !isPaused) {
                            val dx = (planetX - starX).toDouble()
                            val dy = (planetY - starY).toDouble()
                            val r = sqrt(dx * dx + dy * dy)

                            if (r < 5f) {
                                isRunning = false
                                break
                            }

                            val force = (G * planetMass * starMass) / (r * r)

                            val ax = -(force * dx / r).toFloat() / planetMass
                            val ay = -(force * dy / r).toFloat() / planetMass

                            planetVX += ax * deltaT
                            planetVY += ay * deltaT

                            planetX += planetVX * deltaT
                            planetY += planetVY * deltaT

                            val starAx = (force * dx / r).toFloat() / starMass
                            val starAy = (force * dy / r).toFloat() / starMass

                            starVX += starAx * deltaT
                            starVY += starAy * deltaT

                            starX += starVX * deltaT
                            starY += starVY * deltaT

                            if (planetX < 0 || planetX > canvasSize.value.width || planetY < 0 || planetY > canvasSize.value.height ||
                                starX < 0 || starX > canvasSize.value.width || starY < 0 || starY > canvasSize.value.height) {
                                isRunning = false
                                break
                            }

                            planetTrajectory.add(Offset(planetX, planetY))
                            starTrajectory.add(Offset(starX, starY))

                            delay(16L)
                        }
                    }
                }

                val scrollState = rememberScrollState()
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(300.dp).border(3.dp, Color.White)) {
                        canvasSize.value = size

                        if (!isRunning) {
                            starX = canvasSize.value.width / 2
                            starY = canvasSize.value.height / 2
                            planetX = canvasSize.value.width / 2 - initialDistance
                            planetY = canvasSize.value.height / 2
                        }
                        drawCircle(Color.Yellow, radius = 10f, center = Offset(starX, size.height - starY))
                        drawCircle(Color.Red, radius = 10f, center = Offset(planetX, size.height - planetY))

                        if (planetTrajectory.isNotEmpty()) {
                            drawPath(
                                path = Path().apply {
                                    planetTrajectory.forEachIndexed { index, point ->
                                        if (index == 0) moveTo(point.x, size.height - point.y)
                                        else lineTo(point.x, size.height - point.y)
                                    }
                                },
                                color = Color.Green,
                                style = Stroke(width = 2f)
                            )
                        }
                        if (starTrajectory.isNotEmpty()) {
                            drawPath(
                                path = Path().apply {
                                    starTrajectory.forEachIndexed { index, point ->
                                        if (index == 0) moveTo(point.x, size.height - point.y)
                                        else lineTo(point.x, size.height - point.y)
                                    }
                                },
                                color = Color.Cyan,
                                style = Stroke(width = 2f)
                            )
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState), horizontalAlignment = Alignment.CenterHorizontally) {
                        Slider(value = initialVel, onValueChange = { initialVel = it }, valueRange = 0f..10f, modifier = Modifier.padding(4.dp))
                        Text("Initial Velocity: ${"%.1f".format(initialVel)} m/s")
                        Slider(value = starMass, onValueChange = { starMass = it }, valueRange = 1e3f..1e4f, modifier = Modifier.padding(4.dp))
                        Text("Star Mass: ${"%.1f".format(starMass)} kg")
                        Slider(value = planetMass, onValueChange = { planetMass = it }, valueRange = 1e3f..1e4f, modifier = Modifier.padding(4.dp))
                        Text("Planet Mass: ${"%.1f".format(planetMass)} kg")
                        Slider(value = initialDistance, onValueChange = { initialDistance = it }, valueRange = 100f..300f, modifier = Modifier.padding(4.dp))
                        Text("Initial Distance: ${"%.1f".format(initialDistance)} m")
                        Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            Button(
                                onClick = {
                                    isRunning = !isRunning
                                    if (isRunning) {
                                        planetTrajectory.clear()
                                        starTrajectory.clear()
                                        starX = canvasSize.value.width / 2
                                        starY = canvasSize.value.height / 2
                                        planetX = canvasSize.value.width / 2 - initialDistance
                                        planetY = canvasSize.value.height / 2
                                        planetVX = 0f
                                        planetVY = initialVel
                                        starVX = 0f
                                        starVY = 0f
                                    }
                                }
                            ) {
                                Text(if (isRunning) "Stop" else "Start")
                            }

                            Spacer(modifier = Modifier.width(16.dp))
                            Button(onClick = { isPaused = !isPaused }) {
                                Text(if (isPaused) "Resume" else "Pause")
                            }
                        }
                    }
                }
            }
            else if (simulation.name == "Lorentz Force") {
                val isRunning = remember { mutableStateOf(false) }
                val isPaused = remember { mutableStateOf(false) }
                val deltaT = 0.02f
                val coe = 1f

                val k = 10e15f
                val B = 0f
                var particleCount by remember { mutableStateOf(5) }

                data class Particle(
                    var charge: Float,
                    val mass: Float,
                    var position: MutableState<Offset>,
                    var velocity: MutableState<Offset>
                )

                val canvasSize = remember { mutableStateOf(Size.Zero) }

                var particles = remember {
                    MutableList(particleCount) {
                        Particle(
                            charge = listOf(-1f, 1f).random() * 1e-6f,
                            mass = 1e-3f,
                            position = mutableStateOf(Offset(0f, 0f)),
                            velocity = mutableStateOf(Offset(0f, 0f))
                        )
                    }
                }

                fun applyCoulombForces() {
                    for (i in particles.indices) {
                        var netForce = Offset.Zero
                        for (j in particles.indices) {
                            if (i != j) {
                                val p1 = particles[i]
                                val p2 = particles[j]
                                val r = p2.position.value - p1.position.value
                                val distance = r.getDistance().coerceAtLeast(30f)
                                val forceMagnitude = (k * p1.charge * p2.charge) / (distance * distance)
                                val force = r * (forceMagnitude / r.getDistance())
                                netForce += force
                            }
                        }
                        val acceleration = netForce / particles[i].mass
                        particles[i].velocity.value += acceleration * deltaT
                    }
                }

                fun handleCollisions() {
                    for (particle in particles) {
                        var pos = particle.position.value
                        var vel = particle.velocity.value
                        val radius = 10f

                        val minX = radius
                        val maxX = canvasSize.value.width - radius
                        val minY = radius
                        val maxY = canvasSize.value.height - radius

                        if (pos.x < minX) {
                            pos = Offset(minX, pos.y)
                            vel = Offset(-vel.x, vel.y) * coe
                        } else if (pos.x > maxX) {
                            pos = Offset(maxX, pos.y)
                            vel = Offset(-vel.x, vel.y) * coe
                        }

                        if (pos.y < minY) {
                            pos = Offset(pos.x, minY)
                            vel = Offset(vel.x, -vel.y) * coe
                        } else if (pos.y > maxY) {
                            pos = Offset(pos.x, maxY)
                            vel = Offset(vel.x, -vel.y) * coe
                        }

                        particle.position.value = pos
                        particle.velocity.value = vel
                    }
                }

                LaunchedEffect(isRunning.value, isPaused.value) {
                    if (isRunning.value) {
                        while (isRunning.value && !isPaused.value) {
                            applyCoulombForces()
                            for (particle in particles) {
                                particle.position.value += particle.velocity.value * deltaT
                            }
                            handleCollisions()
                            delay(16L)
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(
                        modifier = Modifier.size(300.dp).border(3.dp, Color.White)
                    ) {
                        canvasSize.value = size

                        particles.forEach { particle ->
                            drawCircle(
                                color = if (particle.charge > 0) Color.Red else Color.Blue,
                                radius = 5f,
                                center = particle.position.value
                            )
                        }
                    }

                    Spacer(modifier.height(16.dp))

                    Row {
                        Button(
                            onClick = {
                                isRunning.value = !isRunning.value
                                if (isRunning.value) {
                                    particles.clear()
                                    particles.addAll(List(particleCount) {
                                        Particle(
                                            charge = listOf(-1f, 1f).random() * 1e-6f,
                                            mass = 1e-3f,
                                            position = mutableStateOf(Offset((50..(canvasSize.value.width - 50).toInt()).random().toFloat(), (50..(canvasSize.value.height - 50).toInt()).random().toFloat())),
                                            velocity = mutableStateOf(Offset(0f, 0f))
                                        )
                                    })
                                    Log.d("SIMM", particles.toString())
                                }
                            }
                        ) {
                            if (isRunning.value) Text("Stop")
                            else Text("Start")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        if (isRunning.value) {
                            Button(onClick = { isPaused.value = !isPaused.value }) {
                                Text(if (isPaused.value) "Resume" else "Pause")
                            }
                        }
                    }
                }
            }
        }
    }
}