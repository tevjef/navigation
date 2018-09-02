package com.prolificinteractive.conductornav.fragments

import android.os.Bundle
import android.view.View

class FriendsFragment : BaseFragment() {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    btn.setOnClickListener {
      navController.navigate(FriendsFragmentDirections.toNextFriendsFragment()
          .setComNcapdeviSampleArgsInstance(mInt + 1))
    }
    btn.text = javaClass.simpleName + " " + mInt
  }
}