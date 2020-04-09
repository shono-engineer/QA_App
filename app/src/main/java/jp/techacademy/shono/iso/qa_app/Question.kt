package jp.techacademy.shono.iso.qa_app

import java.io.Serializable
import java.util.ArrayList

class Question(val title: String, val body: String, val name: String, val uid: String, val questionUid: String, val genre: Int, bytes: ByteArray, val answers: ArrayList<Answer>) : Serializable {
    val imageBytes: ByteArray

    //　これらは詳細画面に遷移してから判定を取ることとする ログインくらいはMainActivityでとってもいいかも
    var isLogin: Boolean = false
    var isFavorite: Boolean = false

    init {
        imageBytes = bytes.clone()
    }
}