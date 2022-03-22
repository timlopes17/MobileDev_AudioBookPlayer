package edu.temple.audiobookplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val BOOKS_KEY = "books_key"

/**
 * A simple [Fragment] subclass.
 * Use the [BookListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookListFragment : Fragment() {
    private var books: BookList? = null
    private var recyclerView : RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            books = it.getParcelable(BOOKS_KEY)
        }
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

            books?.run{
                val clickEvent = { book:String ->}

                layoutManager = LinearLayoutManager(requireContext())
                adapter = BookListAdapter(this, clickEvent)
            }
        }
    }

    class BookListAdapter(_books: BookList, _clickEvent: (String)->Unit) : RecyclerView.Adapter<BookListAdapter.BookListViewHolder>() {

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
            holder.author.text = books[position].author
        }

        override fun getItemCount(): Int {
            return books.size()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(list : BookList) =
            BookListFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(BOOKS_KEY, list)
                }
            }
    }
}