package com.twofasapp.data.main.domain

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.Test

class CloudMergerTest {

    private val merger = CloudMerger()

    @Test
    fun `should update local item when cloud item is newer`() {
        val local = vaultBackup(items = listOf(item("1", updatedAt = time(1))))
        val cloud = vaultBackup(items = listOf(item("1", updatedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.items.toUpdate shouldContainExactly listOf(item("1", updatedAt = time(2)))
    }

    @Test
    fun `should not update local item when cloud item is older`() {
        val local = vaultBackup(items = listOf(item("1", updatedAt = time(5))))
        val cloud = vaultBackup(items = listOf(item("1", updatedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.items.toUpdate shouldBe emptyList()
    }

    @Test
    fun `should trash local item when cloud deleted item is newer`() {
        val local = vaultBackup(items = listOf(item("1", updatedAt = time(1))))
        val cloud = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(2))))

        val result = merger.merge(local, cloud)

        with(result.items.toDelete.first()) {
            id shouldBe "1"
            deleted shouldBe true
            deletedAt shouldBe time(2)
        }
    }

    @Test
    fun `should not trash local item when cloud deleted item is older`() {
        val local = vaultBackup(items = listOf(item("1", updatedAt = time(5))))
        val cloud = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.items.toDelete shouldBe emptyList()
    }

    @Test
    fun `should add cloud item when not present locally`() {
        val local = vaultBackup()
        val cloud = vaultBackup(items = listOf(item("1", updatedAt = time(1))))

        val result = merger.merge(local, cloud)

        result.items.toAdd shouldContainExactly listOf(item("1", updatedAt = time(1)))
    }

    @Test
    fun `should not add cloud item if deleted locally and older than deletion`() {
        val local = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(3))))
        val cloud = vaultBackup(items = listOf(item("1", updatedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.items.toAdd shouldBe emptyList()
    }

    @Test
    fun `should add cloud item if deleted locally but cloud is newer`() {
        val local = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(1))))
        val cloud = vaultBackup(items = listOf(item("1", updatedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.items.toAdd shouldContainExactly listOf(item("1", updatedAt = time(2)))
    }

    @Test
    fun `should remove cloud deleted item if item was restored locally`() {
        val local = vaultBackup(items = listOf(item("1", updatedAt = time(5))))
        val cloud = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.items.toAdd.map { it.id } shouldBe emptyList()
        result.items.toUpdate.map { it.id } shouldBe emptyList()
        result.items.toDelete.map { it.id } shouldBe emptyList()
        result.deletedItems.map { it.id } shouldBe emptyList()
    }

    @Test
    fun `should add cloud item and remove local deleted item if cloud item is newer`() {
        val local = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(2))))
        val cloud = vaultBackup(items = listOf(item("1", updatedAt = time(4))))

        val result = merger.merge(local, cloud)

        result.items.toAdd.map { it.id } shouldContainExactly listOf("1")
        result.deletedItems.map { it.id } shouldBe emptyList()
    }

    @Test
    fun `should merge deleted items from both sides and exclude added item ids`() {
        val local = vaultBackup(
            deletedItems = listOf(deletedItem("1", deletedAt = time(1)), deletedItem("2", deletedAt = time(2))),
        )
        val cloud = vaultBackup(
            deletedItems = listOf(deletedItem("3", deletedAt = time(3))),
            items = listOf(item("1", updatedAt = time(5))),
        )

        val result = merger.merge(local, cloud)

        result.deletedItems
            .filter { result.items.toAdd.map { it.id }.contains(it.id).not() }
            .sortedByDescending { it.deletedAt }
            .distinctBy { it.id }
            .map { it.id } shouldContainExactly listOf("3", "2")
    }

    @Test
    fun `should update local tag when cloud tag is newer`() {
        val local = vaultBackup(tags = listOf(tag("1", updatedAt = time(1))))
        val cloud = vaultBackup(tags = listOf(tag("1", updatedAt = time(3))))

        val result = merger.merge(local, cloud)

        result.tags.toUpdate.map { it.id } shouldContainExactly listOf("1")
        result.tags.toAdd shouldBe emptyList()
        result.tags.toDelete shouldBe emptyList()
    }

    @Test
    fun `should not update local tag when cloud tag is older`() {
        val local = vaultBackup(tags = listOf(tag("1", updatedAt = time(5))))
        val cloud = vaultBackup(tags = listOf(tag("1", updatedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.tags.toUpdate shouldBe emptyList()
        result.tags.toAdd shouldBe emptyList()
        result.tags.toDelete shouldBe emptyList()
    }

    @Test
    fun `should add cloud tag if not present locally`() {
        val local = vaultBackup()
        val cloud = vaultBackup(tags = listOf(tag("1", updatedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.tags.toAdd.map { it.id } shouldContainExactly listOf("1")
        result.tags.toUpdate shouldBe emptyList()
        result.tags.toDelete shouldBe emptyList()
    }

    @Test
    fun `should not add cloud tag if deleted locally and older than deletion`() {
        val local = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(5))))
        val cloud = vaultBackup(tags = listOf(tag("1", updatedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.tags.toAdd shouldBe emptyList()
        result.tags.toUpdate shouldBe emptyList()
        result.tags.toDelete shouldBe emptyList()
    }

    @Test
    fun `should add cloud tag if deleted locally but cloud is newer`() {
        val local = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(2))))
        val cloud = vaultBackup(tags = listOf(tag("1", updatedAt = time(4))))

        val result = merger.merge(local, cloud)

        result.tags.toAdd.map { it.id } shouldContainExactly listOf("1")
    }

    @Test
    fun `should delete local tag if deleted in cloud and deletion is newer`() {
        val local = vaultBackup(tags = listOf(tag("1", updatedAt = time(2))))
        val cloud = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(5))))

        val result = merger.merge(local, cloud)

        result.tags.toDelete.map { it.id } shouldContainExactly listOf("1")
    }

    @Test
    fun `should remove deleted tag from result if restored from cloud`() {
        val local = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(2))))
        val cloud = vaultBackup(tags = listOf(tag("1", updatedAt = time(4))))

        val result = merger.merge(local, cloud)

        result.deletedItems.any { it.id == "1" } shouldBe false
    }
}