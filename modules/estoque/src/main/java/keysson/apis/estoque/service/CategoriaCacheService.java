package keysson.apis.estoque.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import keysson.apis.estoque.dto.response.CategoriaResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class CategoriaCacheService {

    private static final String KEY_PREFIX = "estoque:categorias:";
    private static final Duration TTL = Duration.ofMinutes(20);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CategoriaCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<CategoriaResponse> buscar(Long idEmpresa) {
        try {
            String json = redisTemplate.opsForValue().get(KEY_PREFIX + idEmpresa);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, new TypeReference<List<CategoriaResponse>>() {
            });
        } catch (Exception e) {
            log.warn("Erro ao ler categorias do cache: {}", e.getMessage());
            return null;
        }
    }

    public void salvar(Long idEmpresa, List<CategoriaResponse> categorias) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + idEmpresa, objectMapper.writeValueAsString(categorias), TTL);
        } catch (Exception e) {
            log.warn("Erro ao gravar categorias no cache: {}", e.getMessage());
        }
    }
}
