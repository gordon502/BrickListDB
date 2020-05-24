package com.example.bricklistdb

class Item {
    var idInInvParts: Int
    var itemID: Int
    var name: String
    var color: String
    var colorID: Int
    var colorCode: Int
    var code: String
    var quantityInStore: Int
    var quantityInSet: Int

    constructor(idInInvParts: Int, itemID: Int, name: String, color: String, colorID: Int, colorCode: Int, code: String, quantityInStore: Int, quantityInSet: Int) {
        this.idInInvParts = idInInvParts
        this.itemID = itemID
        this.name = name
        this.color = color
        this.colorID = colorID
        this.colorCode = colorCode
        this.code = code
        this.quantityInStore = quantityInStore
        this.quantityInSet = quantityInSet
    }
}