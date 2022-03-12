package com.mobiledeveloper.vktube.ui.screens.splash

import androidx.lifecycle.viewModelScope
import com.mobiledeveloper.vktube.base.BaseViewModel
import com.mobiledeveloper.vktube.data.login.LoginRepository
import com.mobiledeveloper.vktube.ui.screens.splash.models.SplashAction
import com.mobiledeveloper.vktube.ui.screens.splash.models.SplashEvent
import com.mobiledeveloper.vktube.ui.screens.splash.models.SplashViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : BaseViewModel<SplashViewState, SplashAction, SplashEvent>(initialState = SplashViewState()) {

    override fun obtainEvent(viewEvent: SplashEvent) {
        when(viewEvent) {
            SplashEvent.CheckLogin -> checkLogin()
        }
    }

    private fun checkLogin() {
        val isLogged = loginRepository.checkLogin()

        viewModelScope.launch {
            if (isLogged) {
                callAction(SplashAction.OpenMain)
            } else {
                callAction(SplashAction.OpenLogin)
            }
        }
    }
}