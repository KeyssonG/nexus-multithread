package keysson.apis.estoque.exception.enums;

import keysson.nexus.common.exception.IErrorCode;
import org.springframework.http.HttpStatus;

public enum ErrorCode implements IErrorCode {
    ERROR_CADASTRAR_CENTRO("Erro ao cadastrar centro de armazenamento", HttpStatus.BAD_REQUEST),
    ERROR_BUSCAR_CENTRO("Centro de armazenamento não encontrado", HttpStatus.NOT_FOUND),
    ERROR_ATUALIZAR_CENTRO("Erro ao atualizar centro de armazenamento", HttpStatus.BAD_REQUEST),
    ERROR_DESATIVAR_CENTRO("Erro ao desativar centro de armazenamento", HttpStatus.BAD_REQUEST),
    ERROR_CADASTRAR_LOCALIZACAO("Erro ao cadastrar localização", HttpStatus.BAD_REQUEST),
    ERROR_BUSCAR_LOCALIZACAO("Localização não encontrada", HttpStatus.NOT_FOUND),
    ERROR_ATUALIZAR_LOCALIZACAO("Erro ao atualizar localização", HttpStatus.BAD_REQUEST),
    ERROR_VINCULAR_PRODUTO_LOCALIZACAO("Erro ao vincular produto à localização", HttpStatus.BAD_REQUEST),
    ERROR_ATUALIZAR_VINCULO_PRODUTO_LOCALIZACAO("Erro ao atualizar vínculo produto-localização", HttpStatus.BAD_REQUEST),
    ERROR_CADASTRAR_PRODUTO("Erro ao cadastrar produto", HttpStatus.BAD_REQUEST),
    ERROR_BUSCAR_PRODUTO("Produto não encontrado", HttpStatus.NOT_FOUND),
    ERROR_ATUALIZAR_PRODUTO("Erro ao atualizar produto", HttpStatus.BAD_REQUEST),
    ERROR_DESATIVAR_PRODUTO("Erro ao desativar produto", HttpStatus.BAD_REQUEST),
    ERROR_CADASTRAR_CATEGORIA("Erro ao cadastrar categoria", HttpStatus.BAD_REQUEST),
    ERROR_BUSCAR_CATEGORIA("Categoria não encontrada", HttpStatus.NOT_FOUND),
    ERROR_ATUALIZAR_CATEGORIA("Erro ao atualizar categoria", HttpStatus.BAD_REQUEST),
    ERROR_DESATIVAR_CATEGORIA("Erro ao desativar categoria", HttpStatus.BAD_REQUEST),
    ERROR_REGISTRAR_ENTRADA("Erro ao registrar entrada de estoque", HttpStatus.BAD_REQUEST),
    ERROR_REGISTRAR_SAIDA("Erro ao registrar saída de estoque", HttpStatus.BAD_REQUEST),
    ERROR_ESTOQUE_INSUFICIENTE("Estoque insuficiente para esta operação", HttpStatus.BAD_REQUEST),
    ERROR_LISTAR_MOVIMENTACOES("Erro ao listar movimentações", HttpStatus.BAD_REQUEST),
    ERROR_CADASTRAR_INVENTARIO("Erro ao iniciar inventário", HttpStatus.BAD_REQUEST),
    ERROR_REGISTRAR_CONTAGEM("Erro ao registrar contagem física", HttpStatus.BAD_REQUEST),
    ERROR_AJUSTAR_INVENTARIO("Erro ao aplicar ajuste de inventário", HttpStatus.BAD_REQUEST),
    ERROR_LISTAR_DIVERGENCIAS("Erro ao listar divergências", HttpStatus.BAD_REQUEST),
    ERROR_GERAR_RELATORIO("Erro ao gerar relatório", HttpStatus.BAD_REQUEST),
    ;

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
