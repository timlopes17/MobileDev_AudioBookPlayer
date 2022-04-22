package edu.temple.audiobookplayer

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData

class BookListViewModel : ViewModel() {

    private var x : Int = 0
    private val vmBookList : BookList = BookList();

    val change : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun increment(){
        change.value = x + 1
        x++
    }

    fun getIncrement() : LiveData<Int> {
        return change
    }

    fun setBookList(bookList : BookList) {
        vmBookList.clear()

        for(i in 0 until bookList.size()){
            vmBookList.add(bookList[i])
        }
    }

    fun getBookList() : BookList {
        return vmBookList
    }

    fun getBook(id : Int) : Book?{
        for(i in 0 until vmBookList.size()){
            if(vmBookList[i].id == id)
                return vmBookList[i]
        }
        Log.d("BLVM", "getBook failed")
        return null
    }

}