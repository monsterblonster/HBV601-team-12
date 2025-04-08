package `is`.hi.hbv601_team_12

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineUsersRepository
import `is`.hi.hbv601_team_12.data.defaultRepositories.DefaultUsersRepository
import `is`.hi.hbv601_team_12.data.entities.Notification
import `is`.hi.hbv601_team_12.data.repositories.UsersRepository
import `is`.hi.hbv601_team_12.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var defaultUsersRepository: UsersRepository

    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val notificationHelper = NotificationHelper(this)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
            ?.findNavController() ?: throw IllegalStateException("NavHostFragment not found")

        val sharedPref = getSharedPreferences("VibeVaultPrefs", MODE_PRIVATE)
        //sharedPref.edit().clear().apply(); // long villa lausn
        var isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        this.userId = sharedPref.getLong("loggedInUserId", -1L)

        val db = AppDatabase.getDatabase(this)
        val offlineRepo = OfflineUsersRepository(db.userDao())
        val onlineRepo = OnlineUsersRepository(offlineRepo)
        defaultUsersRepository = DefaultUsersRepository(offlineRepo, onlineRepo)

        if (isLoggedIn && userId != -1L) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = defaultUsersRepository.getUserById(userId)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val user = response.body()
                            if (user != null) {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    defaultUsersRepository.cacheUser(user)
                                }
                                if (navController.currentDestination?.id != R.id.profileFragment) {
                                    navController.navigate(R.id.profileFragment)
                                }
                            } else {
                                handleFailedLogin(navController, sharedPref)
                            }
                        } else {
                            when (response.code()) {
                                401, 404 -> {
                                    handleFailedLogin(navController, sharedPref)
                                }
                                else -> {
                                    attemptOfflineLogin(navController, sharedPref, userId)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Network Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        attemptOfflineLogin(navController, sharedPref, userId)
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

        lifecycleScope.launch(Dispatchers.IO) {
            if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1
                    )
                }
            }
            while (isActive) {
                isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
                userId = sharedPref.getLong("loggedInUserId", -1L)
                if (isLoggedIn && userId != -1L) {
                    try {
                        val notificationResponse: Response<List<Notification>> =
                            onlineRepo.getUserNotifications(userId)

                        if (notificationResponse.isSuccessful && notificationResponse.body() != null) {
                            val notifications = notificationResponse.body()!!

                            if (notifications.isNotEmpty()) {
                                for (notification in notifications) {
                                    // send the notification to the user with the title and message from the response
                                    notificationHelper.sendNotification(
                                        "Message from vibevault",
                                        notification.message
                                    )
                                }
                                // clear the notifications from the server
                                onlineRepo.clearNotifications(userId)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Notification Error: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            // notificationHelper.sendNotification("Notification from VibeVault", "Error: ${e.message}")
                        }
                    }
                }
                delay(5000) // 5 seconds delay before checking again
            }
        }
    }





    private fun attemptOfflineLogin(
        navController: androidx.navigation.NavController,
        sharedPref: android.content.SharedPreferences,
        userId: Long
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val cachedUser = defaultUsersRepository.getUserByIdOffline(userId)
            withContext(Dispatchers.Main) {
                if (cachedUser != null) {
                    if (navController.currentDestination?.id != R.id.profileFragment) {
                        navController.navigate(R.id.profileFragment)
                    }
                } else {
                    handleFailedLogin(navController, sharedPref)
                }
            }
        }
    }

    private fun handleFailedLogin(
        navController: androidx.navigation.NavController,
        sharedPref: android.content.SharedPreferences
    ) {
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", false)
            putLong("loggedInUserId", -1L)
            apply()
        }
        navController.navigate(R.id.loginFragment)
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
