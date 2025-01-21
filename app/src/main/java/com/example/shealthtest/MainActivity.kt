package com.example.shealthtest

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shealthtest.ui.theme.SHealthTestTheme
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalDateFilter
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SHealthTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestScreen(
                        activity = this,
                        modifier = Modifier.displayCutoutPadding().padding(innerPadding).padding(horizontal=12.5.dp).fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun TestScreen(activity:MainActivity, modifier: Modifier = Modifier){
    /*기본적으로 Aggregate Data인 것들을 제외하고, 모두 가장 최근 값을 불러와서 띄운다.*/
    val context = LocalContext.current
    val store:HealthDataStore = HealthDataService.getStore(context)
    val permSet = setOf(
        Permission.of(DataTypes.ACTIVITY_SUMMARY, AccessType.READ),
        Permission.of(DataTypes.ACTIVE_CALORIES_BURNED_GOAL, AccessType.READ),
        Permission.of(DataTypes.ACTIVE_TIME_GOAL, AccessType.READ),
        Permission.of(DataTypes.BLOOD_GLUCOSE, AccessType.READ),
        Permission.of(DataTypes.BLOOD_OXYGEN, AccessType.READ),
        Permission.of(DataTypes.BLOOD_PRESSURE, AccessType.READ),
        Permission.of(DataTypes.BODY_COMPOSITION, AccessType.READ),
        Permission.of(DataTypes.EXERCISE, AccessType.READ),
        Permission.of(DataTypes.EXERCISE_LOCATION, AccessType.READ),
        Permission.of(DataTypes.FLOORS_CLIMBED, AccessType.READ),
        Permission.of(DataTypes.NUTRITION, AccessType.READ),
        Permission.of(DataTypes.NUTRITION_GOAL, AccessType.READ),
        Permission.of(DataTypes.SKIN_TEMPERATURE, AccessType.READ),
        Permission.of(DataTypes.SLEEP, AccessType.READ),
        Permission.of(DataTypes.SLEEP_GOAL, AccessType.READ),
        Permission.of(DataTypes.STEPS, AccessType.READ),
        Permission.of(DataTypes.STEPS_GOAL, AccessType.READ),
        Permission.of(DataTypes.USER_PROFILE, AccessType.READ),
        Permission.of(DataTypes.WATER_INTAKE, AccessType.READ),
        Permission.of(DataTypes.WATER_INTAKE_GOAL, AccessType.READ)
    )
    var nowString:String by remember {mutableStateOf(LocalDateTime.now().toString())};
    /*var activitySummary:ActivitySummaryData by remember {
        mutableStateOf(
            ActivitySummaryData(Duration.ofMillis(0), 0.0f, 0.0f, 0.0f)
        )
    }
    var activeCaloriesBurnedGoal by remember {mutableStateOf(0)}
    var activeTimeGoal by remember {mutableStateOf(Duration.ofMillis(0))}
    var bloodGlucose by remember {mutableStateOf(BloodGlucoseData())}
    var bloodOxygen by remember {mutableStateOf(BloodOxygenData())}
    var bloodPressure by remember { mutableStateOf(BloodPressureData())}
    var bodyCompositionData by remember { mutableStateOf(BodyCompositionData())}
    var exerciseData by remember { mutableStateOf(ExerciseData())}*/

    val scope = rememberCoroutineScope();

    LaunchedEffect(context) {
        checkAndRequestPermissions(context, activity, permSet)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier=Modifier.padding(horizontal = 1.dp).fillMaxWidth()
        ){
            Text(
                text = "Today's SHealth Data",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Button(onClick={
                //1. Update nowString
                nowString = LocalDateTime.now().toString()

                //2. Get real data
                scope.launch {
                    /*activitySummary = getActivitySummaryData(store)
                    activeCaloriesBurnedGoal = getActiveCaloriesBurnedGoalData(store)
                    activeTimeGoal = getActiveTimeGoalData(store)
                    bloodGlucose = getBloodGlucoseData(store)
                    bloodOxygen = getBloodOxygenData(store)
                    bloodPressure = getBloodPressureData(store)
                    bodyCompositionData = getBodyComposition(store)
                    exerciseData = getExercise(store)
                    getSteps(store)*/
                }
            }){
                Text(text = "Refresh")
            }
        }
        Text(
            text = "Now : $nowString"
        )

        /*ActivitySummaryWidget(activitySummary, modifier=Modifier.fillMaxWidth())
        SummaryWidget("Active Calories Burned Goal", modifier=Modifier.fillMaxWidth()){
            Text(text = "$activeCaloriesBurnedGoal (kcal)")
        }
        BodyCompositionWidget(bodyCompositionData, modifier=Modifier.fillMaxWidth())*/
    }
}
@Composable
fun SummaryWidget(summaryTitle:String, modifier:Modifier = Modifier, content: @Composable () -> Unit){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(12.dp)
    ) {
        Column(
            horizontalAlignment=Alignment.Start
        ) {
            Text(
                text = "$summaryTitle",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            content()
        }
    }
}
@Composable
fun ValueUnit(
    valueText:String,
    nameText:String = "",
    unitText:String = "",
    modifier:Modifier = Modifier
){
    Row{
        Text(text="$nameText : ")
        Text(
            text = "$valueText",
            fontWeight = FontWeight.Bold
        )
        Text(text = "$unitText")
    }
}
/*@Composable
fun ActivitySummaryWidget(activitySummary:ActivitySummaryData, modifier:Modifier = Modifier){
    SummaryWidget("Activity Summary", modifier){
        ValueUnit("${activitySummary.totalActiveTime.seconds}","Active time", "sec")
        ValueUnit("${activitySummary.totalDistance}", "Distance", "m")
        ValueUnit("${activitySummary.totalCaloriesBurned}","Burned Calories", "kcal")
        ValueUnit("${activitySummary.totalActiveCaloriesBurned}", "Burned Calories", "kcal(active)")
    }
}
@Composable
fun BodyCompositionWidget(bodyComposition:BodyCompositionData, modifier:Modifier = Modifier){
    SummaryWidget("Body Composition", modifier){
        ValueUnit("${bodyComposition.basalMetabolicRate}","Basal Metabolic Rate")
        ValueUnit("${bodyComposition.bodyFat}", "Body Fat")
        ValueUnit("${bodyComposition.bodyFatMass}", "Body Fat Mass")
    }
}*/