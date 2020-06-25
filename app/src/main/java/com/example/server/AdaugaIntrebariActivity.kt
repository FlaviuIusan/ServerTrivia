package com.example.server

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.google.firebase.database.*

class AdaugaIntrebariActivity : AppCompatActivity() {


    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adaugaintrebari)

        val EditTextIntrebare = findViewById<EditText>(R.id.editTextIntrebare)
        val EditTextRaspuns = findViewById<EditText>(R.id.editTextRaspuns)

        findViewById<Button>(R.id.buttonAdaugaIntrebare).setOnClickListener(){
            database = FirebaseDatabase.getInstance()
            databaseReference = database.getReference()
            var number = 0;
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for( intrebare in dataSnapshot.child("questions").children){
                        number = number +1;
                    }

                    number = number +1;
                    Log.e("Number este", number.toString())

                    databaseReference.child("questions").child(number.toString()).child("answer").setValue(EditTextRaspuns.text.toString())
                    databaseReference.child("questions").child(number.toString()).child("question").setValue(EditTextIntrebare.text.toString())

                    EditTextIntrebare.setText("")
                    EditTextRaspuns.setText("")

                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        }
    }
}