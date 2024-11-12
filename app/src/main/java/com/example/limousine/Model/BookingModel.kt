package com.example.limousine.Model

class BookingModel {
    var id: String = ""
    var pickUpLocation:String = ""
    var dropOffLocation:String = ""
    var pickUpDate:String = ""
    var pickUpTime:String = ""
    var distance:String = ""
    var typeOfVehicle: String = ""
    var pax: String = ""
    var luggage: String = ""
    var price: String = ""
    var uid: String = ""

    //empty constructor, required by firebase
    constructor()

    //parameterized constructor
    constructor(
        id: String,
        pickUpLocation: String,
        dropOffLocation: String,
        pickUpDate: String,
        pickUpTime: String,
        distance: String,
        typeOfVehicle: String,
        pax: String,
        luggage: String,
        price: String,
        uid: String
    ) {
        this.id = id
        this.pickUpLocation =  pickUpLocation
        this.dropOffLocation =  dropOffLocation
        this.pickUpDate = pickUpDate
        this.pickUpTime = pickUpTime
        this.distance = distance
        this.typeOfVehicle = typeOfVehicle
        this.pax = pax
        this.luggage = luggage
        this.price = price
        this.uid = uid
    }
}