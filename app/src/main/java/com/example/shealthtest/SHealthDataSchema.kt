package com.example.shealthtest

import java.time.Duration
import java.time.Instant
import java.time.LocalTime

// 0. Default Classes
/* TODO : 내부적으로 저장하는 기능을 구현할 때 사용될 인터페이스
 */
interface SHealthDataSave{
    /*
    fun saveAppend():Boolean
    */
}
/* Reduce 연산을 하기 위해 Number로 변환하는 함수가 필요하여 만든 인터페이스
 *
 * Number를 상속해서 구현해도 되지만, Number는 Abstract class로서 TemporalRecord의 상속을 포기해야 하기 때문에
 * Number 대신 별도의 인터페이스 만듦.
 * */
interface HealthReducable{
    fun toFloat():Float
    fun toDouble():Double
}
/* 기록의 시작과 끝을 저장하기 위한 클래스
 * */
open class TemporalRecord(
    val startTime:Instant,
    val endTime: Instant
)
/* 건강 데이터 중 일정 시간동안 기록하여 최소,최대,대표값이 존재하는 데이터를 나타내는 클래스
 * 예시 : HeartRate, SkinTemperature 등
 * */
open class HealthTemporalRecord<T>(
    val min:T,
    val max:T,
    val value:T,
    startTime:Instant,
    endTime:Instant
):TemporalRecord(startTime, endTime),
    Comparable<HealthTemporalRecord<T>>,
    HealthReducable
        where T:Number,T:Comparable<T>{
    override fun compareTo(other: HealthTemporalRecord<T>): Int {
        if(this.value > other.value)
            return 1
        else if(this.value == other.value) return 0
        else return -1
    }
    override fun toFloat(): Float {
        return this.value.toFloat()
    }
    override fun toDouble(): Double {
        return this.value.toDouble()
    }
}
open class HealthPointRecord<T>(
    val value:T,
    val timestamp:Instant
): Comparable<HealthPointRecord<T>>,
    HealthReducable
        where T:Number,T:Comparable<T>{
    override fun compareTo(other: HealthPointRecord<T>): Int {
        if (this.value > other.value)
            return 1
        else if(this.value == other.value) return 0
        else return 1
    }
    override fun toFloat(): Float {
        return this.value.toFloat()
    }
    override fun toDouble(): Double {
        return this.value.toDouble()
    }
}
open class HealthSeriesData<T>(
    val seriesData: List<T>
)where T:Comparable<T>, T:HealthReducable{
    val max:T get() = seriesData.max()
    val min:T get() = seriesData.min()
    val mean:Float get() = seriesData.fold(0.0f){acc:Float, it -> acc + it.toFloat()}
}

//1. Physiological Data
//1-1. Basic Types
class HeartRate(
    min: Float = 0.0f,
    max: Float = 0.0f,
    heartRate: Float = 0.0f,
    startTime: Instant = Instant.now(),
    endTime: Instant = Instant.now()
) :
    HealthTemporalRecord<Float>(min, max, heartRate, startTime, endTime), SHealthDataSave{
    val heartRate:Float get() = this.value

    companion object{
        val UNIT:String = "bpm"
    }
}
class BloodOxygen(
    min: Float = 0.0f,
    max: Float = 0.0f,
    oxygenSaturation: Float = 0.0f,
    startTime: Instant = Instant.now(),
    endTime: Instant = Instant.now()
) : HealthTemporalRecord<Float>(min, max, oxygenSaturation, startTime, endTime), SHealthDataSave{
    val oxygenSaturation:Float get() = this.value

    companion object{
        val UNIT:String = "%"
    }
}
class SkinTemperature(
    min: Float,
    max: Float,
    skinTemperature: Float,
    startTime: Instant,
    endTime: Instant
) : HealthTemporalRecord<Float>(min, max, skinTemperature, startTime, endTime), SHealthDataSave{
    val skinTemperature:Float get() = this.value

    companion object{
        val UNIT:String = "°C"
    }
}
class BloodPressure(
    val diastolic:Float,
    val mean:Float,
    val systolic:Float,
    val pulseRate:Int,
    val medicationTaken:Boolean,
    val timestamp:Instant
):SHealthDataSave{

    companion object{
        val PRESSURE_UNIT:String = "mmHg"
        val RATE_UNIT:String = "bpm"    //Need to check with dataset.
    }
}
class BloodGlucose(glucose: Float, timestamp: Instant) :
        HealthPointRecord<Float>(glucose, timestamp),
        SHealthDataSave{
    val glucose:Float get() = this.value
    companion object{
        val UNIT:String = "mmol/L"
    }
}
data class BodyComposition(
    val basalMetabolicRate:Float,
    val bodyFat:Float,
    val bodyFatMass:Float,
    val bodyMassIndex:Float,
    val fatFree:Float,
    val fatFreeMass:Float,
    val height:Float,
    val muscleMassPercent:Float,
    val skeletalMuscle:Float,
    val skeletalMuscleMass:Float,
    val totalBodyWater:Float,
    val weight:Float
){
    companion object {
        val BMR_UNIT:String = "kcal/day"
        val HEIGHT_UNIT:String = "cm"
        val WATER_UNIT:String = "L"
        val OTHER_UNIT:String = "%"
        val MASS_UNIT:String = "kg"
    }
}
//1-2. Series Data
class HeartRateSeriesData(
    seriesData:List<HeartRate>
):HealthSeriesData<HeartRate>(seriesData){
    companion object {}
}

class BloodOxygenSeriesData(
    seriesData:List<BloodOxygen>
):HealthSeriesData<BloodOxygen>(seriesData){
    companion object {}
}

class SkinTemperatureSeriesData(
    seriesData:List<SkinTemperature>
):HealthSeriesData<SkinTemperature>(seriesData){
    companion object {}
}

class BloodGlucoseSeriesData(
    val mealStatus : String,
    val mealTime : Instant,
    val measurement : String,
    val sampleSource : String,
    val insulinInjected : Float = Float.NaN,
    val glucoseLevel : Float = Float.NaN,
    val isValueOverride:Boolean = false,
    seriesData: List<BloodGlucose>
):HealthSeriesData<BloodGlucose>(seriesData){
    companion object {}
}


//2. Sleep
class SleepStage(
    val stage:String,
    startTime:Instant,
    endTime:Instant
):TemporalRecord(startTime, endTime){}
class SleepSession(
    val sessions:List<SleepStage>,
    startTime:Instant,
    endTime:Instant
):TemporalRecord(startTime, endTime){}
class Sleep(
    val sleepScore:Int,
    val sessions:List<SleepSession>
){
    companion object {}
}

//3. Step
data class Step(
    val startTime:Instant,
    val steps:Int
){
    companion object {}
}

//4. Activity
// -> should be Interfaces

//5. Goal
open class HealthGoal<T>(
    val goalSetDate:Instant,
    val value:T,
    val isDefaultGoal:Boolean,
    val goalType:String
){}

class ActiveTimeGoal(
    goalSetDate:Instant,
    value:Duration,
    isDefaultGoal:Boolean = false,
):HealthGoal<Duration>(
    goalSetDate, value,
    isDefaultGoal, "ActiveTimeGoal"
){
    companion object{

    }
}
class ActiveCaloriesBurnedGoal(
    goalSetDate:Instant,
    value:Int,
    isDefaultGoal:Boolean = false,
):HealthGoal<Int>(
    goalSetDate, value,
    isDefaultGoal, "ActiveCaloriesBurnedGoal"
){
    companion object{
        val UNIT:String = "kcal"
    }
}
class StepsGoal(
    goalSetDate:Instant,
    value:Int,
    isDefaultGoal: Boolean = false,
):HealthGoal<Int>(
    goalSetDate, value,
    isDefaultGoal, "StepsGoal"
){
    companion object{
        val UNIT:String = "steps"
    }
}
class WaterIntakeGoal(
    goalSetDate:Instant,
    value:Float,
    isDefaultGoal: Boolean = false
):HealthGoal<Float>(
    goalSetDate, value,
    isDefaultGoal, "WaterIntakeGoal"
){
    companion object{
        val UNIT:String = "mL"
    }
}

data class SleepGoalData(
    val bedTime:LocalTime = LocalTime.ofSecondOfDay(0),
    val wakeUpTime:LocalTime = LocalTime.ofSecondOfDay(0)
)
class SleepGoal(
    goalSetDate: Instant,
    value:SleepGoalData,
    isDefaultGoal: Boolean = false
):HealthGoal<SleepGoalData>(
    goalSetDate,
    value,
    isDefaultGoal,
    "SleepGoal"
){
    companion object{}
}