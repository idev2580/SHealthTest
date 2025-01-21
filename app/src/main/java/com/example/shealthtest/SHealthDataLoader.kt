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
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Optional
import kotlin.reflect.KClass

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

// Data Load Functions
abstract class HealthDataLoader<T>(
    val store:HealthDataStore,
    val sample:T
){
    companion object{
        fun getEnumString(enumVal:DataType.BloodGlucoseType.MealStatus):String{
            return when(enumVal){
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
            }
        }
        fun getEnumString(enumVal:DataType.BloodGlucoseType.MeasurementType):String{
            return when(enumVal){
                DataType.BloodGlucoseType.MeasurementType.UNDEFINED -> "UNDEFINED"
                DataType.BloodGlucoseType.MeasurementType.WHOLE_BLOOD -> "WHOLE_BLOOD"
                DataType.BloodGlucoseType.MeasurementType.PLASMA -> "PLASMA"
                DataType.BloodGlucoseType.MeasurementType.SERUM -> "SERUM"
                else -> "UNDEFINED"
            }
        }
        fun getEnumString(enumVal:DataType.BloodGlucoseType.SampleSourceType):String {
            return when (enumVal) {
                DataType.BloodGlucoseType.SampleSourceType.UNDEFINED -> "UNDEFINED"
                DataType.BloodGlucoseType.SampleSourceType.VENOUS -> "VENOUS"
                DataType.BloodGlucoseType.SampleSourceType.CAPILLARY -> "CAPILLARY"
                else -> "UNDEFINED"
            }
        }
    }
    abstract suspend fun loadFromStore(
        startTime:LocalDateTime,
        endTime : LocalDateTime = LocalDateTime.now()
    ):List<T>
    abstract suspend fun loadFromStore(
        startTime:LocalDateTime,
        endTime : LocalDateTime = LocalDateTime.now(),
        unitMinute : Int = 10
    ):List<T>
}
class HealthTemporalAggregateRecordLoader<T:HealthTemporalAggregateRecord<*>>(
    store:HealthDataStore,
    sample:T
):HealthDataLoader<T>(store,sample){
    override suspend fun loadFromStore(
        startTime:LocalDateTime,
        endTime : LocalDateTime,
    ):List<T>{
        val timeFilter = LocalTimeFilter.of(startTime, endTime)
        val sHealthDataType = when(sample){
            is HeartRate -> DataTypes.HEART_RATE
            is BloodOxygen -> DataTypes.BLOOD_OXYGEN
            is SkinTemperature -> DataTypes.SKIN_TEMPERATURE
            else -> throw Exception("INVALID TYPE") //TODO : Make an exception class for this work...
        }
        val request = sHealthDataType
            .readDataRequestBuilder
            .setLocalTimeFilter(timeFilter)
            .setOrdering(Ordering.ASC)
            .build()
        val dataList = store.readData(request).dataList
        val readDataList:MutableList<T> = mutableListOf()
        dataList.forEach{
            when(sample){
                is HeartRate -> it.getValue(DataType.HeartRateType.SERIES_DATA)?.forEach{
                    val data = HeartRate(
                        min = it.min,
                        max = it.max,
                        heartRate = it.heartRate,
                        startTime = it.startTime,
                        endTime = it.endTime
                    )
                    readDataList.add(data as T)
                }
                is BloodOxygen -> it.getValue(DataType.BloodOxygenType.SERIES_DATA)?.forEach{
                    val data = BloodOxygen(
                        min = it.min,
                        max = it.max,
                        oxygenSaturation = it.oxygenSaturation,
                        startTime = it.startTime,
                        endTime = it.endTime
                    )
                    readDataList.add(data as T)
                }
                is SkinTemperature -> it.getValue(DataType.SkinTemperatureType.SERIES_DATA)?.forEach{
                    val data = SkinTemperature(
                        min = it.min,
                        max = it.max,
                        skinTemperature = it.skinTemperature,
                        startTime = it.startTime,
                        endTime = it.endTime
                    )
                    readDataList.add(data as T)
                }
                else -> throw Exception("INVALID TYPE")//TODO : Make an exception class for this work...
            }
        }
        return readDataList
    }
    override suspend fun loadFromStore(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        unitMinute: Int
    ): List<T> {
        //For temporalAggregateRecords, unitMinute won't work.
        return this.loadFromStore(startTime, endTime)
    }
}
class HealthTemporalRecordLoader<T:HealthTemporalRecord<*>>(
    store:HealthDataStore,
    sample:T    //How to remove this?
):HealthDataLoader<T>(store,sample){
    override suspend fun loadFromStore(startTime: LocalDateTime, endTime: LocalDateTime): List<T> {
        TODO("Not yet implemented")
    }

    override suspend fun loadFromStore(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        unitMinute: Int
    ): List<T> {
        TODO("Not yet implemented")
    }
}
/*
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
}*/
suspend fun SkinTemperatureSeriesDataloadFromStore(
    store: HealthDataStore,
    startTime: LocalDateTime,
    endTime: LocalDateTime
){
    val timeFilter = LocalTimeFilter.of(
        startTime, endTime
    )
    val request = DataTypes.SKIN_TEMPERATURE
        .readDataRequestBuilder
        .setLocalTimeFilter(timeFilter)
        .setOrdering(Ordering.ASC)
        .build()
    val dataList = store.readData(request).dataList
    //val seriesDataList:MutableList<SkinTemperatureSeriesData> = mutableListOf()
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
        //val sts:SkinTemperatureSeriesData = SkinTemperatureSeriesData(
        //    seriesData
        //)
        //seriesDataList.add(sts)
    }
    //return seriesDataList
}
/*
suspend fun BloodGlucoseSeriesData.Companion.loadFromStore(
    store: HealthDataStore,
    startTime: LocalDateTime,
    endTime: LocalDateTime
):List<com.example.shealthtest.BloodGlucoseSeriesData>{
    val timeFilter = LocalTimeFilter.of(
        startTime, endTime
    )
    val request = DataTypes.BLOOD_GLUCOSE
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

//Goal Type들은
// 1. 단순한 load 대신 update를,
// 2. 리스트 대신 Optional을 리턴하도록 한다.
//이는 목표가 바뀌었는지를 추적하기 위함이다.
suspend fun ActiveTimeGoal.Companion.updateFromStore(
    store:HealthDataStore,
    originalGoal:ActiveTimeGoal? = null
): Optional<ActiveTimeGoal> {
    val startDate:LocalDate =
        if(originalGoal != null)
            ZonedDateTime.ofInstant(originalGoal.goalSetDate, ZoneId.systemDefault()).toLocalDate()
        else
            LocalDate.now().minusDays(1)

    val request = DataType.ActiveTimeGoalType
        .LAST.requestBuilder
        .setLocalDateFilter(LocalDateFilter.since(startDate))
        .setOrdering(Ordering.DESC)
        .build()

    val dataList = store.aggregateData(request).dataList
    if(dataList.isEmpty()){
        return Optional.empty()
    }

    val isCertainlyDefault = dataList.first().value == null
    val defaultValue = Duration.ofMillis(0) //TODO : Have to find default value for this.
    val goalValue = dataList.first().value?: Duration.ofMillis(0)
    val goalSetTime = dataList.first().startTime
    val isDefault = isCertainlyDefault || goalValue == defaultValue

    if(originalGoal != null){
        if(originalGoal.goalSetDate >= goalSetTime){
            return Optional.empty()
        }
    }
    return Optional.of(
        ActiveTimeGoal(
            goalSetTime,
            goalValue,
            isDefault
        )
    )
}
suspend fun ActiveCaloriesBurnedGoal.Companion.updateFromStore(
    store:HealthDataStore,
    originalGoal:ActiveCaloriesBurnedGoal?,
    startDate:LocalDate = LocalDate.now().minusDays(1),
    endDate:LocalDate = LocalDate.now()
): Optional<ActiveCaloriesBurnedGoal> {

}
suspend fun StepsGoal.Companion.updateFromStore(
    store:HealthDataStore,
    originalGoal:StepsGoal?,
    startDate:LocalDate = LocalDate.now().minusDays(1),
    endDate:LocalDate = LocalDate.now()
):Optional<StepsGoal>{

}
suspend fun WaterIntakeGoal.Companion.updateFromStore(
    store:HealthDataStore,
    originalGoal:WaterIntakeGoal?,
    startDate:LocalDate = LocalDate.now().minusDays(1),
    endDate:LocalDate = LocalDate.now()
):Optional<WaterIntakeGoal>{

}
suspend fun SleepGoal.Companion.updateFromStore(
    store:HealthDataStore,
    originalGoal:SleepGoal?,
    startDate:LocalDate = LocalDate.now().minusDays(1),
    endDate:LocalDate = LocalDate.now()
):Optional<SleepGoal>{

}*/