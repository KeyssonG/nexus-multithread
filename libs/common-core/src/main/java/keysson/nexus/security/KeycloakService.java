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
            }
        } catch (Exception e) {
            log.debug("keycloak_login_failed | username={} error={}", username, e.getMessage());
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
            log.warn("keycloak_user_exists_check_failed | username={} error={}", username, e.getMessage());
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
                userPayload.put("emailVerified", true);
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
                log.info("keycloak_user_created | userId={} username={}", userId, username);
                return userId;
            }
        } catch (Exception e) {
            log.error("keycloak_create_user_failed | username={} error={}", username, e.getMessage());
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

            log.info("keycloak_roles_assigned | userId={} roles={}", userId, roleNames);
        } catch (Exception e) {
            log.error("keycloak_assign_roles_failed | userId={} error={}", userId, e.getMessage());
        }
    }

    private String getAdminToken() {
        String url = serverUrl + "/realms/master/protocol/openid-connect/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", adminClientId);
        body.add("client_secret", adminSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        try {
            Map<String, Object> json = objectMapper.readValue(response.getBody(), Map.class);
            return (String) json.get("access_token");
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to parse admin token response", e);
        }
    }
}
