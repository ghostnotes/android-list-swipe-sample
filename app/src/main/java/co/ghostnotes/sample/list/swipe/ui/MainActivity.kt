package co.ghostnotes.sample.list.swipe.ui

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import co.ghostnotes.sample.list.swipe.R
import co.ghostnotes.sample.list.swipe.R.id
import co.ghostnotes.sample.list.swipe.SwipeAction.ARCHIVE
import co.ghostnotes.sample.list.swipe.SwipeAction.DELETE
import co.ghostnotes.sample.list.swipe.SwipeActionData
import co.ghostnotes.sample.list.swipe.databinding.ActivityMainBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val swipeActionViewModel: SwipeActionViewModel by viewModels()

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                .setAction("Action", null).show()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        swipeActionViewModel.test.observe(this) {
            Timber.d("### liveData: $it")
        }

        lifecycleScope.launchWhenStarted {
            Timber.d("### launchWhenStarted")
            swipeActionViewModel.swipeActionData.collect { data ->
                Timber.d("### collect{}: action=$data")
                when (data.action) {
                    DELETE -> {
                        showSnackbar(data)
                    }
                    ARCHIVE -> {
                        showSnackbar(data)
                    }
                    else -> { /* do nothing... */ }
                }
            }
        }
    }

    private fun showSnackbar(data: SwipeActionData) {
        Snackbar.make(
            binding.coordinator,
            "SwipeAction=${data.action}, position=${data.position}",
            Snackbar.LENGTH_SHORT
        )
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
            .setAction("Action", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}