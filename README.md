# purchase-request-edit-action

Ação de edição de Requisição de Compra com validações específicas e controle de permissão para o requisitante no status PENDING.

## Funcionalidades

- Apenas o requisitante da requisição pode editar.
- Edição permitida apenas enquanto o status for `PENDING`.
- Campos editáveis: empresa, depósito, tipo, produto, quantidade e descrição.
- Validações com `ActionValidationBuilder`.
- Registro de alterações com `ActivityBuilder`.

## Tecnologias

- Java
- Jakarta Persistence (JPA)
- Framework Firsti (interno)

## Como usar

1. Certifique-se de que os campos no banco estão com `updatable = true`.
2. Utilize a classe `PurchaseRequestEdit` para expor a tela de edição.
3. Inclua os botões de edição na lista com validação de perfil e status.

## Autor

Gerado com auxílio do ChatGPT para projeto interno Firsti.
