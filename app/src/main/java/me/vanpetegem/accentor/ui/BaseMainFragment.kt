package me.vanpetegem.accentor.ui

import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

open class BaseMainFragment(protected val scrollCallback: (SwipeRefreshLayout.OnChildScrollUpCallback?) -> Unit) :
    Fragment()