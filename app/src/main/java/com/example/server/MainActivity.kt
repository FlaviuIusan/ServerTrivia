package com.example.server

import android.R.attr.data
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.io.*
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listaMesaje: ArrayList<String> = ArrayList()

        findViewById<Button>(R.id.buttonAdaugaIntrebareActiv).setOnClickListener{
            val intent = Intent(applicationContext, AdaugaIntrebariActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.buttonDescarcaIstoric).setOnClickListener{
            val outputStreamWriter = OutputStreamWriter(applicationContext.openFileOutput("AMele/config.txt", Context.MODE_PRIVATE))

            for(linie in listaMesaje){
                outputStreamWriter.write(linie)
            }

            outputStreamWriter.close()


        }

        val listaSocketClienti: ArrayList<Socket> = ArrayList()
        var question = ""
        var answer = ""


        findViewById<Button>(R.id.buttonStartServer).setOnClickListener {
            Log.e("Button", "Apasat")
            database = FirebaseDatabase.getInstance()
            databaseReference = database.getReference()
            val server = ServerSocket(8888,10, InetAddress.getByName("0.0.0.0"))
            thread { //Thread acceptare conectiune clienti
                run {
                    Log.e("first run check server", server.inetAddress.hostAddress)
                    Log.e("first run", "waiting for clients")
                    while (true) {
                        val client = server.accept()
                        Log.e("first run", "got a client")
                        listaSocketClienti.add(client)

                        Log.e("first run", "added client")
                        thread { //Thread ascultare client, trimitere mesaje la client, game logic
                            run {
                                Log.e("second run", "waiting for messages")
                                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                                var userId = ""
                                var userName = ""
                                var score : Long = -1
                                var firstMessage = true
                                var indexSfarsitUserId : Int = -1
                                var text : String
                                while(true) {
                                    try { //aici primesc mesajele de la clienti
                                        val citeste = reader.readLine()
                                        text = citeste
                                        if(userId.compareTo("")==0 &&userName.compareTo("")==0 && text != null){
                                            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                    indexSfarsitUserId = text.toString().indexOf("-")
                                                    userId = text.toString().substring(0, indexSfarsitUserId)
                                                    userName = dataSnapshot.child("users").child(userId).child("username").getValue() as String
                                                    score = dataSnapshot.child("users").child(userId).child("score").getValue() as Long
                                                }

                                                override fun onCancelled(databaseError: DatabaseError) {}
                                            })
                                            while(userName.compareTo("")==0 && score.compareTo(-1)==0){
                                                Log.e("Waiting for data user", "Still waiting")
                                            }
                                            Log.e("initializat user", userName + " " + score.toString()) //score se aloca corect dar afiseaza -1 in consola / functioneaza

                                        }
                                        if(text != null){
                                            if(firstMessage){
                                                val firstText = text.toString().substring(indexSfarsitUserId, text.toString().length)
                                                text = firstText
                                                firstMessage = false
                                            }
                                            for(clientSocket in listaSocketClienti){
                                                val writer = PrintWriter(BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream())), true)
                                                writer.println(text)
                                            }

                                            listaMesaje.add(text)
                                        }
                                        if(text.toString().contains("!start")){
                                            val randomQuestion = Random.nextInt(1, 3)
                                            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                       question = dataSnapshot.child("questions").child(randomQuestion.toString()).child("question").getValue().toString()
                                                        answer = dataSnapshot.child("questions").child(randomQuestion.toString()).child("answer").getValue().toString()
                                                        Log.e("for database question", question)
                                                        Log.e("for database answer", answer)


                                                }

                                                override fun onCancelled(databaseError: DatabaseError) {}
                                            })
                                            while(question.compareTo("")==0 && answer.compareTo("")==0)
                                            {
                                                Log.e("Waiting for data", "Still waiting");
                                            }
                                            for(clientSocket in listaSocketClienti){
                                                val writer = PrintWriter(BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream())), true)
                                                Log.e("send question", question)
                                                writer.println("Server: " + question)
                                            }

                                        }
                                        if(text.toString().contains(answer) && answer!=""){
                                            //adauga punctaj in baza de date
                                            score=score+1
                                            databaseReference.child("users").child(userId).child("score").setValue(score)

                                            for(clientSocket in listaSocketClienti){
                                                val writer = PrintWriter(BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream())), true)
                                                Log.e("send announcement answear correct", question)
                                                writer.println("Server: " + userName + " a raspuns corect !!!")
                                                writer.println("ServerX: " + userId + " a raspuns corect !!!")

                                            }
                                            question = ""
                                            answer = ""
                                            val randomQuestion = Random.nextInt(1, 3)
                                            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                    question = dataSnapshot.child("questions").child(randomQuestion.toString()).child("question").getValue().toString()
                                                    answer = dataSnapshot.child("questions").child(randomQuestion.toString()).child("answer").getValue().toString()
                                                    Log.e("for database question", question)
                                                    Log.e("for database answer", answer)


                                                }

                                                override fun onCancelled(databaseError: DatabaseError) {}
                                            })
                                            while(question.compareTo("")==0 && answer.compareTo("")==0){
                                                Log.e("Waiting for data question", "Still waiting");
                                            }

                                            for(clientSocket in listaSocketClienti){
                                                val writer = PrintWriter(BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream())), true)
                                                Log.e("send question", question)
                                                writer.println("Server: " + question)
                                            }
                                        }
                                    }catch (ex: Exception){
                                        Log.e("client" + client.inetAddress.hostAddress, ex.toString())
                                    }
                                }
                            }
                        }
                    }
                }
            }


        }

    }
}