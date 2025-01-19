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

suspend fun checkAndRequestPermissions(context: Context, activity:MainActivity, permSet:Set<Permission>){
    val store: HealthDataStore = HealthDataService.getStore(context)
    val isAllAllowed:Boolean = store.getGrantedPermissions(permSet).containsAll(permSet)

    if(!isAllAllowed){
        store.requestPermissions(permSet, activity)
    }
}
fun getTodayTimeFilter():LocalTimeFilter{
    val eDate = LocalDateTime.now()
    val sDate = LocalDateTime.of(
        eDate.year,
        eDate.month,
        eDate.dayOfMonth,
        0,
        0
    )
    val localtimeFilter = LocalTimeFilter.of(sDate, eDate)
    return localtimeFilter
}
suspend fun getActivitySummaryData(store:HealthDataStore):ActivitySummaryData{
    val timeFilter = getTodayTimeFilter()

    val distReadRequest = DataType.ActivitySummaryType
        .TOTAL_DISTANCE
        .requestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.DESC)
        .build()
    val aTimeReadRequest = DataType.ActivitySummaryType
        .TOTAL_ACTIVE_TIME
        .requestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.DESC)
        .build()
    val cBurnReadRequest = DataType.ActivitySummaryType
        .TOTAL_CALORIES_BURNED
        .requestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.DESC)
        .build()
    val acBurnReadRequest = DataType.ActivitySummaryType
        .TOTAL_ACTIVE_CALORIES_BURNED
        .requestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.DESC)
        .build()


    val distDataList = store.aggregateData(distReadRequest).dataList
    val aTimeDataList = store.aggregateData(aTimeReadRequest).dataList
    val cBurnDataList = store.aggregateData(cBurnReadRequest).dataList
    val acBurnDataList = store.aggregateData(acBurnReadRequest).dataList

    if(distDataList.isEmpty())
        return ActivitySummaryData(Duration.ofMillis(0))

    Log.d("SHEALTH_TEST", "cBurnDataList.length = ${cBurnDataList.size}")


    val totalDistance:Float = distDataList.first().value!!
    val totalActiveTime: Duration = aTimeDataList.first().value!!
    val caloriesBurned:Float = cBurnDataList.first().value!!
    val activeCaloriesBurned:Float = acBurnDataList.first().value!!

    return ActivitySummaryData(totalActiveTime,totalDistance, caloriesBurned, activeCaloriesBurned)
}
suspend fun getActiveCaloriesBurnedGoalData(store:HealthDataStore):Int{
    val request = DataType.ActiveCaloriesBurnedGoalType
        .LAST
        .requestBuilder
        .setLocalDateFilter(LocalDateFilter.since(LocalDate.now()))
        .setOrdering(Ordering.DESC)
        .build()
    val dataList = store.aggregateData(request).dataList
    if(dataList.isEmpty())
        return 0
    val value:Int = dataList.first().value!!
    return value
}
suspend fun getActiveTimeGoalData(store:HealthDataStore):Duration{
    val request = DataType.ActiveTimeGoalType
        .LAST
        .requestBuilder
        .setLocalDateFilter(LocalDateFilter.since(LocalDate.now()))
        .setOrdering(Ordering.DESC)
        .build()
    val dataList = store.aggregateData(request).dataList
    if(dataList.isEmpty())
        return Duration.ofMillis(0)
    val value = dataList.first().value!!
    return value
}
suspend fun getBloodGlucoseData(store:HealthDataStore):BloodGlucoseData{
    val timeFilter = getTodayTimeFilter()
    val request = DataTypes.BLOOD_GLUCOSE
        .readDataRequestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.DESC)
        .build()
    val dataList = store.readData(request).dataList
    if(dataList.isEmpty())
        return BloodGlucoseData()
    val glucoseLvl:Float = dataList.first().getValue(DataType.BloodGlucoseType.GLUCOSE_LEVEL)!!
    val mealTime:Instant = dataList.first().getValue(DataType.BloodGlucoseType.MEAL_TIME)!!
    val inInsulin:Float = dataList.first().getValue(DataType.BloodGlucoseType.INSULIN_INJECTED)!!
    //val mealStatus: DataType.BloodGlucoseType.MealStatus = dataList.first().getValue(DataType.BloodGlucoseType.MEAL_STATUS)!!

    return BloodGlucoseData(glucoseLvl, mealTime, inInsulin)
}
suspend fun getBloodOxygenData(store:HealthDataStore):BloodOxygenData{
    val timeFilter = getTodayTimeFilter()
    val request = DataTypes.BLOOD_OXYGEN
        .readDataRequestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.DESC)
        .build()
    val dataList = store.readData(request).dataList
    if(dataList.isEmpty())
        return BloodOxygenData()

    return BloodOxygenData(
        dataList.first().getValue(DataType.BloodOxygenType.OXYGEN_SATURATION)!!,
        dataList.first().getValue(DataType.BloodOxygenType.MIN_OXYGEN_SATURATION)!!,
        dataList.first().getValue(DataType.BloodOxygenType.MAX_OXYGEN_SATURATION)!!
    )
}
suspend fun getBloodPressureData(store:HealthDataStore):BloodPressureData{
    val timeFilter = getTodayTimeFilter()
    val request = DataTypes.BLOOD_PRESSURE
        .readDataRequestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.DESC)
        .build()
    val dataList = store.readData(request).dataList
    if(dataList.isEmpty())
        return BloodPressureData()
    return BloodPressureData(
        dataList.first().getValue(DataType.BloodPressureType.MEAN)!!,
        dataList.first().getValue(DataType.BloodPressureType.SYSTOLIC)!!,
        dataList.first().getValue(DataType.BloodPressureType.DIASTOLIC)!!,
        dataList.first().getValue(DataType.BloodPressureType.PULSE_RATE)!!,
        dataList.first().getValue(DataType.BloodPressureType.MEDICATION_TAKEN)!!
    )
}
suspend fun getBodyComposition(store:HealthDataStore):BodyCompositionData{
    val timeFilter = getTodayTimeFilter()
    val request = DataTypes.BODY_COMPOSITION
        .readDataRequestBuilder
        //.setLocalTimeFilter(timeFilter)   //이걸 넣어버리면 오늘 내로 입력한 정보만 들어가니까
        .setOrdering(Ordering.DESC)
        .build()
    val dataList = store.readData(request).dataList
    if(dataList.isEmpty())
        return BodyCompositionData()

    val data = dataList.first()
    return BodyCompositionData(
        basalMetabolicRate = data.getValue(DataType.BodyCompositionType.BASAL_METABOLIC_RATE)?:0,
        bodyFat = data.getValue(DataType.BodyCompositionType.BODY_FAT)?:Float.NaN,
        bodyFatMass = data.getValue(DataType.BodyCompositionType.BODY_FAT_MASS)?:Float.NaN,
        bodyMassIndex = data.getValue(DataType.BodyCompositionType.BODY_MASS_INDEX)?:Float.NaN,
        fatFree = data.getValue(DataType.BodyCompositionType.FAT_FREE)?:Float.NaN,
        fatFreeMass = data.getValue(DataType.BodyCompositionType.FAT_FREE_MASS)?:Float.NaN,
        height = data.getValue(DataType.BodyCompositionType.HEIGHT)?:Float.NaN,
        muscleMass = data.getValue(DataType.BodyCompositionType.MUSCLE_MASS)?:Float.NaN,
        skeletalMuscle = data.getValue(DataType.BodyCompositionType.SKELETAL_MUSCLE)?:Float.NaN,
        skeletalMuscleMass = data.getValue(DataType.BodyCompositionType.SKELETAL_MUSCLE_MASS)?:Float.NaN,
        totalBodyWater = data.getValue(DataType.BodyCompositionType.TOTAL_BODY_WATER)?:Float.NaN,
        weight = data.getValue(DataType.BodyCompositionType.WEIGHT)?:Float.NaN
    )
}
suspend fun getExercise(store:HealthDataStore):ExerciseData{
    val timeFilter = getTodayTimeFilter()
    val request = DataTypes.EXERCISE
        .readDataRequestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.DESC)
        .build()
    val tCalReq = DataType.ExerciseType.TOTAL_CALORIES
        .requestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.DESC)
        .build()
    val tDurReq = DataType.ExerciseType.TOTAL_DURATION
        .requestBuilder
        .setLocalDateFilter(LocalDateFilter.since(LocalDate.now()))
        .setOrdering(Ordering.DESC)
        .build()

    val eDataList = store.readData(request).dataList
    if(eDataList.isEmpty())
        return ExerciseData()

    val eTitle = store.readData(request).dataList.first().getValue(DataType.ExerciseType.CUSTOM_TITLE)!!
    val totalCalories = store.aggregateData(tCalReq).dataList.first().value!!
    val totalDuration = store.aggregateData(tDurReq).dataList.first().value!!

    return ExerciseData(
        title = eTitle,
        totalCalories = totalCalories,
        totalDuration = totalDuration
    )
}
suspend fun getSteps(store:HealthDataStore){
    val timeFilter = LocalTimeFilter.of(
        LocalDateTime.of(2025,1,15,17,50),
        LocalDateTime.of(2025,1,15,17,51)
    )

    val request = DataType.StepsType.TOTAL
        .requestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.DESC)
        .build()

    val sDataList = store.aggregateData(request).dataList
    //Log.d("SHEALTH_TEST", "step DataList Length = ${sDataList.size}, val=${sDataList.first().value!!}")

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
    var activitySummary:ActivitySummaryData by remember {
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
    var exerciseData by remember { mutableStateOf(ExerciseData())}

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
                    activitySummary = getActivitySummaryData(store)
                    activeCaloriesBurnedGoal = getActiveCaloriesBurnedGoalData(store)
                    activeTimeGoal = getActiveTimeGoalData(store)
                    bloodGlucose = getBloodGlucoseData(store)
                    bloodOxygen = getBloodOxygenData(store)
                    bloodPressure = getBloodPressureData(store)
                    bodyCompositionData = getBodyComposition(store)
                    exerciseData = getExercise(store)
                    getSteps(store)
                }
            }){
                Text(text = "Refresh")
            }
        }
        Text(
            text = "Now : $nowString"
        )

        ActivitySummaryWidget(activitySummary, modifier=Modifier.fillMaxWidth())
        SummaryWidget("Active Calories Burned Goal", modifier=Modifier.fillMaxWidth()){
            Text(text = "$activeCaloriesBurnedGoal (kcal)")
        }
        BodyCompositionWidget(bodyCompositionData, modifier=Modifier.fillMaxWidth())
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
@Composable
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
}