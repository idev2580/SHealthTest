package com.example.shealthtest

import android.content.Context
import android.util.Log
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.data.HealthDataPoint
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalDateFilter
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import com.samsung.android.sdk.health.data.request.ReadDataRequest
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

suspend fun checkAndRequestPermissions(context: Context, activity:MainActivity, permSet:Set<Permission>){
    val store: HealthDataStore = HealthDataService.getStore(context)
    val isAllAllowed:Boolean = store.getGrantedPermissions(permSet).containsAll(permSet)

    if(!isAllAllowed){
        store.requestPermissions(permSet, activity)
    }
}
fun getTodayTimeFilter(): LocalTimeFilter {
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
suspend fun getActivitySummaryData(store: HealthDataStore):ActivitySummaryData{
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
suspend fun getActiveCaloriesBurnedGoalData(store: HealthDataStore):Int{
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
suspend fun getActiveTimeGoalData(store: HealthDataStore): Duration {
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
suspend fun getBloodGlucoseData(store: HealthDataStore):BloodGlucoseData{
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
    val mealTime: Instant = dataList.first().getValue(DataType.BloodGlucoseType.MEAL_TIME)!!
    val inInsulin:Float = dataList.first().getValue(DataType.BloodGlucoseType.INSULIN_INJECTED)!!
    //val mealStatus: DataType.BloodGlucoseType.MealStatus = dataList.first().getValue(DataType.BloodGlucoseType.MEAL_STATUS)!!

    return BloodGlucoseData(glucoseLvl, mealTime, inInsulin)
}
suspend fun getBloodOxygenData(store: HealthDataStore):BloodOxygenData{
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
suspend fun getBloodPressureData(store: HealthDataStore):BloodPressureData{
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
suspend fun getBodyComposition(store: HealthDataStore):BodyCompositionData{
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
suspend fun getExercise(store: HealthDataStore):ExerciseData{
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
suspend fun getSteps(store: HealthDataStore){
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

// Data Load Functions
suspend fun HeartRateSeriesData.Companion.loadFromStore(
    store:HealthDataStore,
    startTime:LocalDateTime,
    endTime : LocalDateTime
):List<HeartRateSeriesData>{
    val timeFilter = LocalTimeFilter.of(
        startTime, endTime
    )
    val request = DataTypes.HEART_RATE
        .readDataRequestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.ASC)
        .build()
    val dataList = store.readData(request).dataList
    val seriesDataList:MutableList<HeartRateSeriesData> = mutableListOf()
    dataList.forEach {
        val seriesPoint = it.getValue(DataType.HeartRateType.SERIES_DATA)
        val seriesData:MutableList<com.example.shealthtest.HeartRate> = mutableListOf()
        seriesPoint?.forEach {
            val hr: HeartRate = com.example.shealthtest.HeartRate(
                min = it.min ?: 0.0f,
                max = it.max ?: 0.0f,
                heartRate = it.heartRate ?: 0.0f,
                startTime = it.startTime,
                endTime = it.endTime
            )
            seriesData.add(hr)
        }
        val hrs:HeartRateSeriesData = HeartRateSeriesData(
            seriesData
        )
        seriesDataList.add(hrs)
    }
    return seriesDataList
}

suspend fun BloodOxygenSeriesData.Companion.loadFromStore(
    store: HealthDataStore,
    startTime: LocalDateTime,
    endTime: LocalDateTime
):List<com.example.shealthtest.BloodOxygenSeriesData>{
    val timeFilter = LocalTimeFilter.of(
        startTime, endTime
    )
    val request = DataTypes.BLOOD_OXYGEN
        .readDataRequestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.ASC)
        .build()
    val dataList = store.readData(request).dataList
    val seriesDataList:MutableList<BloodOxygenSeriesData> = mutableListOf()
    dataList.forEach {
        val seriesPoint = it.getValue(DataType.BloodOxygenType.SERIES_DATA)
        val seriesData:MutableList<com.example.shealthtest.BloodOxygen> = mutableListOf()
        seriesPoint?.forEach {
            val bo: BloodOxygen = com.example.shealthtest.BloodOxygen(
                min = it.min ?: 0.0f,
                max = it.max ?: 0.0f,
                oxygenSaturation = it.oxygenSaturation ?: 0.0f,
                startTime = it.startTime,
                endTime = it.endTime
            )
            seriesData.add(bo)
        }
        val bos:BloodOxygenSeriesData = BloodOxygenSeriesData(
            seriesData
        )
        seriesDataList.add(bos)
    }
    return seriesDataList
}
suspend fun SkinTemperatureSeriesData.Companion.loadFromStore(
    store: HealthDataStore,
    startTime: LocalDateTime,
    endTime: LocalDateTime
):List<com.example.shealthtest.SkinTemperatureSeriesData>{
    val timeFilter = LocalTimeFilter.of(
        startTime, endTime
    )
    val request = DataTypes.SKIN_TEMPERATURE
        .readDataRequestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.ASC)
        .build()
    val dataList = store.readData(request).dataList
    val seriesDataList:MutableList<SkinTemperatureSeriesData> = mutableListOf()
    dataList.forEach {
        val seriesPoint = it.getValue(DataType.SkinTemperatureType.SERIES_DATA)
        val seriesData:MutableList<com.example.shealthtest.SkinTemperature> = mutableListOf()
        seriesPoint?.forEach {
            val st: SkinTemperature = com.example.shealthtest.SkinTemperature(
                min = it.min ?: 0.0f,
                max = it.max ?: 0.0f,
                skinTemperature = it.skinTemperature ?: 0.0f,
                startTime = it.startTime,
                endTime = it.endTime
            )
            seriesData.add(st)
        }
        val sts:SkinTemperatureSeriesData = SkinTemperatureSeriesData(
            seriesData
        )
        seriesDataList.add(sts)
    }
    return seriesDataList
}
suspend fun BloodGlucoseSeriesData.Companion.loadFromStore(
    store: HealthDataStore,
    startTime: LocalDateTime,
    endTime: LocalDateTime
):List<com.example.shealthtest.BloodGlucoseSeriesData>{
    val timeFilter = LocalTimeFilter.of(
        startTime, endTime
    )
    val request = DataTypes.SKIN_TEMPERATURE
        .readDataRequestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.ASC)
        .build()
    val dataList = store.readData(request).dataList
    val seriesDataList:MutableList<com.example.shealthtest.BloodGlucoseSeriesData> = mutableListOf()
    dataList.forEach {
        val seriesPoint = it.getValue(DataType.BloodGlucoseType.SERIES_DATA)
        val seriesData:MutableList<com.example.shealthtest.BloodGlucose> = mutableListOf()
        seriesPoint?.forEach {
            val bg: com.example.shealthtest.BloodGlucose = com.example.shealthtest.BloodGlucose(
                glucose = it.glucose,
                timestamp = it.timestamp
            )
            seriesData.add(bg)
        }
        val bgs:BloodGlucoseSeriesData = BloodGlucoseSeriesData(
            mealStatus = when(it.getValue(DataType.BloodGlucoseType.MEAL_STATUS)?:DataType.BloodGlucoseType.MealStatus.UNDEFINED){
                DataType.BloodGlucoseType.MealStatus.UNDEFINED -> "UNDEFINED"
                DataType.BloodGlucoseType.MealStatus.FASTING -> "FASTING"
                DataType.BloodGlucoseType.MealStatus.AFTER_MEAL -> "AFTER_MEAL"
                DataType.BloodGlucoseType.MealStatus.BEFORE_BREAKFAST -> "BEFORE_BREAKFAST"
                DataType.BloodGlucoseType.MealStatus.AFTER_BREAKFAST -> "AFTER_BREAKFAST"
                DataType.BloodGlucoseType.MealStatus.BEFORE_LUNCH -> "BEFORE_LUNCH"
                DataType.BloodGlucoseType.MealStatus.AFTER_LUNCH -> "AFTER_LUNCH"
                DataType.BloodGlucoseType.MealStatus.BEFORE_DINNER -> "BEFORE_DINNER"
                DataType.BloodGlucoseType.MealStatus.AFTER_DINNER -> "AFTER_DINNER"
                DataType.BloodGlucoseType.MealStatus.AFTER_SNACK -> "AFTER_SNACK"
                DataType.BloodGlucoseType.MealStatus.BEFORE_MEAL -> "BEFORE_MEAL"
                DataType.BloodGlucoseType.MealStatus.GENERAL -> "GENERAL"
                DataType.BloodGlucoseType.MealStatus.BEFORE_SLEEP -> "BEFORE_SLEEP"
                else -> "UNDEFINED"
            },
            mealTime = it.getValue(DataType.BloodGlucoseType.MEAL_TIME)?:Instant.now(),
            measurement = when(it.getValue(DataType.BloodGlucoseType.MEASUREMENT_TYPE)?:DataType.BloodGlucoseType.MeasurementType.UNDEFINED){
                DataType.BloodGlucoseType.MeasurementType.UNDEFINED -> "UNDEFINED"
                DataType.BloodGlucoseType.MeasurementType.WHOLE_BLOOD -> "WHOLE_BLOOD"
                DataType.BloodGlucoseType.MeasurementType.PLASMA -> "PLASMA"
                DataType.BloodGlucoseType.MeasurementType.SERUM -> "SERUM"
                else -> "UNDEFINED"
            },
            sampleSource = when(it.getValue(DataType.BloodGlucoseType.SAMPLE_SOURCE_TYPE)?:DataType.BloodGlucoseType.SampleSourceType.UNDEFINED){
                DataType.BloodGlucoseType.SampleSourceType.UNDEFINED -> "UNDEFINED"
                DataType.BloodGlucoseType.SampleSourceType.VENOUS -> "VENOUS"
                DataType.BloodGlucoseType.SampleSourceType.CAPILLARY -> "CAPILLARY"
                else -> "UNDEFINED"
            },
            insulinInjected = it.getValue(DataType.BloodGlucoseType.INSULIN_INJECTED)?:0.0f,
            glucoseLevel = it.getValue(DataType.BloodGlucoseType.GLUCOSE_LEVEL)?:0.0f,
            isValueOverride = false,    //TODO : CHECK ACTUAL DATA TO SET THIS PROPERLY
            seriesData
        )
        seriesDataList.add(bgs)
    }
    return seriesDataList
}

suspend fun BloodPressure.Companion.loadFromStore(
    store: HealthDataStore,
    startTime: LocalDateTime,
    endTime: LocalDateTime
):List<BloodPressure>{
    val timeFilter = LocalTimeFilter.of(
        startTime, endTime
    )
    val request = DataTypes.BLOOD_PRESSURE
        .readDataRequestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.ASC)
        .build()
    val dataList = store.readData(request).dataList
    val seriesDataList:MutableList<BloodPressure> = mutableListOf()
    dataList.forEach {
        val bp:BloodPressure = BloodPressure(
            diastolic = it.getValue(DataType.BloodPressureType.DIASTOLIC)?:0.0f,
            mean = it.getValue(DataType.BloodPressureType.MEAN)?:0.0f,
            systolic = it.getValue(DataType.BloodPressureType.SYSTOLIC)?:0.0f,
            pulseRate = it.getValue(DataType.BloodPressureType.PULSE_RATE)?:0,
            medicationTaken = it.getValue(DataType.BloodPressureType.MEDICATION_TAKEN)?:false,
            timestamp = Instant.now()   //TODO : FIX THIS
        )

    }
    return seriesDataList
}

suspend fun Sleep.Companion.loadFromStore(
    store:HealthDataStore,
    startTime : LocalDateTime,
    endTime : LocalDateTime
):List<Sleep>{
    val timeFilter = LocalTimeFilter.of(
        startTime, endTime
    )
    val request = DataTypes.SLEEP
        .readDataRequestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.ASC)
        .build()
    val dataList = store.readData(request).dataList
    val sleeps:MutableList<Sleep> = mutableListOf()

    dataList.forEach{
        val sleepScore = it.getValue(DataType.SleepType.SLEEP_SCORE)?:0
        val sessions = it.getValue(DataType.SleepType.SESSIONS)?:listOf()
        val sessionList:MutableList<SleepSession> = mutableListOf()
        if(sessions.isEmpty()){
            sleeps.add(
                Sleep(
                    sleepScore, listOf(
                    SleepSession(
                            listOf(
                                SleepStage("UNDEFINED", it.startTime, it.endTime?:it.startTime)
                            ), it.startTime, it.endTime?:it.startTime
                        )
                    )
                )
            )
        } else {
            sessions.forEach{
                val stages = it.stages?:listOf()
                val stageList:MutableList<SleepStage> = mutableListOf()
                if(stages.isEmpty()){
                    stageList.add(SleepStage("UNDEFINED", it.startTime, it.endTime))
                } else {
                    stageList.forEach{
                        stageList.add(SleepStage(it.stage,it.startTime, it.endTime))
                    }
                }
                sessionList.add(SleepSession(stageList, it.startTime, it.endTime))
            }
            sleeps.add(Sleep(sleepScore, sessionList))
        }
    }
    return sleeps
}

suspend fun Step.Companion.loadFromStore(
    store:HealthDataStore,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    unitMinute:Int = 10
):List<Step>{
    val durationMin:Int = Duration.between(startTime, endTime).toMinutes().toInt()
    val loopCnt = (durationMin / durationMin)

    val stepList:MutableList<Step> = mutableListOf()
    for (i in 1..loopCnt){
        val timeFilter = LocalTimeFilter.of(
            startTime.plusMinutes((i * unitMinute).toLong()), startTime.plusMinutes(((i+1) * unitMinute).toLong())
        )
        val request = DataType.StepsType.TOTAL
            .requestBuilder
            .setLocalTimeFilter(timeFilter)
            .setOrdering(Ordering.DESC)
            .build()
        val sDataList = store.aggregateData(request).dataList
        if(!sDataList.isEmpty()){
            stepList.add(
                Step(
                    sDataList.first().startTime,
                    sDataList.first().value?.toInt() ?: 0
                )
            )
        }
    }
    return stepList
}