package keysson.nexus.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class KeycloakService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${keycloak.server-url:http://keycloak.keycloak.svc.cluster.local:8080}")
    private String serverUrl;

    @Value("${keycloak.realm:multithread}")
    private String realm;

    @Value("${keycloak.admin-client-id:nexus-api}")
    private String adminClientId;

    @Value("${keycloak.admin-secret:}")
    private String adminSecret;

    @Value("${keycloak.gestao-client-id:multithread-gestao}")
    private String gestaoClientId;

    @Value("${keycloak.portal-client-id:multithread-portal}")
    private String portalClientId;

    public static class KeycloakToken {
        @Getter private String accessToken;
        @Getter private String refreshToken;
        @Getter private long expiresAt;

        public KeycloakToken(String accessToken, String refreshToken, int expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresAt = System.currentTimeMillis() + (expiresIn * 1000L);
        }
    }

    public KeycloakToken attemptLogin(String username, String password, String clientId) {
        try {
            String url = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("username", username);
            body.add("password", password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> json = objectMapper.readValue(response.getBody(), Map.class);
                String accessToken = (String) json.get("access_token");
                String refreshToken = (String) json.get("refresh_token");
                int expiresIn = (int) json.get("expires_in");
                return new KeycloakToken(accessToken, refreshToken, expiresIn);
            } else {
                log.debug("keycloak_login_resposta_invalida | usuario={} status={}", username, response.getStatusCode());
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.debug("keycloak_login_rejeitado | usuario={} status={}", username, e.getStatusCode());
        } catch (Exception e) {
            log.debug("keycloak_falha_login | usuario={} erro={}", username, e.getMessage());
        }
        return null;
    }

    public boolean userExists(String username) {
        try {
            String adminToken = getAdminToken();
            String url = serverUrl + "/admin/realms/" + realm + "/users?username=" + username + "&exact=true";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<?> users = objectMapper.readValue(response.getBody(), List.class);
                return !users.isEmpty();
            }
        } catch (Exception e) {
            log.warn("keycloak_falha_verificar_usuario | usuario={} erro={}", username, e.getMessage());
        }
        return false;
    }

    public String findUserId(String username) {
        try {
            String adminToken = getAdminToken();
            String url = serverUrl + "/admin/realms/" + realm + "/users?username=" + username + "&exact=true";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> users = objectMapper.readValue(response.getBody(), List.class);
                if (!users.isEmpty()) {
                    return (String) users.get(0).get("id");
                }
            }
        } catch (Exception e) {
            log.warn("keycloak_falha_buscar_usuario | usuario={} erro={}", username, e.getMessage());
        }
        return null;
    }

    public boolean userHasRealmRoles(String userId) {
        try {
            String adminToken = getAdminToken();
            String url = serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<?> roles = objectMapper.readValue(response.getBody(), List.class);
                return !roles.isEmpty();
            }
        } catch (Exception e) {
            log.debug("keycloak_falha_verificar_roles | userId={} erro={}", userId, e.getMessage());
        }
        return false;
    }

    public String createUser(String username, String rawPassword, int companyId, UUID consumerId, String userType) {
        return createUser(username, rawPassword, companyId, consumerId, userType, null);
    }

    public String createUser(String username, String rawPassword, int companyId, UUID consumerId, String userType, String email) {
        try {
            String adminToken = getAdminToken();
            String url = serverUrl + "/admin/realms/" + realm + "/users";

            Map<String, Object> userPayload = new LinkedHashMap<>();
            userPayload.put("username", username);
            userPayload.put("enabled", true);
            userPayload.put("emailVerified", true);

            if (email != null && !email.isBlank()) {
                userPayload.put("email", email);
            } else {
                userPayload.put("email", username + "@placeholder.local");
            }

            Map<String, List<String>> attributes = new LinkedHashMap<>();
            attributes.put("companyId", List.of(String.valueOf(companyId)));
            attributes.put("consumerId", List.of(consumerId.toString()));
            attributes.put("userType", List.of(userType));
            userPayload.put("attributes", attributes);

            Map<String, Object> credential = new LinkedHashMap<>();
            credential.put("type", "password");
            credential.put("value", rawPassword);
            credential.put("temporary", false);
            userPayload.put("credentials", List.of(credential));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(userPayload, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                String location = response.getHeaders().getLocation().toString();
                String userId = location.substring(location.lastIndexOf('/') + 1);
                log.info("keycloak_usuario_criado | userId={} usuario={}", userId, username);
                return userId;
            }
        } catch (org.springframework.web.client.HttpClientErrorException.Conflict e) {
            log.info("keycloak_usuario_ja_existe | usuario={}", username);
            return findUserId(username);
        } catch (Exception e) {
            log.error("keycloak_falha_criar_usuario | usuario={} erro={}", username, e.getMessage());
        }
        return null;
    }

    public void assignRoles(String userId, List<String> roleNames) {
        try {
            String adminToken = getAdminToken();

            List<Map<String, Object>> rolesPayload = new ArrayList<>();
            for (String roleName : roleNames) {
                Map<String, Object> role = new LinkedHashMap<>();
                role.put("name", roleName);
                role.put("description", "");
                rolesPayload.add(role);
            }

            String url = serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(rolesPayload, headers);
            restTemplate.postForEntity(url, request, Void.class);

            log.info("keycloak_roles_atribuidas | userId={} roles={}", userId, roleNames);
        } catch (Exception e) {
            log.error("keycloak_falha_atribuir_roles | userId={} erro={}", userId, e.getMessage());
        }
    }

    public boolean updatePassword(String userId, String newPassword) {
        try {
            String adminToken = getAdminToken();
            String url = serverUrl + "/admin/realms/" + realm + "/users/" + userId;

            Map<String, Object> credential = new LinkedHashMap<>();
            credential.put("type", "password");
            credential.put("value", newPassword);
            credential.put("temporary", false);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("credentials", List.of(credential));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);

            log.info("keycloak_senha_atualizada | userId={}", userId);
            return true;
        } catch (Exception e) {
            log.error("keycloak_falha_atualizar_senha | userId={} erro={}", userId, e.getMessage());
            return false;
        }
    }

    public boolean updatePasswordByUsername(String username, String newPassword) {
        String userId = findUserId(username);
        if (userId == null) {
            log.debug("keycloak_usuario_nao_encontrado_para_reset | usuario={}", username);
            return false;
        }
        return updatePassword(userId, newPassword);
    }

    private String getAdminToken() {
        String url = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", adminClientId);
        body.add("client_secret", adminSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("keycloak_falha_token_admin | status={}", response.getStatusCode());
                throw new RuntimeException("Falha ao obter token admin: HTTP " + response.getStatusCode());
            }

            Map<String, Object> json = objectMapper.readValue(response.getBody(), Map.class);
            String accessToken = (String) json.get("access_token");
            if (accessToken == null) {
                log.error("keycloak_token_admin_vazio | resposta={}", response.getBody());
                throw new RuntimeException("Token admin vazio na resposta do Keycloak");
            }
            return accessToken;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("keycloak_falha_token_admin | status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao obter token admin: " + e.getStatusCode(), e);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("keycloak_falha_parse_token_admin | erro={}", e.getMessage());
            throw new RuntimeException("Falha ao processar resposta do token admin", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("keycloak_falha_conexao_token_admin | erro={}", e.getMessage());
            throw new RuntimeException("Falha de conexao ao obter token admin", e);
        }
    }
}
