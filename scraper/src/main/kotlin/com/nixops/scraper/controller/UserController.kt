package com.nixops.scraper.controller

import com.nixops.scraper.model.User
import com.nixops.scraper.repository.UserRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val userRepository: UserRepository) {

    @GetMapping
    fun getAll(): List<User> = userRepository.findAll()

    @PostMapping
    fun create(@RequestBody user: User): User = userRepository.save(user)
}
