package com.example.grader.service

import com.example.grader.dto.ProblemTagDto
import com.example.grader.entity.Problem
import com.example.grader.entity.ProblemTag
import com.example.grader.entity.Tag
import com.example.grader.error.*
import com.example.grader.repository.ProblemRepository
import com.example.grader.repository.ProblemTagRepository
import com.example.grader.repository.TagRepository
import com.example.grader.util.toProblemTagDTO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.*
import java.util.*

class ProblemTagServiceTest {

    private lateinit var problemRepository: ProblemRepository
    private lateinit var tagRepository: TagRepository
    private lateinit var problemTagRepository: ProblemTagRepository
    private lateinit var service: ProblemTagService

    private val problem = Problem(id = 1L, /* other fields */)
    private val tag = Tag(id = 1L, /* other fields */)
    private val problemTag = ProblemTag(id = 1L, problem = problem, tag = tag)

    @BeforeEach
    fun setUp() {
        problemRepository = mock()
        tagRepository = mock()
        problemTagRepository = mock()
        service = ProblemTagService(problemRepository, tagRepository, problemTagRepository)
    }

    @Test
    fun `createProblemTag success`() {
        // Mock findById to return Optional.of(entity)
        whenever(problemRepository.findById(1L)).thenReturn(Optional.of(problem))
        whenever(tagRepository.findById(1L)).thenReturn(Optional.of(tag))
        whenever(problemTagRepository.findByProblemIdAndTagId(1L, 1L)).thenReturn(null)
        whenever(problemTagRepository.save(any<ProblemTag>())).thenReturn(problemTag)

        val result = service.createProblemTag(1L, 1L)

        assertEquals(problemTag.id, result.id)
        verify(problemRepository).findById(1L)
        verify(tagRepository).findById(1L)
        verify(problemTagRepository).save(any())
    }

    @Test
    fun `createProblemTag throws ProblemNotFoundException if problem missing`() {
        // Mock findById to return empty Optional
        whenever(problemRepository.findById(anyLong())).thenReturn(Optional.empty())

        val ex = assertThrows(ProblemNotFoundException::class.java) {
            service.createProblemTag(1L, 1L)
        }
        assertTrue(ex.message!!.contains("Problem not found"))
    }

    @Test
    fun `createProblemTag throws TagNotFoundException if tag missing`() {
        whenever(problemRepository.findById(1L)).thenReturn(Optional.of(problem))
        whenever(tagRepository.findById(anyLong())).thenReturn(Optional.empty())

        val ex = assertThrows(TagNotFoundException::class.java) {
            service.createProblemTag(1L, 1L)
        }
        assertTrue(ex.message!!.contains("Tag not found"))
    }

    @Test
    fun `createProblemTag throws DuplicateException if problemTag exists`() {
        whenever(problemRepository.findById(1L)).thenReturn(Optional.of(problem))
        whenever(tagRepository.findById(1L)).thenReturn(Optional.of(tag))
        whenever(problemTagRepository.findByProblemIdAndTagId(1L, 1L)).thenReturn(problemTag)

        val ex = assertThrows(DuplicateException::class.java) {
            service.createProblemTag(1L, 1L)
        }
        assertTrue(ex.message!!.contains("already exists"))
    }

    @Test
    fun `getAllProblemTags returns mapped list`() {
        whenever(problemTagRepository.findAll()).thenReturn(listOf(problemTag))

        val result = service.getAllProblemTags()

        assertEquals(1, result.size)
    }

    @Test
    fun `getProblemTagsByProblemId success`() {
        whenever(problemRepository.findById(1L)).thenReturn(Optional.of(problem))
        whenever(problemTagRepository.findByProblemId(1L)).thenReturn(listOf(problemTag))

        val result = service.getProblemTagsByProblemId(1L)

        assertEquals(1, result.size)
    }

    @Test
    fun `getProblemTagsByProblemId throws ProblemNotFoundException if problem missing`() {
        whenever(problemRepository.findById(1L)).thenReturn(Optional.empty())

        val ex = assertThrows(ProblemNotFoundException::class.java) {
            service.getProblemTagsByProblemId(1L)
        }
        assertTrue(ex.message!!.contains("Problem not found"))
    }

    @Test
    fun `getProblemTagsByProblemId throws ProblemNotFoundException if no tags found`() {
        whenever(problemRepository.findById(1L)).thenReturn(Optional.of(problem))
        whenever(problemTagRepository.findByProblemId(1L)).thenReturn(null)

        val ex = assertThrows(ProblemNotFoundException::class.java) {
            service.getProblemTagsByProblemId(1L)
        }
        assertTrue(ex.message!!.contains("Tag not found"))
    }

    @Test
    fun `getProblemTagsByTagId success`() {
        whenever(tagRepository.findById(1L)).thenReturn(Optional.of(tag))
        whenever(problemTagRepository.findByTagId(1L)).thenReturn(listOf(problemTag))

        val result = service.getProblemTagsByTagId(1L)

        assertEquals(1, result.size)
    }

    @Test
    fun `getProblemTagsByTagId throws TagNotFoundException if tag missing`() {
        whenever(tagRepository.findById(1L)).thenReturn(Optional.empty())

        val ex = assertThrows(TagNotFoundException::class.java) {
            service.getProblemTagsByTagId(1L)
        }
        assertTrue(ex.message!!.contains("Tag not found"))
    }

    @Test
    fun `getProblemTagsByTagId throws TagNotFoundException if no tags found`() {
        whenever(tagRepository.findById(1L)).thenReturn(Optional.of(tag))
        whenever(problemTagRepository.findByTagId(1L)).thenReturn(null)

        val ex = assertThrows(TagNotFoundException::class.java) {
            service.getProblemTagsByTagId(1L)
        }
        assertTrue(ex.message!!.contains("Tag not found"))
    }

    @Test
    fun `updateProblemTag success`() {
        val newProblem = Problem(id = 2L)
        val newTag = Tag(id = 2L)
        val updatedProblemTag = ProblemTag(id = 1L, problem = newProblem, tag = newTag)

        whenever(problemTagRepository.findById(1L)).thenReturn(Optional.of(problemTag))
        whenever(problemRepository.findById(2L)).thenReturn(Optional.of(newProblem))
        whenever(tagRepository.findById(2L)).thenReturn(Optional.of(newTag))
        whenever(problemTagRepository.findByProblemIdAndTagId(2L, 2L)).thenReturn(null)
        whenever(problemTagRepository.save(any<ProblemTag>())).thenReturn(updatedProblemTag)

        val result = service.updateProblemTag(1L, 2L, 2L)

        assertEquals(2L, result.problemId)
        assertEquals(2L, result.tag)
    }

    @Test
    fun `updateProblemTag throws ProblemTagNotFoundException if missing`() {
        whenever(problemTagRepository.findById(1L)).thenReturn(Optional.empty())

        val ex = assertThrows(ProblemTagNotFoundException::class.java) {
            service.updateProblemTag(1L, 2L, 2L)
        }
        assertTrue(ex.message!!.contains("ProblemTag not found"))
    }

    @Test
    fun `updateProblemTag throws ProblemNotFoundException if new problem missing`() {
        whenever(problemTagRepository.findById(1L)).thenReturn(Optional.of(problemTag))
        whenever(problemRepository.findById(2L)).thenReturn(Optional.empty())

        val ex = assertThrows(ProblemNotFoundException::class.java) {
            service.updateProblemTag(1L, 2L, 2L)
        }
        assertTrue(ex.message!!.contains("Problem not found"))
    }

    @Test
    fun `updateProblemTag throws TagNotFoundException if new tag missing`() {
        whenever(problemTagRepository.findById(1L)).thenReturn(Optional.of(problemTag))
        whenever(problemRepository.findById(2L)).thenReturn(Optional.of(problem))
        whenever(tagRepository.findById(2L)).thenReturn(Optional.empty())

        val ex = assertThrows(TagNotFoundException::class.java) {
            service.updateProblemTag(1L, 2L, 2L)
        }
        assertTrue(ex.message!!.contains("Tag not found"))
    }

    @Test
    fun `updateProblemTag throws DuplicateException if duplicate found`() {
        val duplicate = ProblemTag(id = 99L, problem = problem, tag = tag)
        whenever(problemTagRepository.findById(1L)).thenReturn(Optional.of(problemTag))
        whenever(problemRepository.findById(2L)).thenReturn(Optional.of(problem))
        whenever(tagRepository.findById(2L)).thenReturn(Optional.of(tag))
        whenever(problemTagRepository.findByProblemIdAndTagId(2L, 2L)).thenReturn(duplicate)

        val ex = assertThrows(DuplicateException::class.java) {
            service.updateProblemTag(1L, 2L, 2L)
        }
        assertTrue(ex.message!!.contains("already exists"))
    }

    @Test
    fun `deleteProblemTag success`() {
        whenever(problemTagRepository.findById(1L)).thenReturn(Optional.of(problemTag))
        doNothing().whenever(problemTagRepository).delete(problemTag)

        service.deleteProblemTag(1L)

        verify(problemTagRepository).delete(problemTag)
    }

    @Test
    fun `deleteProblemTag throws ProblemTagNotFoundException if missing`() {
        whenever(problemTagRepository.findById(1L)).thenReturn(Optional.empty())

        val ex = assertThrows(ProblemTagNotFoundException::class.java) {
            service.deleteProblemTag(1L)
        }
        assertTrue(ex.message!!.contains("ProblemTag not found"))
    }

    @Test
    fun `deleteProblemTagByProblemIdAndTagId success`() {
        whenever(problemTagRepository.findByProblemIdAndTagId(1L, 1L)).thenReturn(problemTag)
        doNothing().whenever(problemTagRepository).delete(problemTag)

        service.deleteProblemTagByProblemIdAndTagId(1L, 1L)

        verify(problemTagRepository).delete(problemTag)
    }

    @Test
    fun `deleteProblemTagByProblemIdAndTagId throws ProblemTagNotFoundException if missing`() {
        whenever(problemTagRepository.findByProblemIdAndTagId(1L, 1L)).thenReturn(null)

        val ex = assertThrows(ProblemTagNotFoundException::class.java) {
            service.deleteProblemTagByProblemIdAndTagId(1L, 1L)
        }
        assertTrue(ex.message!!.contains("ProblemTag not found"))
    }

    @Test
    fun `addTagToProblem calls createProblemTag`() {
        val spyService = spy(service)
        doReturn(problemTag.toProblemTagDTO()).whenever(spyService).createProblemTag(1L, 1L)

        val result = spyService.addTagToProblem(1L, 1L)

        verify(spyService).createProblemTag(1L, 1L)
        assertEquals(problemTag.id, result.id)
    }

    @Test
    fun `removeTagFromProblem calls deleteProblemTagByProblemIdAndTagId`() {
        val spyService = spy(service)
        doNothing().whenever(spyService).deleteProblemTagByProblemIdAndTagId(1L, 1L)

        spyService.removeTagFromProblem(1L, 1L)

        verify(spyService).deleteProblemTagByProblemIdAndTagId(1L, 1L)
    }
}