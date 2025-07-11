package com.example.grader.util

import com.example.grader.dto.*
import com.example.grader.entity.*


fun AppUser.toAppUserDTO(): AppUserDto {
    return AppUserDto(
        id = this.id,
        username = this.appUsername,
        profilePicture = this.profilePicture
    )
}
fun Problem.toProblemDTO(): ProblemDto {
    return ProblemDto(
        id = this.id,
        title = this.title,
        difficulty = this.difficulty,
        pdf = this.pdf
    )
}
fun Tag.toTagDTO(): TagDto {
    return TagDto(
        id = this.id,
        name = this.name
    )
}

fun ProblemTag.toProblemTagDTO(): ProblemTagDto {
    return ProblemTagDto(
        id = this.id,
        problemId = this.problem.id,
        tag = this.tag.id
    )
}
fun TestCase.toTestCaseDTO(): TestCaseDto {
    return TestCaseDto(
        id = this.id,
        problemId = this.problem.id,
        input = this.input,
        output = this.output,
        type = this.type
    )
}

fun Submission.toSubmissionDTO(): SubmissionDto {
    return SubmissionDto(
        id = this.id,
        appUserId = this.appUser.id,
        problemId = this.problem.id,
        code = this.code,
        language = this.language,
        score = this.score,
        status = this.status,
        submittedAt = this.submittedAt
    )
}

fun mapUserListEntityToUserListDTO(appUsers: List<AppUser>): List<AppUserDto> {
    return appUsers.map { it.toAppUserDTO() }
}

fun mapProblemListEntityToProblemListDTO(problems: List<Problem>): List<ProblemDto>{
    return problems.map { it.toProblemDTO() }
}

fun mapTagListEntityToTagListDTO(tags: List<Tag>): List<TagDto>{
    return tags.map { it.toTagDTO() }
}

fun mapProblemTagListEntityToProblemTagListDTO(problemTags: List<ProblemTag>): List<ProblemTagDto>{
    return problemTags.map { it.toProblemTagDTO() }
}

fun mapTestCaseListEntityToTestCaseListDTO(testcases: List<TestCase>): List<TestCaseDto>{
    return testcases.map { it.toTestCaseDTO() }
}

fun mapSubmissionListEntityToSubmissionListDTO(submissions: List<Submission>): List<SubmissionDto>{
    return submissions.map { it.toSubmissionDTO() }
}
