package co.ghostnotes.sample.list.swipe.ui

import android.graphics.Canvas
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import co.ghostnotes.sample.list.swipe.R
import co.ghostnotes.sample.list.swipe.SwipeAction
import co.ghostnotes.sample.list.swipe.ui.FirstFragment.SwipeActionAdapter.SwipeActionViewHolder
import co.ghostnotes.sample.list.swipe.SwipeAction.ARCHIVE
import co.ghostnotes.sample.list.swipe.SwipeAction.DELETE
import co.ghostnotes.sample.list.swipe.model.User
import co.ghostnotes.sample.list.swipe.databinding.FragmentFirstBinding
import co.ghostnotes.sample.list.swipe.databinding.ListItemUserBinding
import co.ghostnotes.sample.list.swipe.util.DummyDataUtil
import timber.log.Timber
import java.lang.IllegalStateException
import java.lang.UnsupportedOperationException

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private val swipeActionViewModel: SwipeActionViewModel by activityViewModels()
    // TODO DI
    private val dummyDataUtil = DummyDataUtil()

    private var _binding: FragmentFirstBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var adapter: SwipeActionAdapter
    private var swipeActionCallback: SwipeActionCallback? = null
    // TODO
    //private var testItemAnimator: TestItemAnimator? = TestItemAnimator { changeRecyclerViewBackgroundColor(NONE) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SwipeActionAdapter(dummyDataUtil.createDummyUsers())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            //itemAnimator = testItemAnimator TODO
            adapter = this@FirstFragment.adapter
        }

        swipeActionCallback = SwipeActionCallback(0, LEFT or RIGHT) { _, direction, position ->
            lifecycleScope.launchWhenCreated {
                val swipeAction = when (direction) {
                    LEFT -> {
                        // DELETE
                        changeRecyclerViewBackgroundColor(DELETE)
                        adapter.deleteItem(position)
                        DELETE
                    }
                    RIGHT -> {
                        // ARCHIVE
                        changeRecyclerViewBackgroundColor(ARCHIVE)
                        adapter.archiveItem(position)
                        ARCHIVE
                    }
                    else -> throw IllegalStateException()
                }

                swipeActionViewModel.showSnackbar(swipeAction, position)
            }
        }.also { callback ->
            ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        swipeActionCallback = null
        //testItemAnimator = null TODO
    }

    private fun changeRecyclerViewBackgroundColor(swipeAction: SwipeAction) {
        val color = when (swipeAction) {
            ARCHIVE -> R.color.list_item_background_archive
            DELETE -> R.color.list_item_background_delete
            else -> android.R.color.transparent
        }

        binding.recyclerView.setBackgroundResource(color)
    }

    private class TestItemAnimator(
        private val animationFinishedCallback: (() -> Unit)? = null
    ) : DefaultItemAnimator() {
        override fun onAnimationFinished(viewHolder: ViewHolder) {
            super.onAnimationFinished(viewHolder)
            animationFinishedCallback?.invoke()
        }
    }

    private class SwipeActionAdapter(val users: List<User>) : RecyclerView.Adapter<SwipeActionViewHolder>() {

        private var lastArchivedListItemIndex: Int? = null
        private var lastArchivedListItem: User? = null
        fun archiveItem(index: Int) {
            notifyItemRemoved(index)

            lastArchivedListItemIndex = index
            lastArchivedListItem = users.toMutableList().removeAt(index)
        }

        private var lastDeletedListItemIndex: Int? = null
        private var lastDeletedListItem: User? = null
        fun deleteItem(index: Int) {
            notifyItemRemoved(index)

            lastDeletedListItemIndex = index
            lastDeletedListItem = users.toMutableList().removeAt(index)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeActionViewHolder {
            val binding = ListItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SwipeActionViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SwipeActionViewHolder, position: Int) {
            holder.bind(users[position])
        }

        override fun getItemCount(): Int {
            return users.size
        }

        class SwipeActionViewHolder(private val binding: ListItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
            val foreground = binding.foreground

            fun bind(user: User) {
                binding.user = user
            }

            fun setBackground(swipeAction: SwipeAction) {
                when (swipeAction) {
                    ARCHIVE -> {
                        binding.backgroundArchive.visibility = View.VISIBLE
                        binding.backgroundDelete.visibility = View.INVISIBLE
                    }
                    DELETE -> {
                        binding.backgroundArchive.visibility = View.INVISIBLE
                        binding.backgroundDelete.visibility = View.VISIBLE
                    }
                    else -> throw UnsupportedOperationException("Unknown SwipeAction [$swipeAction]")
                }
            }
        }
    }

    private class SwipeActionCallback(
        dragDirs: Int,
        swipeDirs: Int,
        private val onSwiped: ((viewHolder: SwipeActionViewHolder, direction: Int, itemPosition: Int) -> Unit)? = null,
    ) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: ViewHolder,
            target: ViewHolder
        ): Boolean {
            return true
        }

        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            //  actionState
            //    - ACTION_STATE_IDLE
            //    - ACTION_STATE_SWIPE
            //    - ACTION_STATE_DRAG.
            when (actionState) {
                ACTION_STATE_IDLE -> Timber.d("### onSelectedChanged(): ACTION_STATE_IDLE")
                ACTION_STATE_SWIPE -> Timber.d("### onSelectedChanged(): ACTION_STATE_SWIPE")
                ACTION_STATE_DRAG -> Timber.d("### onSelectedChanged(): ACTION_STATE_DRAG")
                else -> Timber.d("### onSelectedChanged(): UNKNOWN")
            }

            if (viewHolder == null) return
            getDefaultUIUtil().onSelected((viewHolder as SwipeActionViewHolder).foreground)
        }

        override fun onChildDrawOver(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: ViewHolder?,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            if (viewHolder == null) return
            val swipeActionViewHolder = viewHolder as? SwipeActionViewHolder ?: return

            swipeActionViewHolder.setBackground(if (dX < 0) DELETE else ARCHIVE)
            getDefaultUIUtil().onDrawOver(
                c,
                recyclerView,
                swipeActionViewHolder.foreground,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
            getDefaultUIUtil().clearView((viewHolder as SwipeActionViewHolder).foreground)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            getDefaultUIUtil().onDraw(
                c,
                recyclerView,
                (viewHolder as SwipeActionViewHolder).foreground,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            onSwiped?.invoke((viewHolder as SwipeActionViewHolder), direction, viewHolder.adapterPosition)
        }
    }
}