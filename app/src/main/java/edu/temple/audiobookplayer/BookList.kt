package edu.temple.audiobookplayer

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class BookList(): Parcelable {

    private val list : ArrayList<Book> = ArrayList<Book>()

    constructor(parcel: Parcel) : this() {
        parcel.writeList(list)
    }

    fun add(_book : Book){
        list.add(_book)
    }

    fun remove(_book : Book){
        list.remove(_book)
    }

    fun clear(){
        list.removeAll(list)
    }

    operator fun get(x : Int) : Book{
        return list[x]
    }

    fun size() : Int{
        return list.size
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BookList> {
        override fun createFromParcel(parcel: Parcel): BookList {
            return BookList(parcel)
        }

        override fun newArray(size: Int): Array<BookList?> {
            return arrayOfNulls(size)
        }
    }
}