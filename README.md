# SOS de Emergência Android — SOS Toque

Repositório de desenvolvimento da Enterprise, de Bobby Dias e Andrew Vox.

**Escopo:** aplicativo aberto destinado a dispositivos Android compatíveis, não exclusivo de um fabricante, modelo ou aparelho pessoal. A versão 1.0.0 exige Android 8.0 (API 26) ou superior; o acionamento com tela apagada requer sensor de proximidade utilizável e permissões de execução em segundo plano. O aparelho que aparece nos registros é apenas como referência de validação física ainda pendente, e não como público-alvo exclusivo.

O código-fonte recuperado da versão **SOS Toque 1.0.0** está em [`sos-toque/`](sos-toque/). Inclui o aplicativo Java para Android, manifest, recursos, lógica de gestos, documentação, testes e script de compilação. O pacote original foi recuperado sem chave de assinatura ou dados pessoais.

## Como o acionamento funciona

O modo com **tela apagada e bloqueada** exige cinco ciclos de cobrir e descobrir o **sensor de proximidade superior**, seguidos de cinco segundos para cancelar a chamada. Isso **não** equivale a cinco toques capacitivos na tela apagada. A aplicação foi concebida para solicitar chamada a um contato pessoal previamente configurado, sem substituir o SOS nativo do Android.

## Estado de validação

- **37 verificações locais aprovadas** em 22/11/2026 para as classes `Numbers`, `TapSequence` e `ProximitySequence`, executadas com Java 17, sem chamadas telefônicas.
- O registro original de QA informa compilação e assinatura prévias, mas **não comprova funcionamento físico** no estado atual de desenvolvimento.
- **Sem teste de integração Android executado nesta importação**.
- Antes de depender do programa, consultar [`VALIDACAO.md`](sos-toque/VALIDACAO.md) e testar com segurança no aparelho real e com um contato avisado. Distribuiremos assim que todos os testes forem concluídos.

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
