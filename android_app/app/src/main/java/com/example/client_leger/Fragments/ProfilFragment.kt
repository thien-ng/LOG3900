package com.example.client_leger.Fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.example.client_leger.Adapters.ConnexionListViewAdapter
import com.example.client_leger.Controller.ProfileController
import com.example.client_leger.R
import com.example.client_leger.databinding.FragmentProfilBinding

class ProfilFragment: Fragment() {
    lateinit var binding: FragmentProfilBinding
    private var profileController:ProfileController = ProfileController()
    lateinit var  adapter: ConnexionListViewAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentProfilBinding>(inflater,R.layout.fragment_profil, container, false)
        adapter = ConnexionListViewAdapter(this.context)
        profileController.getUserProfile(this, activity!!.intent.getStringExtra("username"))
        var listview = binding.root.findViewById<ListView>(R.id.listView);
        listview.adapter = adapter
        return binding.root
    }
}