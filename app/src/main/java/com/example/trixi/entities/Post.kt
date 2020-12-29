package com.example.trixi.entities;


data class Post(
    val uid: String?,
    val title: String?,
    val description: String?,
    val filePath: String?,
    val ownerId: String?,
    var comments: List<Comment>?,
    var likes: List<Like>?,
    var owner: User?,
) {
}

data class FollowingsPosts(
    val isSuccessfull: Boolean,
    val followingsPost: List<Post>
) {

}


