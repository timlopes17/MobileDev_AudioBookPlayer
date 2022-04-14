package edu.temple.audiobookplayer

import android.app.SearchManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.temple.audiobookplayer.Book
import edu.temple.audiobookplayer.BookList
import edu.temple.audlibplayer.PlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URL

class MainActivity : AppCompatActivity(), BookListFragment.SelectionFragmentInterface, ControlFragment.ControlFragmentInterface {

    lateinit var bookListVM: BookListViewModel

    lateinit var audioBinder : PlayerService.MediaControlBinder

    var isConnected = false

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

        bindService(Intent(this, PlayerService::class.java)
            , serviceConnection
            , BIND_AUTO_CREATE)
    }

    override fun bookSelected() {
        if (findViewById<View>(R.id.container2) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container1, BookDetailsFragment())
                .addToBackStack(null)
                .commit()
        }

        audioBinder.stop()
    }

    suspend fun searchBooks(search: String) {

        var thisSearch = search
        var jsonArray: JSONArray
        var jsonObject: JSONObject
        var jsonObjectId: JSONObject
        var tempTitle: String
        var tempAuthor: String
        var tempId: Int
        var tempImg: String
        var tempLength : Int
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

            withContext(Dispatchers.IO) {
                jsonObjectId = JSONObject(
                    URL("https://kamorris.com/lab/cis3515/book.php?id=$tempId")
                        .openStream()
                        .bufferedReader()
                        .readLine()
                )
            }
            Log.d("TEST", jsonObjectId.toString())
            tempLength = jsonObjectId.getInt("duration")

            tempBook = Book(tempTitle, tempAuthor, tempId, tempImg, tempLength)
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

    override fun playBook(bookId : Int, progress : Int) {
        if(isConnected){
            if(progress < 1)
                audioBinder.play(bookId)
            else{
                Log.d("Service", "${bookUri.toString()}")
                audioBinder.play(File(bookUri?.path), progress)
            }
        }
        else
            Log.d("Service", "NOT CONNECTED")
    }

    override fun stopBook() {
        if(isConnected) {
            audioBinder.stop()
        }
    }

    override fun pauseBook() {
        if(isConnected) {
            audioBinder.pause()
        }
    }

    override fun seekBook(position: Int) {
        if(isConnected) {
            audioBinder.seekTo(position)
        }
    }

    private var bookProgress : PlayerService.BookProgress? = null
    private var bookUri : Uri? = null
    var gotProgress = false

    val progressHandler = Handler(Looper.getMainLooper()){
        bookProgress = it.obj as? PlayerService.BookProgress
        Log.d("Service", "Progress: ${bookProgress?.progress}")
        Log.d("Service", "URI: ${bookProgress?.bookUri}")
        Log.d("Service", "Id: ${bookProgress?.bookId}")
        bookUri = bookProgress?.bookUri
        Log.d("Service", "bookUri: ${bookUri.toString()}")
        true
    }

    val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("Service", "CONNECTED")
            isConnected = true
            audioBinder = service as PlayerService.MediaControlBinder
            audioBinder.setProgressHandler(progressHandler)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isConnected = false
            Log.d("Service", "DISCONNECTED")
        }
    }
}