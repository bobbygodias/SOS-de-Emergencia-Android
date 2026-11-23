# SOS de Emergência Android — SOS Toque

Repositório de desenvolvimento da Enterprise, de Bobby Dias e Andrew Vox.

**Escopo:** aplicativo aberto destinado a dispositivos Android compatíveis, não exclusivo de um fabricante, modelo ou aparelho pessoal. A versão 1.0.0 exige Android 8.0 (API 26) ou superior; o acionamento com tela apagada requer sensor de proximidade utilizável e permissões de execução em segundo plano. O Doogee S100 Pro aparece nos registros apenas como referência de validação física ainda pendente, e não como público-alvo exclusivo.

O código-fonte recuperado da versão **SOS Toque 1.0.0** está em [`sos-toque/`](sos-toque/). Inclui o aplicativo Java para Android, manifest, recursos, lógica de gestos, documentação, testes e script de compilação. O pacote original foi recuperado sem chave de assinatura ou dados pessoais.

## Como o acionamento funciona

O modo com **tela apagada e bloqueada** exige cinco ciclos de cobrir e descobrir o **sensor de proximidade superior**, seguidos de cinco segundos para cancelar a chamada. Isso **não** equivale a cinco toques capacitivos na tela apagada. A aplicação foi concebida para solicitar chamada a um contato pessoal previamente configurado, sem substituir o SOS nativo do Android.

## Estado de validação

- **37 verificações locais aprovadas** em 22/11/2026 para as classes `Numbers`, `TapSequence` e `ProximitySequence`, executadas com Java 17, sem chamadas telefônicas.
- O registro original de QA informa compilação e assinatura prévias, mas **não comprova funcionamento físico** no Doogee S100 Pro.
- **Sem teste de integração Android executado nesta importação**, sem chamadas reais, sem APK assinado publicado e sem garantia de confiabilidade para emergências.
- Antes de depender do programa, consultar [`VALIDACAO.md`](sos-toque/VALIDACAO.md) e testar com segurança no aparelho real e com um contato avisado. Preservar o SOS nativo como alternativa.

## Compilação e documentação

Leia [`README.md`](sos-toque/README.md), [`CONTINUIDADE.md`](sos-toque/CONTINUIDADE.md) e [`VALIDACAO.md`](sos-toque/VALIDACAO.md). O projeto original utiliza Android SDK Platform 35, Build Tools 35.0.0, Eclipse ECJ 3.37.0 e Java 17, com compilação por `bash sos-toque/build.sh`.

Teste isolado da lógica, sem SDK Android:

```bash
mkdir -p /tmp/sostoque-test
javac -encoding UTF-8 -d /tmp/sostoque-test \
  sos-toque/app/src/main/java/org/enterprise/sostoque/{Numbers,TapSequence,ProximitySequence}.java \
  sos-toque/tests/CoreTests.java
java -cp /tmp/sostoque-test org.enterprise.sostoque.CoreTests
```

## Proveniência e licença

Fonte: `SOS-Toque-1.0.0-fontes.zip`, recuperado da Biblioteca do projeto. SHA-256: `df3055555e6227116f29d33a67f1570479228641aaf7f5ca40ce47ddf023242f`. Os arquivos foram importados sem alterar o conteúdo. O arquivo `sos-toque/LICENSE` identifica a licença **MIT** original desses fontes; o `LICENSE` existente na raiz permanece **CC0 1.0** e não foi substituído. Esta diferença de escopo deve ser revisada antes de uma distribuição ampliada.

**Não inclua keystores, senhas, certificados privados, contatos reais nem APKs de produção no repositório.** A fonte recuperada já contém regras de exclusão no `.gitignore`.
