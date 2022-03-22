package edu.temple.audiobookplayer

class BookList {

    val list = ArrayList<Book>()

    fun add(_book : Book){
        list.add(_book)
    }

    fun remove(_book : Book){
        list.remove(_book)
    }

    fun get(x : Int) : Book{
        return list[x]
    }

    fun size() : Int{
        return list.size
    }
}