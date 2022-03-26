package edu.temple.audiobookplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import edu.temple.audiobookplayer.Book
import edu.temple.audiobookplayer.BookList

class MainActivity : AppCompatActivity(), BookListFragment.SelectionFragmentInterface {
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

        var fragment = supportFragmentManager.findFragmentById(R.id.container1)
        if(fragment != null){
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }

        if(supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStack()
        else
            supportFragmentManager
            .beginTransaction()
            .add(R.id.container1,  BookListFragment.newInstance(bookList))
            .commit()
        }

    override fun bookSelected() {
        if (findViewById<View>(R.id.container2) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container1, BookDetailsFragment(), "BackStack")
                .addToBackStack(null)
                .commit()
        }
    }
}