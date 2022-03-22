package edu.temple.audiobookplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import edu.temple.audiobookplayer.Book
import edu.temple.audiobookplayer.BookList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bookList = BookList()
        val titleArray: Array<String> = resources.getStringArray(R.array.book_titles)
        val authorArray: Array<String> = resources.getStringArray(R.array.book_authors)

        for (i in 0..9){
            bookList.add(Book(titleArray[i], authorArray[i]))
            //Log.d("TITLE", bookList[i].title)
            //Log.d("AUTHOR", bookList[i].author)
        }
    }
}