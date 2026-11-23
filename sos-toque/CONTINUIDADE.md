# Continuidade — SOS Toque 1.0.0

Objetivo: Bobby precisa acionar um contato pessoal sem apertar o botão lateral e com a tela completamente apagada. A senha não era o obstáculo. O SOS nativo aparece no bloqueio, mas só depois de acender o aparelho. Dispositivo informado para a primeira validação física: Doogee S100 Pro, Android 12. **Escopo do projeto:** aparelhos Android compatíveis, sem exclusividade de modelo, fabricante ou proprietário; o Doogee é referência histórica de testes, não restrição do software.

Decisão técnica: apps comuns não recebem toques capacitivos no painel apagado. Usar cinco ciclos de cobrir e descobrir o sensor de aproximação superior. Não confundir esse gesto com cinco toques no meio da tela. Preferir sensor wake-up; se inexistente, trava parcial de CPU durante serviço ativo, com exceção de otimização de bateria solicitada ao usuário. Não sacudir ou levantar o telefone.

Implementado: serviço em primeiro plano com notificação e desativação; prova obrigatória de cinco ciclos com tela apagada e bloqueada antes de habilitar modo real; qualificação concluída quando o aviso efetivamente chega ao onResume; contagem cancelável de cinco segundos; chamada via TelecomManager com pedido de viva-voz; contatos da agenda por seletor de um número; três contatos, um principal; modo de treino sem ligação; botão flutuante e tela mantida acesa como opções adicionais.

Estado de entrega e evidência: consultar VALIDACAO.md. Não há teste no Doogee físico. Não declarar confiabilidade de emergência ou chamada completada com base apenas em compilação, emulação ou teste do sensor. Próximo passo indispensável é configurar e testar no telefone de Bobby com um contato avisado, primeiro sem chamada, depois chamada real, inclusive após repouso.

Projeto local: /workspace/scratch/570ccbe3a02e/sos-toque
Pacote: org.enterprise.sostoque
Versão: 1.0.0 (1), minSdk 26 / targetSdk 35, APK universal Java, release sem debuggable.
Fontes exportados: SOS-Toque-1.0.0-fontes.zip, sem dados pessoais, APK interno ou chave.
APK: SOS-Toque-1.0.0.apk.
Identidade: vermelho escuro e letras SOS brancas, tipografia grande. Decisão comunicada durante implementação.
Licença: MIT.

Compilação: build.sh, SDK Platform 35 revision 2, Build Tools 35.0.0, ECJ 3.37.0 e Java 17. Caminhos temporários usados nesta sessão em /tmp/sos-toolchain. Variáveis documentadas no README.

Assinatura: RSA 3072, alias sostoque, esquemas v2 e v3. Certificado SHA-256: de06c7063e56e05aca4a98e6898c521279216e25e27ad86fcfec4c60490d80ab. A chave e sua senha são itens privados separados dos fontes: SOS-Toque-private-signing.p12 e SOS-Toque-private-signing-password.txt. Não publicar nem incluir no repositório. Uma atualização que preserve a instalação precisa da mesma chave. A criação de nova chave requer reinstalação e nova configuração.

Limites: sensor condicionado a tela apagada E bloqueada; cinco gestos não configuráveis; 30 segundos entre acionamentos por sensor; só contato pessoal completo para acionamento automático; números de serviço dependem do discador; não lê automaticamente contatos privados do SOS nativo; após reiniciar/forçar parada/trocar contato, abrir e reativar. O fabricante pode exigir autostart/bateria adicional. Não foi modificado o SOS nativo, bloqueio, senha, digital ou firmware.

Não há internet, backend, root, AccessibilityService, DeviceAdmin, leitura global da agenda, SMS ou telemetria. Os avisos de voz só usam voz que o motor TTS declara local.

Pendências futuras, após validação no aparelho: ajustar o gesto à mobilidade de Bobby; alternativa de despertar por gesto nativo do Doogee se existir; ajudar a configurar autostart do fabricante; possível atalho da tecla programável, apenas se útil. Nenhuma dessas alternativas foi tratada como implementada ou comprovada.
