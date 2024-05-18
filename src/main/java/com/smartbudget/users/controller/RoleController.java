package com.smartbudget.users.controller;

import com.smartbudget.users.dto.CreateRoleRequest;
import com.smartbudget.users.dto.MessageResponse;
import com.smartbudget.users.dto.PermissionRequest;
import com.smartbudget.users.dto.UserRoleRequest;
import com.smartbudget.users.model.Permission;
import com.smartbudget.users.model.Role;
import com.smartbudget.users.model.User;
import com.smartbudget.users.repository.PermissionRepository;
import com.smartbudget.users.repository.RoleRepository;
import com.smartbudget.users.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@Tag(name = "Roles", description = "API для управления ролями и правами")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    @Operation(summary = "Добавить новую роль")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Роль успешно добавлена", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Роль уже существует", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<?> addRole(@RequestBody CreateRoleRequest createRoleRequest) {
        if (roleRepository.findByRoleName(createRoleRequest.getRole()) != null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Роль уже существует"));
        }

        Role role = new Role();
        role.setRoleName(createRoleRequest.getRole());
        roleRepository.save(role);
        return ResponseEntity.ok(new MessageResponse("Роль успешно добавлена"));
    }

    @DeleteMapping("/delete/{roleName}")
    @Operation(summary = "Удалить роль")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Роль успешно удалена", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Роль не найдена", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<?> deleteRole(@PathVariable String roleName) {
        Role role = roleRepository.findByRoleName(roleName);
        if (role == null) {
            return ResponseEntity.status(404).body(new MessageResponse("Роль не найдена"));
        }

        roleRepository.delete(role);
        return ResponseEntity.ok(new MessageResponse("Роль успешно удалена"));
    }

    @PostMapping("/assign-permission")
    @Operation(summary = "Присвоить право роли")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Право успешно присвоено роли", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Роль или право не найдены", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<?> assignPermission(@RequestBody PermissionRequest permissionRequest) {
        User user = userRepository.findByUsername(permissionRequest.getUsername());
        if (user == null) {
            return ResponseEntity.status(404).body(new MessageResponse("Пользователь не найден"));
        }

        Role role = roleRepository.findByRoleName(permissionRequest.getPermission());
        if (role == null) {
            return ResponseEntity.status(404).body(new MessageResponse("Роль не найдена"));
        }

        Permission permission = permissionRepository.findByPermission(permissionRequest.getPermission());
        if (permission == null) {
            return ResponseEntity.status(404).body(new MessageResponse("Право не найдено"));
        }

        role.getPermissions().add(permission);
        roleRepository.save(role);
        return ResponseEntity.ok(new MessageResponse("Право успешно присвоено роли"));
    }

    @PostMapping("/remove-permission")
    @Operation(summary = "Удалить право из роли")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Право успешно удалено из роли", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Роль или право не найдены", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<?> removePermission(@RequestBody PermissionRequest permissionRequest) {
        User user = userRepository.findByUsername(permissionRequest.getUsername());
        if (user == null) {
            return ResponseEntity.status(404).body(new MessageResponse("Пользователь не найден"));
        }

        Role role = roleRepository.findByRoleName(permissionRequest.getPermission());
        if (role == null) {
            return ResponseEntity.status(404).body(new MessageResponse("Роль не найдена"));
        }

        Permission permission = permissionRepository.findByPermission(permissionRequest.getPermission());
        if (permission == null) {
            return ResponseEntity.status(404).body(new MessageResponse("Право не найдено"));
        }

        role.getPermissions().remove(permission);
        roleRepository.save(role);
        return ResponseEntity.ok(new MessageResponse("Право успешно удалено из роли"));
    }
}
