package edu.temple.audiobookplayer

import android.os.Parcel
import android.os.Parcelable

class BookList() {

    private val list : ArrayList<Book> = ArrayList<Book>()

    fun add(_book : Book){
        list.add(_book)
    }

    fun remove(_book : Book){
        list.remove(_book)
    }

    operator fun get(x : Int) : Book{
        return list[x]
    }

    fun size() : Int{
        return list.size
    }
}