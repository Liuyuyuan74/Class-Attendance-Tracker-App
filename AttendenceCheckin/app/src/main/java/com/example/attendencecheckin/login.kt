package com.example.attendencecheckin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.attendencecheckin.ui.main.LoginFragment

class login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, LoginFragment.newInstance())
                .commitNow()
        }
    }
}