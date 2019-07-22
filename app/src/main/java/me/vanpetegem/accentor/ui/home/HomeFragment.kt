package me.vanpetegem.accentor.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.BaseMainFragment

class HomeFragment(callback: (SwipeRefreshLayout.OnChildScrollUpCallback?) -> Unit) : BaseMainFragment(callback) {

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
    }

}
