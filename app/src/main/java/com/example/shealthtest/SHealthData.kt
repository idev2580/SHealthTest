package com.example.shealthtest

import java.time.Duration
import java.time.Instant
import java.time.LocalTime

data class ActivitySummaryData(
    val totalActiveTime:Duration,
    val totalDistance:Float = Float.NaN,
    val totalCaloriesBurned:Float = Float.NaN,
    val totalActiveCaloriesBurned:Float = Float.NaN
)

data class BloodGlucoseData(
    val glucoseLevel:Float = Float.NaN,
    val mealTime: Instant = Instant.now(),
    val insulinInjected:Float = Float.NaN
)
data class BloodOxygenData(
    val oxygenSaturation:Float = Float.NaN,
    val minOxygenSaturation:Float = Float.NaN,
    val maxOxygenSaturation:Float = Float.NaN
)
data class BloodPressureData(
    val mean:Float = Float.NaN,
    val systolic:Float = Float.NaN,
    val diastolic:Float = Float.NaN,
    val pulseRate:Int = -1,
    val medicationTaken:Boolean = false
)
data class BodyCompositionData(
    val basalMetabolicRate:Int = -1,
    val bodyFat:Float = Float.NaN,
    val bodyFatMass:Float = Float.NaN,
    val bodyMassIndex:Float = Float.NaN,
    val fatFree:Float = Float.NaN,
    val fatFreeMass:Float = Float.NaN,
    val height:Float = Float.NaN,
    val muscleMass:Float = Float.NaN,
    val skeletalMuscle:Float = Float.NaN,
    val skeletalMuscleMass:Float = Float.NaN,
    val totalBodyWater:Float = Float.NaN,
    val weight:Float = Float.NaN
)
data class ExerciseData(
    val title:String = "",
    val totalCalories:Float = Float.NaN,
    val totalDuration:Duration = Duration.ofMillis(0)
)
data class HeartRateData(
    val rate:Float = Float.NaN,
    val max:Float = Float.NaN,
    val maxRate:Float = Float.NaN,
    val min:Float = Float.NaN,
    val minRate:Float = Float.NaN
)
data class NutritionData(
    val calcium:Float = Float.NaN,
    val calories:Float = Float.NaN,
    val carbohydrate:Float = Float.NaN,
    val cholesterol:Float = Float.NaN,
    val dietaryFiber:Float = Float.NaN,
    val iron:Float = Float.NaN,
    val monoSaturatedFat:Float = Float.NaN,
    val polySaturatedFat:Float = Float.NaN,
    val potassium:Float = Float.NaN,
    val protein:Float = Float.NaN,
    val saturatedFat:Float = Float.NaN,
    val sodium:Float = Float.NaN,
    val sugar:Float = Float.NaN,
    val totalFat:Float = Float.NaN,
    val transFat:Float = Float.NaN,
    val vitaminA:Float = Float.NaN,
    val vitaminC:Float = Float.NaN
)
data class SkinTemperatureData(
    val max:Float = Float.NaN,
    val min:Float = Float.NaN,
    val value:Float = Float.NaN
)
data class SleepData(
    val duration:Duration = Duration.ofMillis(0),
    val sleepScore:Int = -1
)
data class SleepGoalData(
    val bedTime:LocalTime = LocalTime.ofSecondOfDay(0),
    val wakeUpTime:LocalTime = LocalTime.ofSecondOfDay(0)
)
data class UserProfileData(
    val birthDate:String = "",
    val height:Float = Float.NaN,
    val nickname:String = "",
    val weight:Float = Float.NaN
)