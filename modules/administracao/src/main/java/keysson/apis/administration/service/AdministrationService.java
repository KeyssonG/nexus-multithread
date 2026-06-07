package keysson.apis.administration.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import keysson.nexus.security.JwtUtil;
import keysson.apis.administration.dto.ChangeStatusEvent;
import keysson.apis.administration.dto.request.*;
import keysson.apis.administration.dto.response.CompanyModuloDTO;
import keysson.apis.administration.dto.response.CompanyModuloResponseDTO;
import keysson.apis.administration.dto.response.CompanyResponseDTO;
import keysson.apis.administration.dto.response.CompanyStatusDTO;
import keysson.apis.administration.dto.response.ModuloResponseDTO;
import keysson.apis.administration.dto.response.PendingCompanyDTO;
import keysson.apis.administration.dto.response.DepartmentResponse;
import keysson.apis.administration.dto.response.UserModuloResponseDTO;
import keysson.apis.administration.exception.BusinessRuleException;
import keysson.apis.administration.repository.AdministrationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import static keysson.apis.administration.exception.enums.ErrorCode.*;

@Service
@Slf4j
public class AdministrationService {

    @PersistenceContext
    private EntityManager entityManager;

    private AdministrationRepository administrationRepository;

    @Autowired
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HttpServletRequest httpRequest;

    @Autowired
    public AdministrationService(AdministrationRepository administrationRepository, RabbitTemplate rabbitTemplate, JwtUtil jwtUtil) {
        this.administrationRepository = administrationRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public ResponseEntity<List<PendingCompanyDTO>> pendingCompany(int conta) throws BusinessRuleException {
        log.info("Buscando empresas com status pendente para a conta: {}", conta);
        List<PendingCompanyDTO> empresasStatusPendente = administrationRepository.findPendingCompanies(conta);
        return ResponseEntity.ok(empresasStatusPendente);
    };

    public void changeStatus(int newStatus, int conta) throws BusinessRuleException {
        log.info("Alterando status da conta: {} para: {}", conta, newStatus);
        try {
            administrationRepository.newAccontStatus(newStatus, conta);

            ChangeStatusEvent event = new ChangeStatusEvent(
                    conta,
                    newStatus
            );
            log.info("Enviando evento de alteração de status para a fila alteraStatus.fila: {}", event);
            rabbitTemplate.convertAndSend("alteraStatus.fila", event);

        } catch (Exception ex) {
            log.error("Erro ao alterar o status da conta {}: {}", conta, ex.getMessage());
            throw new RuntimeException("Erro ao alterar o status.");
        }

    }

    public CompanyStatusDTO StatusCompanies() {
        log.info("Consultando status de todas as empresas.");
        return administrationRepository.findStatusCompany();
    }

    public void registerDepartment(CreateDepartmentRequest requestBody) throws BusinessRuleException {
        String token =(String)httpRequest.getAttribute("CleanJwt");
        Integer idEmpresa = jwtUtil.extractCompanyId(token);

        if (idEmpresa == null) {
            log.error("ID da empresa não encontrado no token JWT.");
            throw new IllegalArgumentException("ID da empresa não encontrado no token.");
        }

        if (requestBody.getNomeDepartamento() == null || requestBody.getNomeDepartamento().isEmpty()) {
            log.error("Tentativa de cadastrar departamento com nome vazio.");
            throw new IllegalArgumentException("O nome do departamento não pode ser vazio.");
        }

        log.info("Registrando novo departamento: '{}' para a empresa ID: {}", requestBody.getNomeDepartamento(), idEmpresa);
        try {
            administrationRepository.registerNewDepartment(idEmpresa, requestBody.getNomeDepartamento());
        } catch (Exception e) {
            log.error("Erro ao registrar departamento para a empresa {}: {}", idEmpresa, e.getMessage());
            throw new BusinessRuleException(ERROR_CADASTRO_DEPARTAMENTO);
        }
    }

    public List<DepartmentResponse> searchAllDepartments() throws BusinessRuleException {
        String token = (String) httpRequest.getAttribute("CleanJwt");
        Integer idEmpresa = jwtUtil.extractCompanyId(token);

        if (idEmpresa == null) {
            log.error("ID da empresa não encontrado no token JWT ao buscar departamentos.");
            throw new IllegalArgumentException("ID da empresa não encontrado no token.");
        }

        log.info("Buscando todos os departamentos da empresa ID: {}", idEmpresa);
        try {
            return administrationRepository.getDepartmentsByCompany(idEmpresa);
        } catch (Exception e) {
            log.error("Erro ao buscar departamentos da empresa {}: {}", idEmpresa, e.getMessage());
            throw new BusinessRuleException(ERROR_BUSCAR_DEPARTAMENTO);
        }
    }

    public void deleteDepartment(DeleteDepartmentRequest request) throws BusinessRuleException {
        log.info("Deletando departamento ID: {}", request.getIdDepartamento());
        try {
            administrationRepository.deleteDepartmentById(request.getIdDepartamento());
        } catch (Exception e) {
            log.error("Erro ao deletar departamento ID {}: {}", request.getIdDepartamento(), e.getMessage());
            throw new BusinessRuleException(ERROR_DELETAR_DEPARTAMENTO);
        }
    }

    public List<ModuloResponseDTO> listAllModulos(Integer id) throws BusinessRuleException {
        log.info("Listando módulos. Filtro ID: {}", id != null ? id : "Nenhum");
        try {
            return administrationRepository.getAllModulos(id);
        } catch (Exception e) {
            log.error("Erro ao listar módulos: {}", e.getMessage());
            throw new RuntimeException("Erro ao buscar módulos");
        }
    }

    public List<CompanyResponseDTO> getCompaniesByStatus(int statusId) {
        log.info("Buscando empresas com status ID: {}", statusId);
        return administrationRepository.findCompaniesByStatus(statusId);
    }

    public void linkCompanyModulo(LinkCompanyModuloRequest requestBody) throws BusinessRuleException {
        log.info("Vinculando empresa: {} ao módulo: {} com status: {}", 
                requestBody.getCompanyId(), requestBody.getModuloId(), requestBody.getStatus());
        try {
            administrationRepository.linkCompanyModulo(requestBody.getCompanyId(), requestBody.getModuloId(), requestBody.getStatus());
        } catch (Exception e) {
            log.error("Erro ao vincular empresa {} ao módulo {}: {}", 
                    requestBody.getCompanyId(), requestBody.getModuloId(), e.getMessage());
            throw new BusinessRuleException(ERROR_VINCULAR_EMPRESA_MODULO);
        }
    }

    public void unlinkCompanyModulo(UnlinkCompanyModuloRequest requestBody) throws BusinessRuleException {
        log.info("Desvinculando empresa: {} do módulo: {}", requestBody.getCompanyId(), requestBody.getModuloId());
        try {
            administrationRepository.unlinkCompanyModulo(requestBody.getCompanyId(), requestBody.getModuloId());
        } catch (Exception e) {
            log.error("Erro ao desvincular empresa {} do módulo {}: {}", 
                    requestBody.getCompanyId(), requestBody.getModuloId(), e.getMessage());
            throw new RuntimeException("Erro ao desvincular empresa do módulo");
        }
    }

    public List<CompanyModuloResponseDTO> getCompanyModulos() {
        log.info("Buscando todos os vínculos de empresas e módulos.");
        return administrationRepository.getCompanyModulos();
    }

    public List<CompanyModuloDTO> listModulosByCompany() {
        String token = (String) httpRequest.getAttribute("CleanJwt");
        Integer idEmpresa = jwtUtil.extractCompanyId(token);

        if (idEmpresa == null) {
            log.error("ID da empresa não encontrado no token JWT ao listar módulos.");
            throw new IllegalArgumentException("ID da empresa não encontrado no token.");
        }

        log.info("Listando módulos para a empresa ID: {}", idEmpresa);
        return administrationRepository.getModulosByCompanyId(idEmpresa);
    }

    public void linkUserModulo(LinkUserModuloRequest requestBody) throws BusinessRuleException {
        String token = (String) httpRequest.getAttribute("CleanJwt");
        Integer idEmpresa = jwtUtil.extractCompanyId(token);

        if (idEmpresa == null) {
            log.error("ID da empresa não encontrado no token JWT ao vincular usuário ao módulo.");
            throw new IllegalArgumentException("ID da empresa não encontrado no token.");
        }

        log.info("Vinculando usuário: {} ao módulo: {} na empresa ID: {}", 
                requestBody.getUserId(), requestBody.getModuloId(), idEmpresa);
        try {
            administrationRepository.linkUserModulo(requestBody.getUserId(), idEmpresa, requestBody.getModuloId());
        } catch (Exception e) {
            log.error("Erro ao vincular usuário {} ao módulo {} na empresa {}: {}", 
                    requestBody.getUserId(), requestBody.getModuloId(), idEmpresa, e.getMessage());
            throw new BusinessRuleException(ERROR_VINCULAR_USUARIO_MODULO);
        }
    }

    public void unlinkUserModulo(UnlinkUserModuloRequest requestBody) throws BusinessRuleException {
        String token = (String) httpRequest.getAttribute("CleanJwt");
        Integer idEmpresa = jwtUtil.extractCompanyId(token);

        if (idEmpresa == null) {
            log.error("ID da empresa não encontrado no token JWT ao desvincular usuário do módulo.");
            throw new IllegalArgumentException("ID da empresa não encontrado no token.");
        }

        log.info("Desvinculando usuário: {} do módulo: {} na empresa ID: {}", 
                requestBody.getUserId(), requestBody.getModuloId(), idEmpresa);
        try {
            administrationRepository.unlinkUserModulo(requestBody.getUserId(), idEmpresa, requestBody.getModuloId());
        } catch (Exception e) {
            log.error("Erro ao desvincular usuário {} do módulo {} na empresa {}: {}", 
                    requestBody.getUserId(), requestBody.getModuloId(), idEmpresa, e.getMessage());
            throw new RuntimeException("Erro ao desvincular usuário do módulo");
        }
    }

    public List<UserModuloResponseDTO> getUserModulos() throws BusinessRuleException {
        String token = (String) httpRequest.getAttribute("CleanJwt");
        Integer idEmpresa = jwtUtil.extractCompanyId(token);

        if (idEmpresa == null) {
            log.error("ID da empresa não encontrado no token JWT ao buscar vínculos de usuários e módulos.");
            throw new IllegalArgumentException("ID da empresa não encontrado no token.");
        }

        log.info("Buscando todos os vínculos de usuários e módulos para a empresa ID: {}", idEmpresa);
        try {
            return administrationRepository.getUserModulos(idEmpresa);
        } catch (Exception e) {
            log.error("Erro ao buscar vínculos de usuários e módulos para a empresa {}: {}", idEmpresa, e.getMessage());
            throw new BusinessRuleException(ERROR_BUSCAR_DEPARTAMENTO);
        }
    }

    public List<CompanyModuloDTO> listModulosByCompanyPortal(Integer companyId) {
        log.info("Portal: Listando módulos para a empresa ID: {}", companyId);
        return administrationRepository.getModulosByCompanyId(companyId);
    }

    public List<UserModuloResponseDTO> getUserModulosPortal(Integer companyId) throws BusinessRuleException {
        log.info("Portal: Buscando todos os vínculos de usuários e módulos para a empresa ID: {}", companyId);
        try {
            return administrationRepository.getUserModulos(companyId);
        } catch (Exception e) {
            log.error("Portal: Erro ao buscar vínculos para a empresa {}: {}", companyId, e.getMessage());
            throw new BusinessRuleException(ERROR_BUSCAR_DEPARTAMENTO);
        }
    }

    public List<DepartmentResponse> searchAllDepartmentsPortal(Integer companyId) throws BusinessRuleException {
        log.info("Portal: Buscando todos os departamentos da empresa ID: {}", companyId);
        try {
            return administrationRepository.getDepartmentsByCompany(companyId);
        } catch (Exception e) {
            log.error("Portal: Erro ao buscar departamentos da empresa {}: {}", companyId, e.getMessage());
            throw new BusinessRuleException(ERROR_BUSCAR_DEPARTAMENTO);
        }
    }

    public void linkUserModuloPortal(PortalLinkUserModuloRequest requestBody) throws BusinessRuleException {
        log.info("Portal: Vinculando usuário: {} ao módulo: {} na empresa ID: {}", 
                requestBody.getUserId(), requestBody.getModuloId(), requestBody.getCompanyId());
        try {
            administrationRepository.linkUserModulo(requestBody.getUserId(), requestBody.getCompanyId(), requestBody.getModuloId());
        } catch (Exception e) {
            log.error("Portal: Erro ao vincular usuário {} na empresa {}: {}", 
                    requestBody.getUserId(), requestBody.getCompanyId(), e.getMessage());
            throw new BusinessRuleException(ERROR_VINCULAR_USUARIO_MODULO);
        }
    }

    public void unlinkUserModuloPortal(PortalUnlinkUserModuloRequest requestBody) throws BusinessRuleException {
        log.info("Portal: Desvinculando usuário: {} do módulo: {} na empresa ID: {}", 
                requestBody.getUserId(), requestBody.getModuloId(), requestBody.getCompanyId());
        try {
            administrationRepository.unlinkUserModulo(requestBody.getUserId(), requestBody.getCompanyId(), requestBody.getModuloId());
        } catch (Exception e) {
            log.error("Portal: Erro ao desvincular usuário {} na empresa {}: {}", 
                    requestBody.getUserId(), requestBody.getCompanyId(), e.getMessage());
            throw new RuntimeException("Erro ao desvincular usuário do módulo");
        }
    }

}
