package com.example.bricklistdb

class AppSettings {

    var prefixURL: String = "http://fcds.cs.put.poznan.pl"
    var showArchive: Boolean = false

    constructor(prefixURL: String, showArchive: Boolean) {
        this.prefixURL = prefixURL
        this.showArchive = showArchive
    }

}