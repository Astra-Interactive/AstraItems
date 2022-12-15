package com.astrainteractive.empire_items.meg.api

import com.astrainteractive.empire_items.meg.BossBarController
import com.astrainteractive.empire_items.meg.EmpireEntity
import com.astrainteractive.empire_items.meg.EntityInfo
import com.astrainteractive.empire_items.meg.api.core.IEmpireActiveModel
import com.astrainteractive.empire_items.meg.api.core.IEmpireModelEngineAPI
import com.astrainteractive.empire_items.meg.api.core.IEmpireModeledEntity
import ru.astrainteractive.astralibs.async.PluginScope
import ru.astrainteractive.astralibs.async.BukkitMain
import ru.astrainteractive.astralibs.utils.valueOfOrNull
import com.astrainteractive.empire_itemss.api.EmpireItemsAPI
import com.astrainteractive.empire_itemss.api.play
import com.astrainteractive.empire_itemss.api.utils.IManager
import com.astrainteractive.empire_itemss.api.utils.addAttribute
import com.astrainteractive.empire_itemss.api.utils.calcChance
import com.astrainteractive.empire_itemss.api.utils.getBiome
import com.atrainteractive.empire_items.models.mob.YmlMob
import com.ticxo.modelengine.api.ModelEngineAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityTargetEvent
import kotlin.math.max


object EmpireModelEngineAPI : IEmpireModelEngineAPI, IManager {
    val entityTagHolder = TagHolder<Entity, EntityInfo>()
    private val ignoredLocations = HashSet<Location>()

    private fun getMobSpawnList(e: Entity): List<YmlMob>? {
        val mobs = EmpireItemsAPI.ymlMobById.values.filter { ymlMob ->
            !ymlMob.spawn?.values?.filter { cond ->
                val chance = cond.replace[e.type.name]
                calcChance(chance?.toFloat() ?: -1f) &&
                (e.location.y > cond.minY && e.location.y < cond.maxY) &&
                (cond.biomes.isEmpty() || cond.biomes.contains(e.location.getBiome().name))
            }.isNullOrEmpty()
        }
        if (mobs.isEmpty()) return null
        return mobs
    }

    private fun configureEntity(entity: Entity, ymlMob: YmlMob) = entity.apply {
        customName = ymlMob.id
        isCustomNameVisible = false

        (this as? LivingEntity)?.let {
            it.removeWhenFarAway = false
            equipment?.clear()
            ymlMob.attributes.forEach { (_, ymlAttribute) ->
                val attr = valueOfOrNull<Attribute>(ymlAttribute.name) ?: return@forEach
                addAttribute(attr, ymlAttribute.realValue)
            }
            val maxHealth = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: it.maxHealth
            health = maxHealth
            ymlMob.potionEffects.forEach {
                it.value.copy(duration = Int.MAX_VALUE).play(this)
            }
        }

        if (!ymlMob.canBurn) {
            isVisualFire = false
            fireTicks = 0
        }
        isSilent = true
    }

    override fun createActiveModel(id: String): IEmpireActiveModel {
        val activeModel = ModelEngineAPI.createActiveModel(id)
        return EmpireActiveModel(activeModel)
    }

    override fun createModeledEntity(entity: Entity): IEmpireModeledEntity {
        val modeledEntity = ModelEngineAPI.createModeledEntity(entity)
        return EmpireModeledEntity(modeledEntity)
    }

    override fun replaceEntity(entity: Entity, vararg ymlMobs: YmlMob): EmpireEntity {
        val ymlMob = ymlMobs.randomOrNull() ?: throw Exception("Replace entity list must not be empty")
        val entityInfo = EntityInfo(
            modelID = ymlMob.modelID,
            empireID = ymlMob.id,
            uuid = entity.uniqueId
        )
        entityTagHolder.put(entity, entityInfo)
        val model = createActiveModel(ymlMob.id)
        val modeledEntity = createModeledEntity(entity).apply {
            addModel(model)
            isBaseEntityVisible = false
        }
        ymlMob.bossBar?.let { BossBarController.create(entity, it) }
        return EmpireEntity(entity, ymlMob, modeledEntity, model)
    }

    fun processSpawnedMob(location: Location, entity: Entity) {
        if (ignoredLocations.contains(location)) return
        val ymlMon = getMobSpawnList(entity)?.firstOrNull() ?: return
        entity.remove()
        spawnMob(ymlMon, location)
    }

    override fun spawnMob(ymlMob: YmlMob, location: Location): EmpireEntity {
        ignoredLocations.add(location)
        val entityType = EntityType.fromName(ymlMob.entity) ?: throw Exception("Unknown entity type: ${ymlMob.entity}")
        println("EntityType: $entityType")
        val entity =
            location.world.spawnEntity(location, entityType).run { configureEntity(this, ymlMob) }
        val replaced = replaceEntity(entity, ymlMob)
        ignoredLocations.remove(location)
        return replaced
    }

    override fun performAttack(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val entity = event.entity
        val damage = event.damage

        val entityInfo = entityTagHolder.get(damager) ?: return

        if (entityInfo.isAttacking) return
        else {
            entityTagHolder.update(damager) {
                entityInfo.apply { isAttacking = true }
            }
            event.isCancelled = true
        }

        val empireEntity = getEmpireEntity(damager) ?: return
        empireEntity.activeModel.playAnimation("attack")


        val distance = damager.location.distance(entity.location)

        PluginScope.launch(Dispatchers.IO) {
            delay(empireEntity.ymlMob.hitDelay.toLong())

            val calculatedDamage = if (empireEntity.ymlMob.decreaseDamageByRange)
                damage / max(1.0, distance)
            else damage

            PluginScope.launch(Dispatchers.BukkitMain) {
                if ((damager as LivingEntity).health > 0)
                    (entity as LivingEntity).damage(calculatedDamage, damager)
                empireEntity.ymlMob.events["onDamage"]?.playSound?.play(damager.location)
            }

            delay(1000L)

            entityTagHolder.update(damager) {
                entityInfo.apply { isAttacking = false }
            }
        }

    }

    override fun getEmpireEntity(entity: Entity): EmpireEntity? = kotlin.runCatching {
        val entityInfo = entityTagHolder.get(entity) ?: return null
        val ymlMob = EmpireItemsAPI.ymlMobById[entityInfo.empireID] ?: return null
        val modeledEntity = ModelEngineAPI.getModeledEntity(entity.uniqueId) ?: return null
        val activeModel = modeledEntity?.getModel(entityInfo.modelID ?: return null) ?: return null
        val empireModeledEntity = EmpireModeledEntity(modeledEntity)
        val empireActiveModel = EmpireActiveModel(activeModel)
        return EmpireEntity(
            entity = entity,
            ymlMob = ymlMob,
            modeledEntity = empireModeledEntity,
            activeModel = empireActiveModel
        )
    }.getOrNull()

    fun onEntityDied(entity: Entity) {
        entityTagHolder.remove(entity)
    }

    fun onEntityTarget(event: EntityTargetEvent) {
        val targetName = event.target?.type?.name?.uppercase() ?: return
        val ymlMob = getEmpireEntity(event.entity)?.ymlMob ?: return
        if (ymlMob.ignoreMobs.contains(targetName))
            event.isCancelled = true

    }

    override suspend fun onEnable() {
    }

    override suspend fun onDisable() {
        val players = Bukkit.getOnlinePlayers()
        entityTagHolder.map.forEach { (entity, entityInfo) ->
            getEmpireEntity(entity)?.let { empireEntity ->
                players.forEach {
                    empireEntity.activeModel.activeModel.hideFromPlayer(it)
                    empireEntity.modeledEntity.modeledEntity.hideFromPlayer(it)
                }
                empireEntity.modeledEntity.modeledEntity.removeModel(entityInfo.modelID)
                empireEntity.modeledEntity.modeledEntity.destroy()
                empireEntity.activeModel.activeModel.destroy()
            }
            entity.remove()
        }

    }
}
