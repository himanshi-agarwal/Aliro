package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    private lateinit var topAnimation : Animation
    private lateinit var bottomAnimation : Animation
    private lateinit var logo : CardView
    private lateinit var app : TextView
    private lateinit var slogan : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.splash)

        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        logo = findViewById(R.id.logo)
        app = findViewById(R.id.app_name)
        slogan = findViewById(R.id.slogan)

        logo.startAnimation(topAnimation)
        app.startAnimation(bottomAnimation)
        slogan.startAnimation(bottomAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)
    }
}