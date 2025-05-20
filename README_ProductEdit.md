
# ProductEdit.java

Esta classe `ProductEdit` é uma ação de edição usada no módulo de produtos (`ModuleProduct`) de um sistema ERP desenvolvido em Java.

## Funcionalidades

- Permite editar informações de um produto, incluindo fabricante, modelo, tipo, status, GTIN, SKU, especificações e acessórios.
- Valida informações com verificação de unicidade e consistência (como SKU único, GTIN válido).
- Usa recursos como PictureEdit e upload de manuais PDF.
- Registra mudanças via `ActivityBuilder` para controle de alterações.
- Carrega informações de apoio para selects (tipos, categorias, fabricantes, acessórios).
- Trabalha com um formulário dinâmico com agrupamentos e ações de mudança (onChange).

## Estrutura

- `onWindowRequest`: monta a tela de edição com dados e estrutura visual.
- `onValidationRequest`: realiza validações completas dos dados.
- `onSaveRequest`: salva os dados e registra mudanças.

## Dependências

- Framework interno `firsti`, com uso de:
  - `EntityManagerWrapper`
  - `WindowBuilder`, `Select`, `InputText`, `PictureEdit`, etc.
  - `ActivityBuilder` e `ActionValidationBuilder`
- Classes do pacote `product`, como `Product`, `ProductType`, `Manufacturer`, `ProductAccessoryType`.

## Autor

Acacio Fagundes
