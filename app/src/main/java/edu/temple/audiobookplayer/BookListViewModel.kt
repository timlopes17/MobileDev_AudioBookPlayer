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

    fun updateBook(id : Int){
        var tempBookList : BookList = BookList()

        for(i in 0 until vmBookList.size()){
            tempBookList.add(vmBookList[i])
        }
        vmBookList.clear()
        for(i in 0 until tempBookList.size()){
            if(id == tempBookList[i].id){
                Log.d("updateBook", "Book Updated to True")
                vmBookList.add(Book(tempBookList[i].title, tempBookList[i].author, tempBookList[i].id, tempBookList[i].coverURL, tempBookList[i].duration, true))
            }
            else{
                vmBookList.add(tempBookList[i])
            }
        }
    }

    fun toString(id : Int): String {
        var myString : String = "^"
        Log.d("toString", "${vmBookList.size()}")
        for(i in 0 until vmBookList.size()){
            if(i == vmBookList[i].id){
                myString = "size: ${vmBookList.size()} downloaded:${vmBookList[i].downloaded}"
            }
        }
        return myString
    }
}