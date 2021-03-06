package com.laam.laammedia.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.laam.laammedia.R
import com.laam.laammedia.adapters.HomeRecyclerViewAdapter
import com.laam.laammedia.models.Post
import com.laam.laammedia.services.api.PostService
import com.laam.laammedia.services.api.ServiceBuilder
import com.laam.laammedia.services.SharedPrefHelper
import com.laam.laammedia.ui.activities.HeaderMessageActivity
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        home_recyclerview.layoutManager = LinearLayoutManager(activity)
        home_recyclerview.setHasFixedSize(true)

        callDataNotUser()

        home_swipe_refresh.setOnRefreshListener {
            callDataNotUser()
        }

        main_img_send.setOnClickListener {
            startActivity(Intent(activity, HeaderMessageActivity::class.java))
        }

        home_btn_start_following.setOnClickListener {
            onFollowingPressed()
        }
    }

    private fun onFollowingPressed() {
        (activity!!.findViewById(R.id.bottomNavigationView) as BottomNavigationView).selectedItemId =
            R.id.navigation_discover
    }

    override fun onResume() {
        super.onResume()

        callDataNotUser()
    }

    private fun callDataNotUser() {
        home_swipe_refresh.isRefreshing = true

        val pref = SharedPrefHelper(activity!!.applicationContext)

        val service: Call<List<Post>> =
            ServiceBuilder.buildService(PostService::class.java).getPostHome(pref.getAccount().id)

        service.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    val listPost: List<Post> = response.body()!!
                    val adapter = HomeRecyclerViewAdapter(
                        listPost,
                        activity!!.applicationContext,
                        activity!!.supportFragmentManager
                    )
                    home_recyclerview.adapter = adapter

                    if (listPost.isEmpty()) {
                        home_layout_not_found.visibility = View.VISIBLE
                    } else {
                        home_layout_not_found.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                }
                home_swipe_refresh.isRefreshing = false
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Toast.makeText(activity, "Error : ${t.message}", Toast.LENGTH_SHORT).show()
                home_swipe_refresh.isRefreshing = false
            }
        })
    }

    companion object {
        fun newInstance(): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
