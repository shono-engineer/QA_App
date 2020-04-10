package jp.techacademy.shono.iso.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class QuestionDetailListAdapter(context: Context, private val mQustion: Question) : BaseAdapter() {
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null

    init {
        mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + mQustion.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return mQustion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView =
                    mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }
            val body = mQustion.body
            val name = mQustion.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name

            val bytes = mQustion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }

            // ログイン済みのユーザーを取得する
            val favbtn = convertView.findViewById<Button>(R.id.favoriteButton) as Button
            if (mQustion.isLogin) {
                val user = FirebaseAuth.getInstance().currentUser
                val dataBaseReference = FirebaseDatabase.getInstance().reference
                favbtn.setOnClickListener {
                    if (!mQustion.isFavorite) {
                        val data = mutableMapOf<String, String>()
                        data["questionUid"] = mQustion.questionUid
                        data["genre"] = mQustion.genre.toString()
                        // お気に入り質問特定用にジャンルを保存
                        dataBaseReference.child(UsersPATH).child(user!!.uid).child("favorite")
                            .push().setValue(data)
                    } else {
                        dataBaseReference.child(UsersPATH).child(user!!.uid).child("favorite")
                            .orderByChild("questionUid").equalTo(mQustion.questionUid)
                            .addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                    }

                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        Log.d("once", dataSnapshot.toString())
                                        val map = dataSnapshot.getValue() as Map<String, String>
                                        map.forEach {
                                            dataBaseReference.child(UsersPATH).child(user!!.uid)
                                                .child("favorite").child(it.key).removeValue()
                                        }
                                    }
                                }
                            )
                    }
                }
                if (mQustion.isFavorite) {
                    favbtn.text = "済"
                } else {
                    favbtn.text = "お気に入り"
                }
            } else {
                favbtn.visibility = View.GONE
            }


        } else {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQustion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name
        }

        return convertView
    }
}