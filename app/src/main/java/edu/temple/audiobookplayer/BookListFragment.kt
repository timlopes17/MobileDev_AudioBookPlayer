package edu.temple.audiobookplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var recyclerView : RecyclerView = null

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
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    class BookListAdapter(_books: BookList, _clickEvent: (String)->Unit) : RecyclerView.Adapter<BookListAdapter.BookListViewHolder>() {

        val books = _books
        val clickEvent = _clickEvent

        class BookListViewHolder(_view: View) : RecyclerView.ViewHolder(_view){
            val view = _view;
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookListViewHolder {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: BookListViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            return books.size()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BookListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookListFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(BOOKS_KEY, books)
                }
            }
    }
}