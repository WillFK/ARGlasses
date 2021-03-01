package fk.home.arglasses.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fk.home.arglasses.R
import fk.home.arglasses.ui.glasses.GlassesFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        load()
    }

    private fun load() {
        supportFragmentManager.beginTransaction()
            .add(R.id.container, GlassesFragment())
            .commitAllowingStateLoss()
    }
}
