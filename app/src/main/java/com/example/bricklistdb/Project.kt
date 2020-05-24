package com.example.bricklistdb

class Project {
    val id: Int
    val name: String
    val active: Int
    val lastAccessed: Long

    constructor(id: Int, name: String, active: Int, lastAccessed: Long) {
        this.id = id
        this.name = name
        this.active = active
        this.lastAccessed = lastAccessed
    }
}