# SOS Toque

Aplicativo Android de código aberto para solicitar uma chamada a um contato escolhido sem apertar o botão lateral, destinado a aparelhos Android compatíveis, independentemente de marca ou modelo. Projeto de Bobby Dias e Andrew Vox, Enterprise. Código aberto, licença MIT.

## O que o gesto realmente faz

Com o modo de sensor ativo, a tela **apagada e bloqueada**, cubra e descubra a região do sensor de aproximação, junto ao alto-falante superior, cinco vezes. Deixe a mão sobre a região por aproximadamente meio segundo por gesto. Cada ciclo reconhecido vibra. A quinta repetição abre uma contagem cancelável de cinco segundos e solicita uma chamada pelo chip ao contato principal.

**Não são cinco toques capacitivos em qualquer ponto da tela apagada.** Aplicativos comuns não recebem esses eventos quando o painel está desligado. O sensor de aproximação é uma alternativa distinta, que precisa responder no aparelho real. Não é necessário levantar ou sacudir o aparelho.

## Instalar e preparar, com ajuda se necessário

1. Instale `SOS-Toque-1.0.0.apk` e abra SOS Toque.
2. Escolha o contato da agenda ou digite nome e telefone com DDD. É possível escolher o mesmo contato já usado no SOS do celular; a lista privada do SOS do sistema não é importada automaticamente. Existem até três contatos, com um principal. Os outros não recebem ligações automáticas.
3. Toque em **Testar sensor sem ligar**. O app orienta a permitir aparecer sobre outros apps e funcionar sem otimização de bateria. No Android 13 ou superior também solicita notificações. Volte ao app após cada permissão e toque novamente no teste.
4. Inicie o teste. Apague e bloqueie a tela, deixe o topo descoberto e então cubra/descubra a região do sensor cinco vezes. Não deixe mais de três segundos entre movimentos. O teste termina após três minutos se não concluir. **Não faz ligação.**
5. O sucesso deve acender a tela e mostrar **Sensor respondeu**. Só depois disso o botão de ativar chamadas por sensor fica habilitável.
6. Toque em **ATIVAR SOS com tela apagada**, autorize chamadas e confirme a ativação. Combine com o contato e faça uma chamada real de teste com a tela apagada e bloqueada. Em aparelhos com dois chips, configure um chip padrão para chamadas no Android, para evitar uma escolha durante o acionamento.
7. Confira se a ligação chega ao contato e se o viva-voz funciona. Repita o teste depois de o telefone ficar parado com a tela apagada por algum tempo; otimizações adicionais do fabricante podem suspender o app. Se falhar, ainda não está adequado para depender dele.

O teste do sensor valida apenas a detecção dos gestos e a abertura do aviso acima do bloqueio. **Não valida atendimento, rede móvel, chip, crédito, viva-voz ou operação após longos períodos de repouso.**

## Outros modos

- Botão flutuante: cinco toques (opcionalmente três) com tela acesa. Não substitui o sensor para tela apagada.
- Tela SOS: área grande de toque, mantida acesa enquanto a atividade estiver visível; pode aparecer sobre o bloqueio.
- Treino de toques: usa a mesma sequência e contagem, sem telefone.
- Contatos de reserva: disponíveis manualmente após uma tentativa, sem chamadas sequenciais automáticas.
- Contagem com botão CANCELAR e solicitação de viva-voz. O aviso falado depende de uma voz local em português instalada no Android. Vibração depende do hardware e configurações do sistema.

## Limites relevantes

- Escopo geral: aparelhos Android compatíveis com Android 8.0 (API 26) ou superior. O modo de tela apagada depende de sensor de proximidade funcional e das condições de funcionamento em segundo plano do aparelho. Não exige root nem substitui o discador padrão.
- O Doogee S100 Pro / Android 12 foi apenas o aparelho de referência previsto para validação inicial, **não** um requisito, alvo exclusivo nem limite de distribuição. Não houve teste físico nele nesta entrega. Consulte `VALIDACAO.md` para as evidências efetivamente obtidas; cada modelo requer validação prática.
- A qualificação usa tela apagada **e** bloqueada; deixar a tela apagada sem bloqueio não atende esse modo nesta versão.
- Números curtos e números reconhecidos pelo sistema como emergência usam o discador, com confirmação do Android. O modo automático por sensor exige telefone completo de um contato pessoal. O app não modifica o SOS original do sistema.
- Solicitar uma chamada ao Android não comprova que ela completou ou foi atendida.
- Após reiniciar, forçar parada ou mudar contatos, abra e ative o app novamente. Se o serviço for recuperado pelo Android após falta de memória, ele tenta restaurar o modo real previamente ativado; o fabricante ainda pode impedir isso.
- Há um intervalo mínimo de 30 segundos entre acionamentos por sensor para evitar duplicações. Os botões na tela continuam disponíveis para uma nova tentativa explícita.
- Capa, película, posição da mão, sensor virtual ou políticas do fabricante podem impedir a leitura. Uma cobertura contínua, como no bolso, não equivale aos cinco ciclos.
- Aparelho sem bateria ou completamente desligado não executa o aplicativo. Permitir funcionamento sem otimização de bateria pode aumentar o consumo. Sensores sem capacidade de despertar o processador exigem uma trava de CPU enquanto o modo estiver ativo.

## Privacidade e permissões

Contatos salvos apenas em SharedPreferences privadas; backup Android desativado. Nenhum telefone pessoal é incluído no código ou APK. Não pede acesso geral à agenda: usa o seletor de um contato. Sem INTERNET, localização, microfone, SMS, acessibilidade, administrador do dispositivo, analytics, anúncios ou conta. A voz usa apenas uma voz que o motor TTS informa ser local. A chamada passa pelo sistema telefônico e sua operadora.

Permissões: CALL_PHONE, VIBRATE, SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE, FOREGROUND_SERVICE_SPECIAL_USE, POST_NOTIFICATIONS, WAKE_LOCK e REQUEST_IGNORE_BATTERY_OPTIMIZATIONS. Serviço em primeiro plano visível, com ação de desativar na notificação. Activities de acionamento e serviço não são exportados.

## Compilação

Java nativo e widgets Android, sem dependências de aplicativo. Escolha deliberada para manter a ferramenta pequena e auditável. `minSdk 26`, `targetSdk 35`, versão 1.0.0, pacote `org.enterprise.sostoque`. APK universal, sem bibliotecas nativas. Não é uma submissão para a Play Store.

Ferramentas usadas: Android SDK Platform 35 revision 2, Build Tools 35.0.0, Eclipse ECJ 3.37.0, Java 17. Configure os caminhos e execute:

```bash
export SOS_ANDROID_JAR=/caminho/android-35/android.jar
export SOS_BUILD_TOOLS=/caminho/build-tools/35.0.0
export SOS_ECJ_JAR=/caminho/ecj-3.37.0.jar
bash build.sh
```

O script compila recursos, Java, DEX, alinha o APK e executa os testes de sequência/validação. Para assinar, forneça `SOS_KEYSTORE` e `SOS_KEYSTORE_PASS_FILE`; alias `sostoque`, senha do arquivo de armazenamento igual à senha da chave. A chave de assinatura pessoal **não** integra os fontes ou APK. Para gerar sua própria distribuição, crie sua própria chave. Atualizar a instalação original exige o mesmo certificado.

Fontes principais: `MainActivity` (configuração), `OverlayService` (sensor e botão), `ProximitySequence` (gestos), `TapSequence` (toques), `TriggerActivity` (contagem e pedido de chamada), `ReadyActivity` (tela acesa).

## Referências técnicas

- https://source.android.com/docs/core/interaction/sensors/suspend-mode
- https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview
- https://developer.android.com/reference/android/telecom/TelecomManager#placeCall(android.net.Uri,%20android.os.Bundle)
- https://developer.android.com/guide/components/activities/secure-bal
- https://developer.android.com/develop/background-work/services/fgs/service-types
