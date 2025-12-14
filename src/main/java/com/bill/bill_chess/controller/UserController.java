package com.bill.bill_chess.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bill.bill_chess.controller.dto.UserResponseDto;
import com.bill.bill_chess.persistence.User;
import com.bill.bill_chess.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ResponseStatus;

import static com.bill.bill_chess.infra.swagger.OpenApiConstants.SECURITY_SCHEME_NAME;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "User", description = "User API")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "get all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "get user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public UserResponseDto getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "get user by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(OK)
    public UserResponseDto getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @PostMapping("/{id}/add-game/{idGame}")
    @Operation(summary = "add game to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(OK)
    public UserResponseDto updateAddGame(@PathVariable String id, @PathVariable @NotEmpty String idGame) {
        return userService.updateAddGame(id, idGame);
    }

    @PostMapping("/{id}/remove-game/{idGame}")
    @Operation(summary = "remove game from user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(OK)
    public UserResponseDto updateRemoveGame(@PathVariable String id, @PathVariable @NotEmpty String idGame) {
        return userService.updateRemoveGame(id, idGame);
    }

    @PostMapping("/{id}/add-role/{role}")
    @Operation(summary = "add role to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public UserResponseDto updateAddRole(@PathVariable String id, @PathVariable @NotEmpty User.Role role) {
        return userService.updateAddRole(id, role);
    }

    @PostMapping("/{id}/remove-role/{role}")
    @Operation(summary = "remove role from user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public UserResponseDto updateRemoveRole(@PathVariable String id, @PathVariable @NotEmpty User.Role role) {
        return userService.updateRemoveRole(id, role);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "deactivate user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deactivated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public void deactivate(@PathVariable String id) {
        userService.deactivate(id);
    }

    @DeleteMapping("/{id}/delete")
    @Operation(summary = "delete user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }

}
