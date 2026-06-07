package keysson.apis.administration.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/administracao")
public interface AdministrationController {

    @GetMapping("/empresa/pendente/")
    @Operation(
            summary = "Busca empresas com status pendente.",
            description = "Endpoint para verificar empresas pendentes"
    )
    ResponseEntity<List<PendingCompanyDTO>> getPendingCompany(
            @RequestParam(required = false, defaultValue = "0") int numeroConta
    ) throws BusinessRuleException;

    @PutMapping("/status/conta")
    @Operation(
            summary = "Altera o Status da Conta.",
            description = "Endpoint para atualizar o Status da Conta"
    )
    void putStatusAccount(
            @RequestParam(required = false, defaultValue = "0") int numeroConta,
            @RequestBody ChangeAccountStatusRequest requestBody
    ) throws BusinessRuleException;


    @GetMapping("/empresa/status")
    @Operation(
            summary = "Busca empresas com status pendente.",
            description = "Endpoint para verificar empresas pendentes"
    )
    CompanyStatusDTO getStatusCompanies(
    ) throws BusinessRuleException;

    @GetMapping("/empresa/status/{statusId}")
    @Operation(
            summary = "Busca empresas por status.",
            description = "Endpoint para buscar ID e Nome das empresas filtradas por status"
    )
    List<CompanyResponseDTO> getCompaniesByStatus(
            @PathVariable int statusId
    ) throws BusinessRuleException;

    @PostMapping("/departamento")
    @Operation(
            summary = "Cria um novo departamento.",
            description = "Endpoint para criar um novo departamento"
    )
    void postDepartment(
            @RequestBody CreateDepartmentRequest requestBody
    ) throws BusinessRuleException;

    @GetMapping("/departamento")
    @Operation(
            summary = "Busca todos os departamentos por empresa.",
            description = "Endpoint para buscar todos os departamentos"
    )
    List<DepartmentResponse> getAllDepartments(
    ) throws BusinessRuleException;

    @DeleteMapping("/departamento")
    @Operation(
            summary = "Deleta um departamento por ID.",
            description = "Endpoint para deletar um departamento por ID"
    )
    void deleteDepartmentById(
            @RequestBody DeleteDepartmentRequest requestBody
    ) throws BusinessRuleException;

    @GetMapping("/modulos")
    @Operation(
            summary = "Busca todos os módulos disponíveis.",
            description = "Endpoint para consultar módulos de serviço"
    )
    List<ModuloResponseDTO> getModulos(
            @RequestParam(required = false) Integer id
    ) throws BusinessRuleException;

    @PostMapping("/empresa/modulo")
    @Operation(
            summary = "Vincula uma empresa a um módulo.",
            description = "Endpoint para vincular uma empresa a um módulo de serviço"
    )
    void postLinkCompanyModulo(
            @RequestBody LinkCompanyModuloRequest requestBody
    ) throws BusinessRuleException;

    @GetMapping("/empresa/modulo")
    @Operation(
            summary = "Busca todos os vínculos de empresas e módulos.",
            description = "Endpoint para consultar empresas vinculadas a módulos"
    )
    List<CompanyModuloResponseDTO> getCompanyModulos(
    ) throws BusinessRuleException;

    @DeleteMapping("/empresa/modulo")
    @Operation(
            summary = "Remove o vínculo de uma empresa com um módulo.",
            description = "Endpoint para desvincular uma empresa de um módulo de serviço. Remove também todos os usuários da empresa vinculados a este módulo."
    )
    void deleteLinkCompanyModulo(
            @RequestBody UnlinkCompanyModuloRequest requestBody
    ) throws BusinessRuleException;

    @GetMapping("/empresa/modulos")
    @Operation(
            summary = "Busca módulos por empresa.",
            description = "Endpoint para buscar os módulos que a empresa autenticada possui"
    )
    List<CompanyModuloDTO> getModulosByCompany(
    ) throws BusinessRuleException;

    @PostMapping("/usuario/modulo")
    @Operation(
            summary = "Vincula um usuário a um módulo.",
            description = "Endpoint para vincular um usuário a um módulo de serviço"
    )
    void postLinkUserModulo(
            @RequestBody LinkUserModuloRequest requestBody
    ) throws BusinessRuleException;

    @DeleteMapping("/usuario/modulo")
    @Operation(
            summary = "Remove o vínculo de um usuário com um módulo.",
            description = "Endpoint para desvincular um usuário de um módulo de serviço"
    )
    void deleteLinkUserModulo(
            @RequestBody UnlinkUserModuloRequest requestBody
    ) throws BusinessRuleException;

    @GetMapping("/usuario/modulo")
    @Operation(
            summary = "Busca todos os vínculos de usuários e módulos.",
            description = "Endpoint para consultar usuários vinculados a módulos"
    )
    List<UserModuloResponseDTO> getUserModulos(
    ) throws BusinessRuleException;

    // Novos endpoints para o Portal de Administração
    @GetMapping("/portal/empresa/modulos")
    @Operation(summary = "Busca módulos por empresa (Portal).")
    List<CompanyModuloDTO> getModulosByCompanyPortal(@RequestParam Integer companyId) throws BusinessRuleException;

    @GetMapping("/portal/usuario/modulo")
    @Operation(summary = "Busca vínculos de usuários por empresa (Portal).")
    List<UserModuloResponseDTO> getUserModulosPortal(@RequestParam Integer companyId) throws BusinessRuleException;

    @GetMapping("/portal/departamento")
    @Operation(summary = "Busca departamentos por empresa (Portal).")
    List<DepartmentResponse> getAllDepartmentsPortal(@RequestParam Integer companyId) throws BusinessRuleException;

    @PostMapping("/portal/usuario/modulo")
    @Operation(summary = "Vincula usuário a módulo informando a empresa (Portal).")
    void postLinkUserModuloPortal(@RequestBody PortalLinkUserModuloRequest requestBody) throws BusinessRuleException;

    @DeleteMapping("/portal/usuario/modulo")
    @Operation(summary = "Desvincula usuário de módulo informando a empresa (Portal).")
    void deleteLinkUserModuloPortal(@RequestBody PortalUnlinkUserModuloRequest requestBody) throws BusinessRuleException;
}
