package edu.temple.audiobookplayer

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import edu.temple.audiobookplayer.Book
import edu.temple.audiobookplayer.BookList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MainActivity : AppCompatActivity(), BookListFragment.SelectionFragmentInterface {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bookList = BookList()
        val searchButton = findViewById<Button>(R.id.searchButton)

        lateinit var bookViewModel: BookViewModel
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)

        searchButton.setOnClickListener {
            onSearchRequested()
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
            .replace(R.id.container1,  BookListFragment.newInstance(bookList))
            .commit()

        if(bookViewModel.getSelectedBook().value != null && findViewById<View>(R.id.container2) == null){
            bookSelected()
        }
    }

    override fun bookSelected() {
        if (findViewById<View>(R.id.container2) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container1, BookDetailsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    suspend fun searchBooks(search : String){
        Log.d("Server Response", "Fuck me")
        withContext(Dispatchers.IO) {
            val serverResponse = URL("https://kamorris.com/lab/cis3515/search.php")
                .openStream()
                .bufferedReader()
                .readLine()

            Log.d("Server Response", serverResponse)
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent!!.action) {
            intent!!.getStringExtra(SearchManager.QUERY)?.also { query ->
                CoroutineScope(Dispatchers.Main).launch() {
                    searchBooks(query)
                }
            }
        }
    }
}