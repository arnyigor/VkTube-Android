package com.mobiledeveloper.vktube.ui.screens.settings

import androidx.lifecycle.viewModelScope
import com.mobiledeveloper.vktube.base.BaseViewModel
import com.mobiledeveloper.vktube.ui.screens.settings.models.SettingsAction
import com.mobiledeveloper.vktube.ui.screens.settings.models.SettingsEvent
import com.mobiledeveloper.vktube.ui.screens.settings.models.SettingsViewState
import com.vk.api.sdk.VK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(

) : BaseViewModel<SettingsViewState, SettingsAction, SettingsEvent>(initialState = SettingsViewState()) {

    override fun obtainEvent(viewEvent: SettingsEvent) {
        when (viewEvent) {
            SettingsEvent.ClearAction -> clearAction()
            SettingsEvent.LogOut -> logOutVK()
            SettingsEvent.SubscriptionsScreen -> goToSubscribesList()
        }
    }

    private fun logOutVK() {
        VK.logout()
        viewAction = SettingsAction.NavigateLogin
    }

    private fun goToSubscribesList() {
        VK.logout()
        viewAction = SettingsAction.NavigateSubscribes
    }


    private fun clearAction() {
        viewModelScope.launch {
            viewAction = null
        }
    }
}