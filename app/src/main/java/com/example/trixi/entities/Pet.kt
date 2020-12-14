package com.example.trixi.entities

import java.io.Serializable

data class Pet (
    val uid: String,
    val posts: ArrayList<Post>,
    val ownerId: String,
    val name: String,
    val imageUrl: String?,
    val age: Int?,
    val bio: String?,
    val breed: String?,
    val followers: ArrayList<User>?,
    val gender: String?
) : Serializable