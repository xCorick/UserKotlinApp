package com.example.proyecto

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(framgentActivity: FragmentActivity):
    FragmentStateAdapter(framgentActivity) {

    //numero de pestañas
    override fun getItemCount(): Int = 3

    //Navegacion de los fragmentos, devuelve el fragmento actual
    override fun createFragment(position: Int): Fragment {
        return  when(position){
            0 -> Fragment1()
            1 -> ClientesFragment()
            2 -> Fragment3()
            else -> Fragment1()
        }
    }
}