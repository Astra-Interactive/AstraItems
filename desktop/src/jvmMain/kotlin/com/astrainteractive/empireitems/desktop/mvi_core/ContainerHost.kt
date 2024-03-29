package com.astrainteractive.empireitems.desktop.mvi_core

interface ContainerHost<STATE : Any, SIDE_EFFECT : Any, INTENT : Any> {
    val container: IContainer<STATE, SIDE_EFFECT>
    fun onIntent(intent: INTENT)

    suspend fun SIDE_EFFECT.sendSideEffect(){
        container.send(this)
    }
}

