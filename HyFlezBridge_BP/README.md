# HyFlez Bridge 1.0.0

Add-on experimental de integração para Minecraft Bedrock 1.26.0.

## O que esta primeira versão faz
- Confirma no chat quando o Bridge inicia no mundo.
- Consulta entidades próximas dentro de 24 blocos.
- Mostra a quantidade e a entidade mais próxima na barra de ação.
- Não lê memória do jogo, não injeta código e não automatiza combate.

## Instalação
1. Baixe o arquivo `HyFlezBridge.mcaddon` no artefato da compilação do GitHub Actions.
2. Abra o arquivo no Android e escolha Minecraft para importar.
3. No seu mundo, abra as configurações e ative o pacote de comportamento HyFlez Bridge.
4. Entre no mundo e confirme a mensagem [HYFLEZ] Bridge conectado.

A API `@minecraft/server` 2.5.0 corresponde à linha de Minecraft Bedrock 1.26.0.

## Limites importantes
O add-on executa dentro de mundos onde o pacote está instalado e permitido. Ele não cria uma conexão externa direta entre o APK e o processo do Minecraft. Em servidores públicos que não permitem seu add-on, o Bridge não executará. Esta versão não fornece ESP de jogadores nem mira automática.
