package com.example.mylawyer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mylawyer.databinding.FragmentCatalogueBinding

class CatalogueFragment : Fragment() {
    private lateinit var binding: FragmentCatalogueBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatalogueBinding.inflate(inflater, container, false)
        return binding.root
    }
}