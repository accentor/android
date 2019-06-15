package me.vanpetegem.accentor.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.navigation.NavigationView
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.albums.AlbumsFragment
import me.vanpetegem.accentor.ui.artists.ArtistsFragment
import me.vanpetegem.accentor.ui.home.HomeFragment
import me.vanpetegem.accentor.ui.login.LoginActivity
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        val headerView: View = navView.getHeaderView(0)
        val usernameText: TextView = headerView.findViewById(R.id.nav_header_username)
        val serverURLText: TextView = headerView.findViewById(R.id.nav_header_server_url)

        toggle.setHomeAsUpIndicator(R.drawable.ic_menu_back)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        mainViewModel.loginState.observe(this@MainActivity, Observer {
            val loggedIn = it ?: return@Observer
            if (!loggedIn) {
                startActivity<LoginActivity>()
                finish()
            } else {
                mainViewModel.refresh()
            }
        })

        mainViewModel.currentUser.observe(this@MainActivity, Observer {
            if (it == null) {
                usernameText.text = ""
            } else {
                usernameText.text = it.name
            }
        })

        mainViewModel.serverURL.observe(this@MainActivity, Observer {
            if (it == null) {
                serverURLText.text = ""
            } else {
                serverURLText.text = it
            }
        })

        mainViewModel.navState.observe(this@MainActivity, Observer {
            val navState = it ?: return@Observer

            val transaction = supportFragmentManager.beginTransaction()
            val fragment = when (navState.fragmentId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_albums -> AlbumsFragment()
                R.id.nav_artists -> ArtistsFragment()
                else -> HomeFragment()

            }

            transaction.replace(R.id.main_fragment_container, fragment)
            if (navState.showBack) {
                transaction.addToBackStack(null)
            }
            transaction.commit()

            toggle.isDrawerIndicatorEnabled = !navState.showBack
            toggle.syncState()
        })
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_sign_out -> {
                mainViewModel.logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        mainViewModel.navigate(item.itemId)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
