package ru.brauer.catalogofgoods.extensions

import ru.brauer.catalogofgoods.data.commerceml.EntityOfCommerceMl

fun List<EntityOfCommerceMl>.convertPathOfPhotoRelativelyFileName(fileName: String): List<EntityOfCommerceMl> {
    val pathName = fileName.dropLastWhile { it != '/' }
    return this.map { entity ->
        if (entity is EntityOfCommerceMl.Goods) {
            EntityOfCommerceMl.Goods(
                id = entity.id,
                name = entity.name,
                photoUrl = entity.photoUrl.map { photoUrl ->
                    pathName + photoUrl
                }
            )
        } else {
            entity
        }
    }
}