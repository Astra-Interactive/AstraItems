package com.astrainteractive.empire_items.meg.wrapper

import com.astrainteractive.empire_items.meg.wrapper.core.IEmpireActiveModel
import com.ticxo.modelengine.api.model.ActiveModel

class EmpireActiveModel(override val activeModel: ActiveModel) : IEmpireActiveModel {

    override fun isPlayingAnimation(id: String): Boolean {
        return activeModel.animationHandler.isPlayingAnimation(id)
    }

    override val isAttackAnimationInProgress: Boolean
        get() = isPlayingAnimation("attack")

    override fun playAnimation(id: String) {
        activeModel.animationHandler.playAnimation(id, 0.0, 0.0, 1.0, true)
    }

}
