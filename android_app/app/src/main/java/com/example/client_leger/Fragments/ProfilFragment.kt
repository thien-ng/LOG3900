package com.example.client_leger.Fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.client_leger.R
import com.example.client_leger.databinding.FragmentProfilBinding
import com.example.client_leger.models.User

class ProfilFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var binding = DataBindingUtil.inflate<FragmentProfilBinding>(inflater,R.layout.fragment_profil, container, false)
        binding.user = User("user")
        return binding.root
    }
}