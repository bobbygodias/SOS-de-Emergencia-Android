# Validação — SOS Toque 1.0.0

Estado: APK pessoal compilado e assinado; funcionamento no telefone físico ainda precisa de validação. Não classificar como solução de emergência comprovada.

## Verificações concluídas

- Recursos Android compilados e manifest ligado com AAPT2 / SDK 35.
- Código Java compilado com ECJ 3.37.0 / Java 17 e convertido para DEX com D8 release, min API 26.
- 37 verificações JVM aprovadas: disparo só no total exigido; duplicações; intervalo máximo; duração total; relógio regressivo; três toques opcionais; formatação de número; rejeição de USSD, URI, ramal/comando e números inválidos; cinco ciclos de aproximação; início já coberto; ruído breve; cobertura longa; expiração e eventos near repetidos.
- APK alinhado e assinatura verificada com apksigner: v2 e v3, RSA 3072, um certificado.
- Pacote org.enterprise.sostoque, versão 1.0.0 (1), min SDK 26, target SDK 35.
- Manifest inspecionado: debuggable=false, backup=false; serviço e atividades de chamada não exportados; apenas a atividade inicial é pública. Sem INTERNET, READ_CONTACTS, SMS, microfone, localização, acessibilidade ou administrador de dispositivo.
- Código revisado para revalidar permissões ao ativar e ao chamar; impedir chamada no treino; exigir treino apagado/bloqueado antes de armar sensor; não repetir chamada em recriação do processo; liberar proteção contra duplicações depois de sair da tela de acionamento.
- Não há telefones pessoais, senhas ou chave de assinatura nos fontes públicos ou APK.

## Validação de execução pendente

O emulador Android 12 foi preparado com imagem AOSP API 31 x86_64 e sensor de aproximação virtual, mas o ambiente não concluiu uma sessão de QA utilizável. O roteiro está em qa/android_flow.py; só aceita um serial emulator-* e usa um contato fictício. Não contar o roteiro escrito como teste realizado. Consulte qa/emulator-results.txt para o resultado da tentativa final, quando disponível.

Não há evidência de teste visual no Android, chamada completada, voz instalada, sensor físico do S100 Pro, bloqueio do fabricante, seleção de chip, Doze prolongado ou desempenho de bateria. Nenhuma chamada real foi realizada.

## Critério de aceitação no aparelho

Primeiro, o modo de treino deve detectar os cinco ciclos com a tela apagada e bloqueada e acender o aviso Sensor respondeu. Depois, com um contato avisado, o modo real deve solicitar a ligação sem senha nem botão lateral e ela deve chegar ao destino. Conferir viva-voz e repetir após repouso com tela apagada. Se uma etapa falhar, o aplicativo ainda não está validado para a necessidade do usuário.

O treino bem-sucedido habilita o modo real, mas não comprova a chamada nem garante operação futura. O SOS nativo deve continuar disponível.
