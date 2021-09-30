package com.example.APIDogs

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.APIDogs.databinding.ActivityMainBinding
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: DogAdapter
    private val dogImages = mutableListOf<String>()
    private val dogBreeds = mutableListOf<String>()
    private val listAllBreeds = "https://dog.ceo/api/breeds/"
    private val imagesByBreeds = "https://dog.ceo/api/breed/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.svMovies.setOnQueryTextListener(this)
        initRecyclerView()
        imagesAllDogs()
    }

    private fun initRecyclerView() {
        adapter = DogAdapter(dogImages)
        binding.rvDogs.layoutManager = LinearLayoutManager(this)
        binding.rvDogs.adapter = adapter
    }

    private fun getRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun imagesAllDogs() {
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit(listAllBreeds).create(APIService::class.java)
                .getAllBreeds("list/all")
            val breeds = call.body()
            runOnUiThread {
                if (call.isSuccessful) {
                    val breed = breeds?.breeds ?: ""
                    val jsonElement: JsonElement = JsonParser().parse(breed.toString())
                    val jsonObject = jsonElement.asJsonObject
                    val entrySet = jsonObject.entrySet()
                    entrySet.forEach {
                        val breedName = it.key
                        val dogArray = jsonObject.getAsJsonArray(breedName)
                        if (dogArray != null && dogArray.size() > 0) {
                            dogArray.forEach { dog ->
                                dogBreeds.add(dog.toString().replace("\"", ""))
                            }
                        } else {
                            dogBreeds.add(breedName)
                        }
                    }
                } else {
                    showError()
                }
                dogImages.clear()
                dogBreeds.forEach { breed ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val call2 = getRetrofit(imagesByBreeds).create(APIService::class.java)
                            .getDogsByBreeds("$breed/images")
                        val puppies = call2.body()
                        runOnUiThread {
                            if (call2.isSuccessful) {
                                val images = puppies?.images ?: emptyList()
                                dogImages.addAll(images)
                                adapter.notifyDataSetChanged()
                                Log.e("TAG", "imagesAllDogs: $images")
                            } else {
                                showError()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun searchByName(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit(imagesByBreeds).create(APIService::class.java)
                .getDogsByBreeds("$query/images")
            val puppies = call.body()
            runOnUiThread {
                if (call.isSuccessful) {
                    val images = puppies?.images ?: emptyList()
                    dogImages.clear()
                    dogImages.addAll(images)
                    adapter.notifyDataSetChanged()
                } else {
                    showError()
                }
                hideKeyBoard()
            }
        }
    }

    private fun hideKeyBoard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.viewRoot.windowToken, 0)
    }

    private fun showError() {
        Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (!query.isNullOrEmpty()) {
            searchByName(query.toLowerCase())
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }
}