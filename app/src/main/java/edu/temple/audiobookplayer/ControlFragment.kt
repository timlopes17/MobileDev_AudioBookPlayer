package edu.temple.audiobookplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso

class ControlFragment : Fragment() {

    lateinit var bookViewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookViewModel = ViewModelProvider(requireActivity()).get(BookViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playBut = view.findViewById<ImageButton>(R.id.playButton)
        val stopBut = view.findViewById<ImageButton>(R.id.stopButton)
        val pauseBut = view.findViewById<ImageButton>(R.id.pauseButton)
        val nowText = view.findViewById<TextView>(R.id.nowPlayingText)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)

        bookViewModel.getSelectedBook().observe(requireActivity()) {
            nowText.text = it.title

            playBut.setOnClickListener {
                (requireActivity() as ControlFragment.ControlFragmentInterface).playBook(it.id)
            }
            stopBut.setOnClickListener {
                (requireActivity() as ControlFragment.ControlFragmentInterface).stopBook()
            }
            pauseBut.setOnClickListener {
                (requireActivity() as ControlFragment.ControlFragmentInterface).pauseBook()
            }
        }
    }

    interface ControlFragmentInterface {
        fun playBook(bookId : Int)
        fun stopBook()
        fun pauseBook()
        fun seekBook(position: Int)
        fun getProgress()
    }
}