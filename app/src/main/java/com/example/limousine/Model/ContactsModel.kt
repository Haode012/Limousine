package com.example.limousine.Model

import android.os.Message

class ContactsModel {
    var profilePicture: String = ""
    var uid: String = ""
    var fullName:String = ""
    var lastMessage: String = ""
    var UnreadCounts: Int = 0

    //empty constructor, required by firebase
    constructor()

    //parameterized constructor
    constructor(
        profilePicture: String,
        uid: String,
        fullName: String,
        lastMessage: String,
        UnreadCounts: Int
    ) {
        this.profilePicture = profilePicture
        this.uid = uid
        this.fullName = fullName
        this.lastMessage = lastMessage
        this.UnreadCounts = UnreadCounts
    }
}