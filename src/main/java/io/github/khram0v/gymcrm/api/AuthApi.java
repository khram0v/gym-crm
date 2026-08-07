package io.github.khram0v.gymcrm.api;

import io.github.khram0v.gymcrm.dto.request.LoginRequest;
import io.github.khram0v.gymcrm.dto.response.ErrorResponse;
import io.github.khram0v.gymcrm.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Auth", description = "Authentication endpoints")
public interface AuthApi {

    @Operation(summary = "Login and obtain a JWT access token")
    @ApiResponse(responseCode = "200", description = "Authenticated")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "423", description = "Account temporarily locked due to too many failed attempts",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    LoginResponse login(@Valid LoginRequest request);
}
