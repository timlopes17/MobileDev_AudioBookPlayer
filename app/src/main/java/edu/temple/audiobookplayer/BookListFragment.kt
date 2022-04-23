package edu.temple.audiobookplayer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val BOOKS_KEY = "books_key"

class BookListFragment : Fragment() {
    //private var recyclerView : RecyclerView? = null
    private lateinit var bookViewModel : BookViewModel
    private lateinit var bookListVM: BookListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookViewModel = ViewModelProvider(requireActivity()).get(BookViewModel::class.java)
        bookListVM = ViewModelProvider(requireActivity()).get(BookListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_list, container, false) as RecyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with (view as RecyclerView) {

            var books: BookList = BookList()

            bookListVM.getIncrement().observe(requireActivity()) {

                books = bookListVM.getBookList()

                books?.run{
                    val clickEvent = {
                            book:Book -> bookViewModel.setSelectedBook(book)
                        Log.d("BLF", "Book Selected Id: ${bookViewModel.getSelectedBook().value!!.id}")
                        (requireActivity() as SelectionFragmentInterface).bookSelected()
                    }

                    layoutManager = LinearLayoutManager(getContext())
                    adapter = BookListAdapter(this, clickEvent)

                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    class BookListAdapter(_books: BookList, _clickEvent: (Book)->Unit) : RecyclerView.Adapter<BookListAdapter.BookListViewHolder>() {

        val books = _books
        val clickEvent = _clickEvent

        class BookListViewHolder(_view: View) : RecyclerView.ViewHolder(_view){
            val view = _view;
            val title = _view.findViewById<TextView>(R.id.titleTextView)
            val author = _view.findViewById<TextView>(R.id.authorTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookListViewHolder {
            return BookListViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.book_list_layout, parent, false)
            )
        }

        override fun onBindViewHolder(holder: BookListViewHolder, position: Int) {
            holder.title.text = books[position].title
            if(books[position].downloaded)
                holder.title.setTextColor(Color.parseColor("#00FF00"))
            holder.author.text = books[position].author
            holder.view.setOnClickListener { clickEvent(books[position]) }
        }

        override fun getItemCount(): Int {
            return books.size()
        }
    }

    interface SelectionFragmentInterface {
        fun bookSelected()
    }
}