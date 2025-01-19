package com.example.shealthtest

import android.health.connect.datatypes.BodyFatRecord
import java.time.Duration
import java.time.Instant
import java.time.LocalTime

// 0. Default Classes
interface SHealthDataSavable{

}
open class TemporalRecord(
    val startTime:Instant,
    val endTime:Instant
)
open class HealthTemporalRecord<T>(
    val min:T,
    val max:T,
    val value:T,
    startTime:Instant,
    endTime:Instant
):TemporalRecord(startTime, endTime){}
open class HealthPointRecord<T>(
    val value:T,
    val timestamp:Instant
)
open class HealthSeriesData<T>(
    val seriesData: List<T>
)

//1. Physiological Data
//1-1. Basic Types
class HeartRate(min: Float, max: Float, heartRate: Float, startTime: Instant, endTime: Instant) :
    HealthTemporalRecord<Float>(min, max, heartRate, startTime, endTime), SHealthDataSavable{
    val heartRate:Float get() = this.value

    companion object{
        val UNIT:String = "bpm"
    }
}
class BloodOxygen(
    min: Float,
    max: Float,
    oxygenSaturation: Float,
    startTime: Instant,
    endTime: Instant
) : HealthTemporalRecord<Float>(min, max, oxygenSaturation, startTime, endTime), SHealthDataSavable{
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
) : HealthTemporalRecord<Float>(min, max, skinTemperature, startTime, endTime), SHealthDataSavable{
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
):SHealthDataSavable{

    companion object{
        val PRESSURE_UNIT:String = "mmHg"
        val RATE_UNIT:String = "bpm"    //Need to check with dataset.
    }
}
class BloodGlucose(glucose: Float, timestamp: Instant) :
    HealthPointRecord<Float>(glucose, timestamp), SHealthDataSavable{
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
):HealthSeriesData<HeartRate>(seriesData){}

class BloodOxygenSeriesData(
    seriesData:List<BloodOxygen>
):HealthSeriesData<BloodOxygen>(seriesData){}

class SkinTemperatureSeriesData(
    seriesData:List<SkinTemperature>
):HealthSeriesData<SkinTemperature>(seriesData){}

class BloodGlucoseSeriesData(
    val mealStatus : String,
    val mealTime : Instant,
    val measurement : String,
    val sampleSource : String,
    val insulinInjected : Float = Float.NaN,
    val glucoseLevel : Float = Float.NaN,
    val isValueOverride:Boolean = false,
    seriesData: List<BloodGlucose>
):HealthSeriesData<BloodGlucose>(seriesData){}


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
}

//3. Step
data class Step(
    val startTime:Instant,
    val steps:Int
)

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
){}
class ActiveCaloriesBurnedGoal(
    goalSetDate:Instant,
    value:Int,
    isDefaultGoal:Boolean = false,
):HealthGoal<Int>(
    goalSetDate, value,
    isDefaultGoal, "ActiveCaloriesBurnedGoal"
){}
class StepsGoal(
    goalSetDate:Instant,
    value:Int,
    isDefaultGoal: Boolean = false,
):HealthGoal<Int>(
    goalSetDate, value,
    isDefaultGoal, "StepsGoal"
){}
class WaterIntakeGoal(
    goalSetDate:Instant,
    value:Float,
    isDefaultGoal: Boolean = false
):HealthGoal<Float>(
    goalSetDate, value,
    isDefaultGoal, "WaterIntakeGoal"
){}

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
){}