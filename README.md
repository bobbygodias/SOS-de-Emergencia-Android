# SOS de Emergência Android — SOS Toque

**SOS Toque** é um aplicativo Android de código aberto criado para facilitar o acionamento de uma chamada telefônica a um contato de confiança, inclusive quando a tela está apagada e bloqueada. A proposta é oferecer uma alternativa de acesso para situações em que pressionar o botão lateral ou navegar pelo telefone seja difícil, por exemplo por limitações de mobilidade.

O projeto é destinado ao público em geral, em **aparelhos Android compatíveis de qualquer fabricante**. Não é exclusivo de um modelo, pessoa ou dispositivo.

> **Aviso importante — projeto em desenvolvimento:** o aplicativo **não substitui os serviços oficiais de emergência, o SOS nativo do Android nem outros meios de pedir ajuda**. O funcionamento de sensores, serviços em segundo plano e chamadas varia conforme o aparelho e a versão do sistema. A lógica foi testada, mas o fluxo completo de emergência **ainda não foi validado em aparelhos físicos** nesta publicação. Não dependa deste aplicativo como único recurso de segurança. A distribuição de um APK ocorrerá após os testes necessários.

## O que o aplicativo oferece

- **Acionamento com tela apagada e bloqueada:** cinco movimentos de cobrir e descobrir o sensor de proximidade superior. Não são toques na tela desligada.
- **Confirmação antes da chamada:** após reconhecer os cinco gestos, uma contagem de cinco segundos permite cancelar um acionamento involuntário.
- **Contato principal configurável:** o aplicativo solicita ao Android uma ligação telefônica para um número pessoal previamente escolhido.
- **Teste sem ligação:** permite conferir o reconhecimento do sensor antes de habilitar o modo de chamadas.
- **Outras formas de acesso:** botão flutuante e área SOS na tela acesa, além de contatos de reserva para seleção manual.
- **Privacidade:** o código não solicita acesso à internet, localização, microfone ou SMS; não inclui anúncios, contas ou telemetria. Os contatos configurados ficam no armazenamento privado do aplicativo.

**Importante:** solicitar uma chamada ao Android não garante que ela será completada. Rede móvel, permissões, chip, políticas de bateria e hardware interferem no resultado. Para números curtos de serviços de emergência, o sistema pode exigir o uso e a confirmação no discador.

## Compatibilidade

A versão 1.0.0 requer **Android 8.0 (API 26) ou superior**. O modo de tela apagada depende de sensor de proximidade disponível e funcional, permissões adequadas e possibilidade de manter o serviço em execução. O aplicativo não exige root e não substitui o discador padrão.

A compatibilidade nominal com uma versão do Android não significa validação prática em todos os modelos.

## Estado atual do projeto

O código-fonte da versão **SOS Toque 1.0.0** está publicado em [`sos-toque/`](sos-toque/), incluindo aplicativo Java, recursos Android, lógica dos gestos, testes, documentação e script de compilação.

- **37 verificações locais de lógica aprovadas** em 22/11/2026, sem realizar chamadas.
- A documentação do projeto original registra compilação e assinatura anteriores, mas a importação para este repositório **não incluiu testes físicos de integração**.
- Ainda não há uma versão distribuída aqui como aplicativo validado para uso em emergências.

Consulte [a documentação técnica](sos-toque/README.md) e [os critérios de validação](sos-toque/VALIDACAO.md) para detalhes e limitações. A orientação é testar cada aparelho em condições controladas, com o contato previamente avisado, mantendo disponíveis os recursos nativos de emergência.

## Código-fonte e desenvolvimento


Para compilar, são utilizados Java 17, Android SDK Platform 35, Build Tools 35.0.0 e Eclipse ECJ 3.37.0; veja [as instruções de compilação](sos-toque/README.md). Há também [um documento de continuidade técnica](sos-toque/CONTINUIDADE.md).

**Licenciamento:** os fontes da aplicação em `sos-toque/` trazem licença MIT; o arquivo `LICENSE` na raiz deste repositório permanece CC0 1.0. Consulte os arquivos de licença aplicáveis antes de redistribuir.

Projeto desenvolvido por **Bobby Dias e Andrew Vox**, no âmbito da **Enterprise**.
