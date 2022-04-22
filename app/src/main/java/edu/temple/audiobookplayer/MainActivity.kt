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
import java.io.FileOutputStream
import java.net.URL

class MainActivity : AppCompatActivity(), BookListFragment.SelectionFragmentInterface, ControlFragment.ControlFragmentInterface {

    lateinit var bookListVM: BookListViewModel

    lateinit var audioBinder : PlayerService.MediaControlBinder

    lateinit var controlFrag : ControlFragment

    lateinit var bookViewModel: BookViewModel

    var isConnected = false
    var once = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.searchButton)

        bookListVM = ViewModelProvider(this).get(BookListViewModel::class.java)
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)

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

        controlFrag = supportFragmentManager.findFragmentById(R.id.controlContainer) as ControlFragment
    }

    suspend fun updateControlFragment(bookId : Int){
        Log.d("NewTest", "updateControlFragment")
        val tempBook : Book
        withContext(Dispatchers.IO) {
            val jsonObject = JSONObject(
                URL("https://kamorris.com/lab/cis3515/book.php?id=$bookId")
                    .openStream()
                    .bufferedReader()
                    .readLine()
            )
            tempBook = Book(jsonObject.getString("title"), jsonObject.getString("author"),
                jsonObject.getInt("id"), jsonObject.getString("cover_url"), jsonObject.getInt("duration"), false)
        }
        bookViewModel.setSelectedBook(tempBook)
        once = false
    }

    override fun bookSelected() {
        if (findViewById<View>(R.id.container2) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container1, BookDetailsFragment())
                .addToBackStack(null)
                .commit()
        }
        if(this::audioBinder.isInitialized)
        {
            audioBinder.stop()
            controlFrag.getProgress(0)
        }
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
            tempLength = jsonObjectId.getInt("duration")

            val path = this.filesDir.absolutePath

            if(File("$path/$tempId.mp3").exists())
                tempBook = Book(tempTitle, tempAuthor, tempId, tempImg, tempLength, true)
            else
                tempBook = Book(tempTitle, tempAuthor, tempId, tempImg, tempLength, false)
            tempBookList.add(tempBook)
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

    override fun playBook(bookId : Int) {
        if(isConnected){
            bookListVM.getBook(bookId)?.run{
                if(this.downloaded)
                {

                }
                else
                {
                    audioBinder.play(bookId)
                    withContext(Dispatchers.IO) {
                        download()
                    }
                }
            }

        }
        else
            Log.d("Service", "NOT CONNECTED")
    }

    override fun stopBook() {
        if(isConnected) {
            audioBinder.stop()
            controlFrag.getProgress(0)
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

    val progressHandler = Handler(Looper.getMainLooper()){
        bookProgress = it.obj as? PlayerService.BookProgress
        if(bookProgress?.progress != null && this::controlFrag.isInitialized)
            controlFrag.getProgress(bookProgress!!.progress)

        if(audioBinder.isPlaying && once){
            Log.d("NewTest", "isPlaying")
            if(bookProgress?.progress != null && this@MainActivity::controlFrag.isInitialized){
                Log.d("NewTest", "controlFrag is init")
                CoroutineScope(Dispatchers.Main).launch() {
                    updateControlFragment(bookProgress!!.bookId)
                }
            }
            else
                Log.d("NewTest", "controlFrag is NOT init")
        }

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

    override fun onStop(){
        val intent = Intent(this, PlayerService::class.java)
        if (this != null) {
            this.startService(intent)
        }
        Log.d("Service", "Activity onStop")
        super.onStop()
    }

    fun download(link: String, path: String) {
        URL(link).openStream().use { input ->
            FileOutputStream(File(path)).use { output ->
                input.copyTo(output)
            }
        }
    }
}