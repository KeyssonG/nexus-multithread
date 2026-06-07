package keysson.apis.administration.controller;

import keysson.apis.administration.dto.request.*;
import keysson.apis.administration.dto.response.CompanyModuloDTO;
import keysson.apis.administration.dto.response.CompanyModuloResponseDTO;
import keysson.apis.administration.dto.response.CompanyResponseDTO;
import keysson.apis.administration.dto.response.CompanyStatusDTO;
import keysson.apis.administration.dto.response.DepartmentResponse;
import keysson.apis.administration.dto.response.ModuloResponseDTO;
import keysson.apis.administration.dto.response.PendingCompanyDTO;
import keysson.apis.administration.dto.response.UserModuloResponseDTO;
import keysson.apis.administration.exception.BusinessRuleException;
import keysson.apis.administration.service.AdministrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class AdministrationControllerImpl implements AdministrationController{

    private final AdministrationService administrationService;

    @Autowired
    public AdministrationControllerImpl(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    @Override
    public ResponseEntity<List<PendingCompanyDTO>> getPendingCompany(int accountNumber) throws BusinessRuleException {
        log.info("Recebendo requisição para buscar empresas pendentes para a conta: {}", accountNumber);
        return administrationService.pendingCompany(accountNumber);
    }

    @Override
    public void putStatusAccount(int numeroConta, ChangeAccountStatusRequest requestBody) throws BusinessRuleException {
        log.info("Recebendo requisição para alterar status da conta: {} para o novo status: {}", numeroConta, requestBody.getNewStatus());
        administrationService.changeStatus(requestBody.getNewStatus(), numeroConta);
    }

    @Override
    public CompanyStatusDTO getStatusCompanies() throws BusinessRuleException {
        log.info("Recebendo requisição para buscar o status geral das empresas.");
        return administrationService.StatusCompanies();
    }

    @Override
    public void postDepartment(@RequestBody CreateDepartmentRequest requestBody) throws BusinessRuleException {
        log.info("Recebendo requisição para cadastrar novo departamento: {}", requestBody.getNomeDepartamento());
        administrationService.registerDepartment(requestBody);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() throws BusinessRuleException {
        log.info("Recebendo requisição para buscar todos os departamentos.");
        return administrationService.searchAllDepartments();
    }

    @Override
    public void deleteDepartmentById(@RequestBody DeleteDepartmentRequest requestBody) throws BusinessRuleException {
        log.info("Recebendo requisição para deletar departamento por ID: {}", requestBody.getIdDepartamento());
        administrationService.deleteDepartment(requestBody);
    }

    @Override
    public List<ModuloResponseDTO> getModulos(Integer id) throws BusinessRuleException {
        log.info("Recebendo requisição para buscar módulos. ID opcional: {}", id);
        return administrationService.listAllModulos(id);
    }

    @Override
    public List<CompanyResponseDTO> getCompaniesByStatus(@PathVariable int statusId) throws BusinessRuleException {
        log.info("Recebendo requisição para buscar empresas pelo status: {}", statusId);
        return administrationService.getCompaniesByStatus(statusId);
    }

    @Override
    public void postLinkCompanyModulo(@RequestBody LinkCompanyModuloRequest requestBody) throws BusinessRuleException {
        log.info("Recebendo requisição para vincular empresa: {} ao módulo: {} com status: {}", 
                requestBody.getCompanyId(), requestBody.getModuloId(), requestBody.getStatus());
        administrationService.linkCompanyModulo(requestBody);
    }

    @Override
    public List<CompanyModuloResponseDTO> getCompanyModulos() throws BusinessRuleException {
        log.info("Recebendo requisição para buscar todos os vínculos de empresas e módulos.");
        return administrationService.getCompanyModulos();
    }

    @Override
    public void deleteLinkCompanyModulo(UnlinkCompanyModuloRequest requestBody) throws BusinessRuleException {
        log.info("Recebendo requisição para desvincular empresa: {} do módulo: {}", 
                requestBody.getCompanyId(), requestBody.getModuloId());
        administrationService.unlinkCompanyModulo(requestBody);
    }

    @Override
    public List<CompanyModuloDTO> getModulosByCompany() throws BusinessRuleException {
        log.info("Recebendo requisição para buscar módulos da empresa autenticada.");
        return administrationService.listModulosByCompany();
    }

    @Override
    public void postLinkUserModulo(@RequestBody LinkUserModuloRequest requestBody) throws BusinessRuleException {
        log.info("Recebendo requisição para vincular usuário: {} ao módulo: {}", requestBody.getUserId(), requestBody.getModuloId());
        administrationService.linkUserModulo(requestBody);
    }

    @Override
    public void deleteLinkUserModulo(UnlinkUserModuloRequest requestBody) throws BusinessRuleException {
        log.info("Recebendo requisição para desvincular usuário: {} do módulo: {}", 
                requestBody.getUserId(), requestBody.getModuloId());
        administrationService.unlinkUserModulo(requestBody);
    }

    @Override
    public List<UserModuloResponseDTO> getUserModulos() throws BusinessRuleException {
        log.info("Recebendo requisição para buscar vínculos de usuários e módulos.");
        return administrationService.getUserModulos();
    }

    @Override
    public List<CompanyModuloDTO> getModulosByCompanyPortal(Integer companyId) throws BusinessRuleException {
        log.info("Portal: Recebendo requisição para buscar módulos da empresa: {}", companyId);
        return administrationService.listModulosByCompanyPortal(companyId);
    }

    @Override
    public List<UserModuloResponseDTO> getUserModulosPortal(Integer companyId) throws BusinessRuleException {
        log.info("Portal: Recebendo requisição para buscar vínculos de usuários da empresa: {}", companyId);
        return administrationService.getUserModulosPortal(companyId);
    }

    @Override
    public List<DepartmentResponse> getAllDepartmentsPortal(Integer companyId) throws BusinessRuleException {
        log.info("Portal: Recebendo requisição para buscar departamentos da empresa: {}", companyId);
        return administrationService.searchAllDepartmentsPortal(companyId);
    }

    @Override
    public void postLinkUserModuloPortal(PortalLinkUserModuloRequest requestBody) throws BusinessRuleException {
        log.info("Portal: Recebendo requisição para vincular usuário: {} ao módulo: {} na empresa: {}",
                requestBody.getUserId(), requestBody.getModuloId(), requestBody.getCompanyId());
        administrationService.linkUserModuloPortal(requestBody);
    }

    @Override
    public void deleteLinkUserModuloPortal(PortalUnlinkUserModuloRequest requestBody) throws BusinessRuleException {
        log.info("Portal: Recebendo requisição para desvincular usuário: {} do módulo: {} na empresa: {}",
                requestBody.getUserId(), requestBody.getModuloId(), requestBody.getCompanyId());
        administrationService.unlinkUserModuloPortal(requestBody);
    }

}

