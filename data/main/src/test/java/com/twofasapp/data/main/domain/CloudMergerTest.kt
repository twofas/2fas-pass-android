package com.twofasapp.data.main.domain

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.Test

class CloudMergerTest {

    private val merger = CloudMerger()

    @Test
    fun `should update local login when cloud login is newer`() {
        val local = vaultBackup(logins = listOf(login("1", updatedAt = time(1))))
        val cloud = vaultBackup(logins = listOf(login("1", updatedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.logins.toUpdate shouldContainExactly listOf(login("1", updatedAt = time(2)))
    }

    @Test
    fun `should not update local login when cloud login is older`() {
        val local = vaultBackup(logins = listOf(login("1", updatedAt = time(5))))
        val cloud = vaultBackup(logins = listOf(login("1", updatedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.logins.toUpdate shouldBe emptyList()
    }

    @Test
    fun `should trash local login when cloud deleted item is newer`() {
        val local = vaultBackup(logins = listOf(login("1", updatedAt = time(1))))
        val cloud = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(2))))

        val result = merger.merge(local, cloud)

        with(result.logins.toDelete.first()) {
            id shouldBe "1"
            deleted shouldBe true
            deletedAt shouldBe time(2)
        }
    }

    @Test
    fun `should not trash local login when cloud deleted item is older`() {
        val local = vaultBackup(logins = listOf(login("1", updatedAt = time(5))))
        val cloud = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.logins.toDelete shouldBe emptyList()
    }

    @Test
    fun `should add cloud login when not present locally`() {
        val local = vaultBackup()
        val cloud = vaultBackup(logins = listOf(login("1", updatedAt = time(1))))

        val result = merger.merge(local, cloud)

        result.logins.toAdd shouldContainExactly listOf(login("1", updatedAt = time(1)))
    }

    @Test
    fun `should not add cloud login if deleted locally and older than deletion`() {
        val local = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(3))))
        val cloud = vaultBackup(logins = listOf(login("1", updatedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.logins.toAdd shouldBe emptyList()
    }

    @Test
    fun `should add cloud login if deleted locally but cloud is newer`() {
        val local = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(1))))
        val cloud = vaultBackup(logins = listOf(login("1", updatedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.logins.toAdd shouldContainExactly listOf(login("1", updatedAt = time(2)))
    }

    @Test
    fun `should remove cloud deleted item if login was restored locally`() {
        val local = vaultBackup(logins = listOf(login("1", updatedAt = time(5))))
        val cloud = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(2))))

        val result = merger.merge(local, cloud)

        result.logins.toAdd.map { it.id } shouldBe emptyList()
        result.logins.toUpdate.map { it.id } shouldBe emptyList()
        result.logins.toDelete.map { it.id } shouldBe emptyList()
        result.deletedItems.map { it.id } shouldBe emptyList()
    }

    @Test
    fun `should add cloud login and remove local deleted item if cloud login is newer`() {
        val local = vaultBackup(deletedItems = listOf(deletedItem("1", deletedAt = time(2))))
        val cloud = vaultBackup(logins = listOf(login("1", updatedAt = time(4))))

        val result = merger.merge(local, cloud)

        result.logins.toAdd.map { it.id } shouldContainExactly listOf("1")
        result.deletedItems.map { it.id } shouldBe emptyList()
    }

    @Test
    fun `should merge deleted items from both sides and exclude added login ids`() {
        val local = vaultBackup(
            deletedItems = listOf(deletedItem("1", deletedAt = time(1)), deletedItem("2", deletedAt = time(2))),
        )
        val cloud = vaultBackup(
            deletedItems = listOf(deletedItem("3", deletedAt = time(3))),
            logins = listOf(login("1", updatedAt = time(5))),
        )

        val result = merger.merge(local, cloud)

        result.deletedItems
            .filter { result.logins.toAdd.map { it.id }.contains(it.id).not() }
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