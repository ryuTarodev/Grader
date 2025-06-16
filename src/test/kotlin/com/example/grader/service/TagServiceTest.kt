package com.example.grader.service

import com.example.grader.dto.TagDto
import com.example.grader.entity.Tag
import com.example.grader.error.BadRequestException
import com.example.grader.error.TagNotFoundException
import com.example.grader.repository.TagRepository
import com.example.grader.util.toTagDTO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import java.util.*

class TagServiceTest {

    private lateinit var tagRepository: TagRepository
    private lateinit var tagService: TagService

    @BeforeEach
    fun setup() {
        tagRepository = mock(TagRepository::class.java)
        tagService = TagService(tagRepository)
    }

    @Test
    fun `createTag should save and return TagDto`() {
        val tagName = "NewTag"
        val savedTag = Tag(id = 1L, name = tagName.trim())

        `when`(tagRepository.existsByName(tagName)).thenReturn(false)
        `when`(tagRepository.save(any())).thenReturn(savedTag)

        val result = tagService.createTag(tagName)

        assertEquals(savedTag.toTagDTO(), result)
    }

    @Test
    fun `createTag should throw if tag already exists`() {
        val tagName = "DuplicateTag"

        `when`(tagRepository.existsByName(tagName)).thenReturn(true)

        val exception = assertThrows(BadRequestException::class.java) {
            tagService.createTag(tagName)
        }

        assertEquals("Tag with name [$tagName] already exists", exception.message)
    }

    @Test
    fun `createTag should throw if name is blank`() {
        val exception = assertThrows(BadRequestException::class.java) {
            tagService.createTag("   ")
        }

        assertEquals("Tag name cannot be blank", exception.message)
    }

    @Test
    fun `getTags should return list of TagDto`() {
        val tags = listOf(Tag(id = 1, name = "Math"), Tag(id = 2, name = "Science"))

        `when`(tagRepository.findAll()).thenReturn(tags)

        val result = tagService.getTags()

        assertEquals(2, result.size)
        assertEquals("Math", result[0].name)
        assertEquals("Science", result[1].name)
    }

    @Test
    fun `getTagById should return TagDto`() {
        val tag = Tag(id = 1, name = "Physics")

        `when`(tagRepository.findById(any())).thenReturn(Optional.of(tag))

        val result = tagService.getTagById(1)

        assertEquals(tag.toTagDTO(), result)
    }

    @Test
    fun `getTagById should throw if tag not found`() {
        `when`(tagRepository.findById(any())).thenReturn(Optional.empty())

        val exception = assertThrows(TagNotFoundException::class.java) {
            tagService.getTagById(1)
        }

        assertEquals("Tag with ID 1 not found", exception.message)
    }

    @Test
    fun `updateTagById should update and return TagDto`() {
        val existing = Tag(id = 1L, name = "Old")
        val newName = "Updated"

        `when`(tagRepository.findById(any())).thenReturn(Optional.of(existing))
        `when`(tagRepository.existsByNameAndIdNot(newName, 1L)).thenReturn(false)
        `when`(tagRepository.save(any())).thenReturn(existing.apply { name = newName })

        val result = tagService.updateTagById(1L, newName)

        assertEquals(newName, result.name)
    }

    @Test
    fun `updateTagById should throw if new name exists for another tag`() {
        val existing = Tag(id = 1L, name = "Old")
        val newName = "Conflict"

        `when`(tagRepository.findById(any())).thenReturn(Optional.of(existing))
        `when`(tagRepository.existsByNameAndIdNot(newName, 1L)).thenReturn(true)

        val exception = assertThrows(BadRequestException::class.java) {
            tagService.updateTagById(1L, newName)
        }

        assertEquals("Tag with name [$newName] already exists", exception.message)
    }

    @Test
    fun `deleteTagById should delete tag`() {
        val tag = Tag(id = 1, name = "ToDelete")

        `when`(tagRepository.findById(any())).thenReturn(Optional.of(tag))
        doNothing().`when`(tagRepository).delete(tag)

        assertDoesNotThrow {
            tagService.deleteTagById(1)
        }

        verify(tagRepository).delete(tag)
    }

    @Test
    fun `deleteTagById should throw if tag not found`() {
        `when`(tagRepository.findById(any())).thenReturn(Optional.empty())

        val exception = assertThrows(TagNotFoundException::class.java) {
            tagService.deleteTagById(99L)
        }

        assertEquals("Tag with ID 99 not found", exception.message)
    }
}