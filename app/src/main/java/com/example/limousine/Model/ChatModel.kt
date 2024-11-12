package com.example.limousine.Model

class ChatModel {
    var id: String = ""
    var message: String = ""
    var image: String = ""
    var video: String = ""
    var recording: String = ""
    var receiverId:String = ""
    var senderId:String = ""
    var currentDate: String = ""
    var currentTime: String = ""
    var unread: String = ""

    //empty constructor, required by firebase
    constructor()

    //parameterized constructor
    constructor(
        id: String,
        message: String,
        video: String,
        image: String,
        recording: String,
        receiverId: String,
        senderId: String,
        currentDate: String,
        currentTime: String,
        unread: String
    ) {
        this.id = id
        this.message = message
        this.video = video
        this.image = image
        this.recording = recording
        this.receiverId = receiverId
        this.senderId = senderId
        this.currentDate = currentDate
        this.currentTime = currentTime
        this.unread = unread
    }
}