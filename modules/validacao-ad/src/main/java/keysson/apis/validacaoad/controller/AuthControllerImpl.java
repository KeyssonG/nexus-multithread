package keysson.apis.validacaoad.controller;

import keysson.apis.validacaoad.dto.request.ConfirmResetPassword;
import keysson.apis.validacaoad.dto.request.LoginRequest;
import keysson.apis.validacaoad.dto.request.RequestResetPassword;
import keysson.apis.validacaoad.dto.request.RequestUpdatePassword;
import keysson.apis.validacaoad.dto.response.LoginResponse;
import keysson.apis.validacaoad.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController("validacaoADAuthController")
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController{

    private final AuthService authService;


    @Override
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) throws SQLException {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Override
    public void updatePassword(@RequestBody RequestUpdatePassword request, String token) throws SQLException {
        authService.updatePasswordUser(request);
    }

    @Override
    public ResponseEntity<Void> requestPasswordReset(@RequestBody RequestResetPassword request) throws SQLException {
        authService.requestPasswordChange(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> confirmPasswordReset(@RequestBody ConfirmResetPassword request) throws SQLException {
        authService.validatePasswordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
