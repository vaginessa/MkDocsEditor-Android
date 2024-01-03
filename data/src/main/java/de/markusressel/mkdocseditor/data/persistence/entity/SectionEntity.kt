package de.markusressel.mkdocseditor.data.persistence.entity

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany

/**
 * Created by Markus on 05.06.2018.
 */
@Entity
class SectionEntity(
    @Id var entityId: Long = 0,
    val id: String = "",
    var name: String = ""
) {

    lateinit var subsections: ToMany<SectionEntity>

    @Backlink
    lateinit var documents: ToMany<DocumentEntity>

    @Backlink
    lateinit var resources: ToMany<ResourceEntity>

}
