package com.my.raido.models.cab

import com.google.gson.annotations.SerializedName

data class RideBookResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("status") val status: Boolean,
//    @SerializedName("nearby_riders") val nearbyRiders: NearbyRiders,
    @SerializedName("steps") val drivePath: String = "",
    @SerializedName("fare") val fareData: FareData,
    @SerializedName("Distance") val Distance: String,
    @SerializedName("bookingId") val bookingId: String,
    @SerializedName("districtfare") val districtFareData: ArrayList<DistrictFareData>
)

//data class NearbyRiders(
//    @SerializedName("Cabs") val nearbyCabs: riderData,
//    @SerializedName("Bikes") val nearbyBikes: riderData,
//    @SerializedName("Autos") val nearbyAutos: riderData,
//    @SerializedName("Bike Light") val nearbyBikeLite: riderData,
//)

data class riderData(
    @SerializedName("riders") val riders: ArrayList<NearByRiderModel>,
    @SerializedName("average_duration") val averageDuration: String,

)

data class FareData(
    @SerializedName("Cabs") val cabDetail: FareCharges,
    @SerializedName("Bike") val bikeDetail: FareCharges,
    @SerializedName("Auto") val autoDetail: FareCharges,
    @SerializedName("Bike_Lite") val bikeLiteDetail: FareCharges
    )

data class FareCharges(
    @SerializedName("fare") val fare: Double,
    @SerializedName("final_amount") val finalFare: Double,
    @SerializedName("duration_in_minutes") val durationTime: String,
    @SerializedName("expected_arrival_time") val arrivalTime: String,
)

data class RiderFares(
    @SerializedName("Cabfare") val cabFare: Double = 0.0,
    @SerializedName("Cabfare_final_amount") val cabFareFinal: Double = 0.0,
    @SerializedName("Cabs_Expected_Drop_Time") val cabArrivalTime: String = "",
    @SerializedName("Cabs_Duration_in_Minutes") val cabDurationMin: String = "",

    @SerializedName("Autofare") val autoFare: Double = 0.0,
    @SerializedName("Autofare_final_amount") val autoFareFinal: Double = 0.0,
    @SerializedName("Auto_Expected_Drop_Time") val autoArrivalTime: String = "",
    @SerializedName("Auto_Duration_in_Minutes") val autoDurationMin: String = "",

    @SerializedName("Bikefare") val bikeFare: Double = 0.0,
    @SerializedName("Bikefare_final_amount") val bikeFareFinal: Double = 0.0,
    @SerializedName("Bike_Expected_Drop_Time") val bikeArrivalTime: String = "",
    @SerializedName("Bike_Duration_in_Minutes") val bikeDurationMin: String = "",

    @SerializedName("Bike_Litefare") val bikeLiteFare: Double = 0.0,
    @SerializedName("Bike_Litefare_final_amount") val bikeLiteFareFinal: Double = 0.0,
    @SerializedName("Bike_Lite_Expected_Drop_Time") val bikeLiteArrivalTime: String = "",
    @SerializedName("Bike_Lite_Duration_in_Minutes") val bikeLiteDurationMin: String = "",
)

data class DistrictFareData(
    @SerializedName("service_category_id") val serviceCategoryId: Int,
    @SerializedName("base_fare") val baseFare: Int,
    @SerializedName("distance_fare_min_1") val distanceFareMinFirst: Int,
    @SerializedName("distance_fare_max_1") val distanceFareMaxFirst: Int,
    @SerializedName("distance_fare_rate_1") val distanceFareRateFirst: Any,
    @SerializedName("distance_fare_min_2") val distanceFareMinSecond: Int,
    @SerializedName("distance_fare_max_2") val distanceFareMaxSecond: Int,
    @SerializedName("distance_fare_rate_2") val distanceFareRateSecond: Any,
    @SerializedName("night_fare") val nightFare: Int,
    @SerializedName("wait_time_max") val waitTimeMax: Int,
    @SerializedName("wait_time_charge") val waitTimeCharge: Int,
)


