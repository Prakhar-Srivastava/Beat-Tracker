package com.prosthetik.beattracker

import com.prosthetik.beattracker.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.Task
import kotlin.math.sign


class MainActivity : AppCompatActivity() {

    lateinit var mGoogleSignInClient: GoogleSignInClient

    companion object{
        @JvmStatic val RC_SIGN_IN: Int = 0x400
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun onSignIn(task: Task<GoogleSignInAccount>){
        val mAcc : GoogleSignInAccount? = task.result

        println(mAcc)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)

        if(account != null){
            val openDash = Intent(this, DashboardActivity::class.java).apply {
                putExtra("name", account.givenName)
                putExtra("avatar", account.photoUrl)
            }

            startActivity(openDash)
        }

        //make the button pretty

        // Set the dimensions of the sign-in button.
        // Set the dimensions of the sign-in button.
        val signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.setSize(SignInButton.SIZE_WIDE)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        signInButton.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                buttonClick(v)
            }
        })
    }

    private fun buttonClick(view: View?){
        when(view?.id){
            R.id.sign_in_button -> signIn()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            onSignIn(task)
        }
    }
}

