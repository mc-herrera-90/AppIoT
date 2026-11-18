package com.example.appiot.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.util.Locale
import com.example.appiot.R
import android.content.Intent

class UserListActivity : AppCompatActivity() {

    private lateinit var searchUser: EditText
    private lateinit var listUsers: ListView
    private lateinit var userCount: TextView
    private lateinit var emptyUsers: TextView

    private val allUsers = mutableListOf<JSONObject>()
    private val filteredUsers = mutableListOf<JSONObject>()

    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        searchUser = findViewById(R.id.etSearchUser)
        listUsers = findViewById(R.id.lvUsers)
        userCount = findViewById(R.id.tvUserCount)
        emptyUsers = findViewById(R.id.tvEmptyUsers)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            mutableListOf()
        )

        // Se configura el adaptador visual
        listUsers.adapter = object : android.widget.BaseAdapter() {

            override fun getCount(): Int = filteredUsers.size

            override fun getItem(position: Int): JSONObject =
                filteredUsers[position]

            override fun getItemId(position: Int): Long =
                position.toLong()

            override fun getView(
                position: Int,
                convertView: android.view.View?,
                parent: android.view.ViewGroup
            ): android.view.View {

                val view = convertView ?: layoutInflater.inflate(
                    android.R.layout.simple_list_item_2,
                    parent,
                    false
                )

                val user = getItem(position)

                val firstLine = view.findViewById<TextView>(
                    android.R.id.text1
                )

                val secondLine = view.findViewById<TextView>(
                    android.R.id.text2
                )

                val name = user.optString("firstName")
                val lastName = user.optString("lastName")
                val email = user.optString("email")

                firstLine.text = "$name $lastName"
                secondLine.text = email

                firstLine.setTextColor(
                    getColor(R.color.text_primary)
                )

                secondLine.setTextColor(
                    getColor(R.color.text_secondary)
                )

                return view
            }
        }

        listUsers.setOnItemClickListener { _, _, position, _ ->

            val selectedUser = filteredUsers[position]

            val intent = Intent(this, UserEditActivity::class.java)
            intent.putExtra("user_email", selectedUser.optString("email"))

            startActivity(intent)
        }

        loadUsers()
        updateList()

        searchUser.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                filterUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
        filterUsers(searchUser.text.toString())
    }

    private fun loadUsers() {
        allUsers.clear()

        val preferences = getSharedPreferences(
            "SYNTRA_USERS",
            MODE_PRIVATE
        )

        val json = preferences.getString("users", "[]") ?: "[]"
        val usersArray = JSONArray(json)

        for (i in 0 until usersArray.length()) {
            allUsers.add(usersArray.getJSONObject(i))
        }
    }

    private fun filterUsers(query: String) {
        filteredUsers.clear()

        val normalizedQuery = normalize(query)

        for (user in allUsers) {

            val firstName = normalize(user.optString("firstName"))
            val lastName = normalize(user.optString("lastName"))

            if (
                firstName.contains(normalizedQuery) ||
                lastName.contains(normalizedQuery)
            ) {
                filteredUsers.add(user)
            }
        }

        updateList()
    }

    private fun updateList() {
        (listUsers.adapter as android.widget.BaseAdapter).notifyDataSetChanged()

        userCount.text = "Usuarios encontrados: ${filteredUsers.size}"

        val isEmpty = filteredUsers.isEmpty()

        emptyUsers.visibility =
            if (isEmpty) android.view.View.VISIBLE
            else android.view.View.GONE

        listUsers.visibility =
            if (isEmpty) android.view.View.GONE
            else android.view.View.VISIBLE

        if (allUsers.isEmpty()) {
            emptyUsers.text = "Todavía no hay usuarios registrados."
        } else if (isEmpty) {
            emptyUsers.text = "No se encontraron usuarios."
        }
    }

    private fun normalize(value: String): String {
        return Normalizer.normalize(
            value.lowercase(Locale.ROOT),
            Normalizer.Form.NFD
        ).replace(Regex("\\p{Mn}+"), "")
    }
}
