package com.nixops.scraper.controller

import com.nixops.scraper.services.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

  @PostMapping fun createUser(@RequestParam name: String): Int = userService.createUser(name)

  @GetMapping fun getUsers(): List<String> = userService.getAllUsers()
}
