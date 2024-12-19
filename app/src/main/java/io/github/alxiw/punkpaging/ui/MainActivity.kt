package io.github.alxiw.punkpaging.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import io.github.alxiw.punkpaging.R
import io.github.alxiw.punkpaging.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val fragment: Fragment = CatalogueFragment()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addFragment(savedInstanceState)
    }

    fun addFragment(savedInstanceState: Bundle? = null, fragment: Fragment = this.fragment) {
        savedInstanceState ?: supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
