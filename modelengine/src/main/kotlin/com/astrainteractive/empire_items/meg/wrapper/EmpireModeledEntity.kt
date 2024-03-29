package com.astrainteractive.empire_items.meg.wrapper

import com.astrainteractive.empire_items.meg.wrapper.core.IEmpireActiveModel
import com.astrainteractive.empire_items.meg.wrapper.core.IEmpireModeledEntity
import com.ticxo.modelengine.api.model.ModeledEntity
import org.bukkit.Bukkit

class EmpireModeledEntity(override val modeledEntity: ModeledEntity) : IEmpireModeledEntity {
    override var isBaseEntityVisible: Boolean
        get() = modeledEntity.isBaseEntityVisible
        set(value) {
            modeledEntity.isBaseEntityVisible = value
        }
    override fun addModel(model: IEmpireActiveModel) {
        Bukkit.getOnlinePlayers().forEach(modeledEntity::showToPlayer)
        Bukkit.getOnlinePlayers().forEach(model.activeModel::showToPlayer)
        modeledEntity.addModel(model.activeModel, true)
        modeledEntity.setRenderRadius(32)
    }

}