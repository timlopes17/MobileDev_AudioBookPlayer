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
import java.io.*
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity(), BookListFragment.SelectionFragmentInterface, ControlFragment.ControlFragmentInterface {

    lateinit var bookListVM: BookListViewModel

    lateinit var audioBinder : PlayerService.MediaControlBinder

    lateinit var controlFrag : ControlFragment

    lateinit var bookViewModel: BookViewModel

    var hashMap : HashMap<Int, Int> = HashMap<Int, Int>()

    var searchWord : String = ""

    var isConnected = false
    var once = true
    lateinit var path : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        path = this.filesDir.absolutePath

        val searchButton = findViewById<Button>(R.id.searchButton)

        bookListVM = ViewModelProvider(this).get(BookListViewModel::class.java)
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)

        searchButton.setOnClickListener {
            onSearchRequested()
            if (supportFragmentManager.backStackEntryCount > 0)
                supportFragmentManager.popBackStack()
        }

        if(File("$path/hmFile").exists() && File("$path/search").length() > 0){
            Log.d("HashMap", path)
            Log.d("HashMap", "Receiving hmFile")
            try{
                ObjectInputStream(FileInputStream("$path/hmFile")).use { it ->
                    hashMap = it.readObject() as HashMap<Int, Int>
                    Log.d("HashMap", "$hashMap")
                }
            }
            catch(e : Exception){
                Log.d("HashMap", "Creating New HashMap")
                File("$path/hmFile").createNewFile()
            }
        }
        else{
            Log.d("HashMap", "Creating New HashMap")
            File("$path/hmFile").createNewFile()
        }

        if(File("$path/search").exists() && File("$path/search").length() > 0){
            try {
                Log.d("ERROR", "ERROR")
                ObjectInputStream(FileInputStream("$path/search")).use { it ->
                    Log.d("ERROR", "ERROR")
                    searchWord = it.readObject() as String
                    Log.d("ERROR", "ERROR")
                    if (searchWord == null)
                        Log.d("searchWord", "is null")
                    else
                        Log.d("searchWord", "$searchWord")
                }
            }
            catch (e : Exception){
                Log.d("ERROR", "ERROR")
            }
        }
        else if(!File("$path/search").exists()){
            Log.d("searchWord", "Creating File search")
            File("$path/search").createNewFile()
        }
        else{
            Log.d("searchWord", "File is empty")
        }

        var fragment = supportFragmentManager.findFragmentById(R.id.container1)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }

        if (supportFragmentManager.backStackEntryCount > 0){
            supportFragmentManager.popBackStack()
            bookListVM.increment()
        }

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

        if(searchWord.length > 0)
            CoroutineScope(Dispatchers.Main).launch() {
                searchBooks(searchWord)
            }
    }

    suspend fun updateControlFragment(bookId : Int){
        Log.d("UCF", "updateControlFragment")
        Log.d("UCF", "bookId:$bookId")
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
        bookViewModel.setPlayingBook(tempBook)
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
    }

    suspend fun searchBooks(search: String) {

        Log.d("searchBooks", "Searching...")

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

            if(File("$path/$tempId").exists())
                tempBook = Book(tempTitle, tempAuthor, tempId, tempImg, tempLength, true)
            else
                tempBook = Book(tempTitle, tempAuthor, tempId, tempImg, tempLength, false)
            tempBookList.add(tempBook)
        }

            bookListVM.setBookList(tempBookList)
            bookListVM.increment()
    }

    override fun onNewIntent(intent: Intent?) {

        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent!!.action) {
            intent!!.getStringExtra(SearchManager.QUERY)?.also { query ->
                CoroutineScope(Dispatchers.Main).launch() {
                    searchBooks(query)
                    val searchFile = "$path/search"
                    ObjectOutputStream(FileOutputStream(searchFile)).use{ it ->
                        it.writeObject(query)
                        it.close()
                    }
                }
            }
        }
    }

    override fun playBook(bookId : Int) {
//        val hmFile = File("$path/hmFile")
//        ObjectOutputStream(FileOutputStream(hmFile)).use{ it ->
//            it.writeObject(hashMap)
//            it.close()
//        }
        if(isConnected){
            Log.d("playBook", "BookId: $bookId")
            bookListVM.getBook(bookId)?.run{
                if(hashMap.containsKey(bookId))
                {
                    Log.d("playBook", "DOWNLOADED")
                    var file = File("$path/$bookId")
                    Log.d("playBook HashMap", "$hashMap")
                    audioBinder.play(file, hashMap.get(bookId)!!)
                    bookViewModel.setPlayingBook(this)
                }
                else
                {
                    Log.d("playBook", "NOT DOWNLOADED")
                    audioBinder.play(bookId)
                    bookViewModel.setPlayingBook(this)
                    hashMap[bookId] = 0
                    Log.d("playBook", "${this.id}")
                    bookListVM.updateBook(this.id)
                    Log.d("playBook", "${bookListVM.toString(this.id)}")
                    CoroutineScope(Dispatchers.Main).launch() {
                        download("https://kamorris.com/lab/audlib/download.php?id=$bookId", "$path/$bookId")
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
            hashMap.replace(bookViewModel.getPlayingBook().value!!.id, 0)
        }
    }

    override fun pauseBook() {
        if(isConnected) {
            audioBinder.pause()
            val hmFile = "$path/hmFile"
            ObjectOutputStream(FileOutputStream(hmFile)).use{ it ->
                it.writeObject(hashMap)
                it.close()
            }
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
                    if(bookProgress!!.bookId != null && bookProgress!!.bookId != -1)
                        updateControlFragment(bookProgress!!.bookId)
                    else
                        updateControlFragment(bookProgress!!.bookUri?.lastPathSegment!!.toInt())
                }
            }
            else
                Log.d("NewTest", "controlFrag is NOT init")
        }

        if(audioBinder.isPlaying && bookViewModel.getPlayingBook().value != null){
            val playingBook = bookViewModel.getPlayingBook().value
//            Log.d("Playing", "${bookViewModel.getPlayingBook().value}")
//            Log.d("Playing", "$playingBook.id")
//            Log.d("Playing", "${bookProgress!!.progress}")
            hashMap.replace(playingBook!!.id, bookProgress!!.progress)
            Log.d("HashMap", "$hashMap")
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

    private suspend fun download(link: String, path: String) {
        withContext(Dispatchers.IO) {
            URL(link).openStream().use { input ->
                FileOutputStream(File(path)).use { output ->
                    input.copyTo(output)
                    output.close()
                }
                input.close()
            }
        }
        Log.d("Download", "Finished")
        bookListVM.increment()
    }
}