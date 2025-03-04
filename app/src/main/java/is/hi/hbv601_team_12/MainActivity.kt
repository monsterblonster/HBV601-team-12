package `is`.hi.hbv601_team_12

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import `is`.hi.hbv601_team_12.databinding.ActivityMainBinding
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: OfflineUsersRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
            ?.findNavController() ?: throw IllegalStateException("NavHostFragment not found")

        val sharedPref = getSharedPreferences("VibeVaultPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        val username = sharedPref.getString("loggedInUsername", null)

        val db = AppDatabase.getDatabase(this)
        repository = OfflineUsersRepository(db.userDao())

        if (isLoggedIn && username != null) {
            GlobalScope.launch(Dispatchers.IO) {
                val userExists = repository.getUserByUsername(username)
                withContext(Dispatchers.Main) {
                    if (userExists != null) {
                        navController.navigate(R.id.profileFragment)
                    } else {
                        with(sharedPref.edit()) {
                            putBoolean("isLoggedIn", false)
                            putString("loggedInUsername", null)
                            apply()
                        }
                        navController.navigate(R.id.loginFragment)
                    }
                }
            }
        } else {
            navController.navigate(R.id.loginFragment)
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.profileFragment, R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment || destination.id == R.id.registerFragment) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
            ?.findNavController() ?: throw IllegalStateException("NavHostFragment not found")
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
