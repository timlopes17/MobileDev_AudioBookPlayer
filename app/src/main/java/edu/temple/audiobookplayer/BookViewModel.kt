package edu.temple.audiobookplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BookViewModel : ViewModel() {
    val selectedBook : MutableLiveData<Book> by lazy {
        MutableLiveData<Book>()
    }

    val playingBook : MutableLiveData<Book> by lazy {
        MutableLiveData<Book>()
    }


    fun setSelectedBook(book : Book) {
        selectedBook.value = book
    }

    fun getSelectedBook() : LiveData<Book> {
        return selectedBook
    }

    fun setPlayingBook(book : Book) {
        playingBook.value = book
    }

    fun getPlayingBook() : LiveData<Book> {
        return playingBook
    }
}
