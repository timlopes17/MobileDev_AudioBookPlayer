package edu.temple.audiobookplayer

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.temple.audiobookplayer.Book
import edu.temple.audiobookplayer.BookList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity(), BookListFragment.SelectionFragmentInterface {

    lateinit var bookListVM: BookListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.searchButton)
        lateinit var bookViewModel: BookViewModel

        bookListVM = ViewModelProvider(this).get(BookListViewModel::class.java)
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)

        Log.d("TEST", "Main 1")

        searchButton.setOnClickListener {
            onSearchRequested()
            if (supportFragmentManager.backStackEntryCount > 0)
                supportFragmentManager.popBackStack()
        }


        var fragment = supportFragmentManager.findFragmentById(R.id.container1)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }

        if (supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStack()
        else
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container1, BookListFragment())
                .commit()

        if (bookViewModel.getSelectedBook().value != null && findViewById<View>(R.id.container2) == null) {
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

    suspend fun searchBooks(search: String) {

        var thisSearch = search
        var jsonArray: JSONArray
        var jsonObject: JSONObject
        var tempTitle: String
        var tempAuthor: String
        var tempId: Int
        var tempImg: String
        var tempBook: Book
        var tempBookList: BookList = BookList()

        if(thisSearch.equals("*")){
            thisSearch = "";
        }

        withContext(Dispatchers.IO) {
            jsonArray = JSONArray(
                URL("https://kamorris.com/lab/cis3515/search.php?term=$thisSearch")
                    .openStream()
                    .bufferedReader()
                    .readLine()
            )
        }

        Log.d("TEST", jsonArray.toString())

        for (i in 0 until jsonArray.length()) {
            jsonObject = jsonArray.getJSONObject(i)
            tempTitle = jsonObject.getString("title")
            tempAuthor = jsonObject.getString("author")
            tempId = jsonObject.getInt("id")
            tempImg = jsonObject.getString("cover_url")
            tempBook = Book(tempTitle, tempAuthor, tempId, tempImg)
            tempBookList.add(tempBook)
            Log.d("Book", "$tempTitle $tempAuthor $tempId $tempImg")
        }

        //if(jsonArray.length() != 0){
            bookListVM.setBookList(tempBookList)
            bookListVM.increment()
        //}
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