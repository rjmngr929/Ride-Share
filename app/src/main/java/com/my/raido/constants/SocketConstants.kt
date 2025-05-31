package com.my.raido.constants

object SocketConstants {


    /**
     * Socket Base Url (Live & Dev)
     */
//    const val SOCKET_BASE_URL = "http://192.168.1.58:3000"

    const val SOCKET_BASE_URL = "https://raidosocket.guddukumar.com/"

    /**
     * Name Space
     */
    const val NAME_SPACE = "test"


    var JOIN_ROOM = "join-room"


    var TRANSACTION_EVENT = "transactionEvent"

//    Emit constants
    var GET_NEAR_BY_RIDERS = "getNearbyRiders"
    var CREATE_BOOKING = "createBooking"
    var RIDE_CANCEL = "rideCancel"
    var USER_UPDATE = "user_update"

//    Listen constants
    var RIDE_ACCEPT_ERROR = "riderAcceptErroResponse"
    var RIDE_ACCEPT_RESPONSE = "riderAcceptResponse"
    var USER_INFO_RIDE_COMPLETE = "userinforideComplete"
    var RIDER_RIDE_START = "riderrideStart"
    var RIDE_CANCELLED = "rideCanceled"
    var RIDER_LOCATION_CHANGE = "riderlocationchange"
    var RECEIVE_NEARBY_RIDERS = "receiveNearbyRiders"
    var RIDE_CANCEL_SUCCESS = "rideCancelSuccess"


}