# Wiki — RegionFlag

Guia completo de uso do plugin. Cada seção aborda uma funcionalidade específica com explicações e exemplos.

---

## Índice

1. [Primeiros passos](#primeiros-passos)
2. [Criando regiões](#criando-regiões)
3. [Flag: Playsound](#flag-playsound)
4. [Flag: Console Command](#flag-console-command)
5. [Flag: Title](#flag-title)
6. [Flag: Teleport](#flag-teleport)
7. [Prioridade e sobreposição](#prioridade-e-sobreposição)
8. [Configuração](#configuração)
9. [Permissões](#permissões)
10. [Perguntas frequentes](#perguntas-frequentes)

---

## Primeiros passos

Após instalar o plugin, o básico é:

1. Selecionar dois pontos no mundo (os cantos opostos de um cubo)
2. Criar a região com um nome
3. Adicionar uma ou mais flags à região

As regiões são salvas automaticamente em YAML e persistem após reiniciar o servidor.

### Aliases

O comando principal é `/regiao`, mas você também pode usar `/region` ou `/rg`.

---

## Criando regiões

### Passo 1 — Selecionar os pontos

Vá até o primeiro canto da área que deseja proteger/marcar:

```
/regiao pos1
```

O plugin salva a posição do bloco onde você está. Agora vá até o canto oposto (diagonal):

```
/regiao pos2
```

A seleção forma um cubo entre os dois pontos. Não importa a ordem — o plugin calcula automaticamente qual é o menor e o maior ponto.

### Passo 2 — Criar a região

```
/regiao criar minha-regiao
```

Pronto. A região foi salva no arquivo `regions/<mundo>.yml`. A seleção é limpa automaticamente.

### Outros comandos úteis

Ver informações de uma região:
```
/regiao info minha-regiao
```

Listar todas as regiões do mundo onde você está:
```
/regiao listar
```

Deletar uma região:
```
/regiao deletar minha-regiao
```

Visualizar as bordas com partículas (temporário, dura alguns segundos):
```
/regiao visualizar minha-regiao
```

---

## Flag: Playsound

Toca um som do Minecraft quando o jogador entra na região.

### Adicionar um som

```
/regiao spawn playsound minecraft:entity.experience_orb.pickup
```

Você pode adicionar vários sons — todos serão tocados ao mesmo tempo:

```
/regiao spawn playsound minecraft:entity.experience_orb.pickup
/regiao spawn playsound minecraft:block.note_block.pling
```

### Listar sons configurados

```
/regiao spawn playsound listar
```

### Remover um som

```
/regiao spawn playsound remover minecraft:entity.experience_orb.pickup
```

### Onde encontrar nomes de sons

Os nomes seguem o padrão do Minecraft. Alguns exemplos úteis:

| Som | Descrição |
|---|---|
| `minecraft:entity.experience_orb.pickup` | Som de XP |
| `minecraft:block.note_block.pling` | Pling de note block |
| `minecraft:entity.player.levelup` | Som de level up |
| `minecraft:ui.toast.challenge_complete` | Som de conquista |
| `minecraft:block.portal.trigger` | Som de portal |

O tab completion sugere sons conforme você digita.

### Cooldown

Existe um cooldown entre ativações para evitar spam. O padrão é 5 segundos (configurável no `config.yml`).

---

## Flag: Console Command

Executa um ou mais comandos no console do servidor quando o jogador entra na região. Útil para dar itens, aplicar efeitos, enviar mensagens, etc.

### Adicionar um comando

```
/regiao spawn console_command give %player_name% diamond 1
```

O `%player_name%` será substituído pelo nome do jogador automaticamente. Se o PlaceholderAPI estiver instalado, qualquer placeholder funciona.

### Múltiplos comandos

Cada execução do comando adiciona um novo à lista. Todos são executados em sequência:

```
/regiao spawn console_command give %player_name% diamond 1
/regiao spawn console_command effect give %player_name% speed 30 1
/regiao spawn console_command msg %player_name% Bem-vindo ao spawn!
```

### Listar comandos

```
/regiao spawn console_command listar
```

Mostra os comandos com seus índices (0, 1, 2...).

### Remover um comando por índice

```
/regiao spawn console_command remover 0
```

Remove o primeiro comando da lista.

### Cooldown

Padrão: 10 segundos. Configurável no `config.yml`.

---

## Flag: Title

Exibe um título grande na tela do jogador ao entrar na região. Suporta cores HEX e texto formatado.

### Definir título simples

```
/regiao spawn title Bem-vindo ao Spawn
```

### Definir título com subtítulo

Use ` ; ` (espaço, ponto-e-vírgula, espaço) para separar título e subtítulo:

```
/regiao spawn title Bem-vindo ; Aproveite o servidor!
```

### Usar cores

Cores legacy (padrão do Minecraft):
```
/regiao spawn title &aBem-vindo ; &7Divirta-se!
```

Cores HEX (qualquer cor RGB):
```
/regiao spawn title &#FF0000Bem-vindo ; &#00FF00Aproveite!
```

Você pode misturar os dois formatos:
```
/regiao spawn title &#FF6B35Spawn &l- &fServidor ; &7Use /ajuda para começar
```

### Ajustar o tempo de exibição

Os tempos são em ticks (20 ticks = 1 segundo):

```
/regiao spawn title tempo 10 70 20
```

Onde:
- `10` = fadeIn (tempo para aparecer)
- `70` = stay (tempo visível)
- `20` = fadeOut (tempo para sumir)

### Remover título

```
/regiao spawn title remover
```

### Cooldown

Padrão: 15 segundos. Configurável no `config.yml`.

---

## Flag: Teleport

Teleporta o jogador para coordenadas específicas ao entrar na região. Útil para portais, lobbies, ou redirecionamento de áreas.

### Definir destino

```
/regiao portal teleport 100 64 200 0 0 world
```

Os argumentos são: `<x> <y> <z> [yaw] [pitch] [mundo]`

- `x y z` — coordenadas de destino (obrigatório)
- `yaw` — rotação horizontal (0 = sul, 90 = oeste) — opcional, padrão 0
- `pitch` — rotação vertical (0 = frente, -90 = cima) — opcional, padrão 0
- `mundo` — nome do mundo de destino — opcional, usa o mundo atual

### Exemplos

Teleportar para o spawn do mundo:
```
/regiao entrada teleport 0 100 0
```

Teleportar para o nether olhando para o norte:
```
/regiao portal-nether teleport 50 64 50 180 0 world_nether
```

Teleportar mantendo a mesma direção que o jogador (yaw e pitch do tab complete):
```
/regiao lobby teleport 0 64 0
```

O tab completion sugere automaticamente suas coordenadas atuais.

### Ver configuração atual

```
/regiao portal teleport
```

### Remover teleport

```
/regiao portal teleport remover
```

### Cooldown

Padrão: 10 segundos. Configurável no `config.yml`.

---

## Prioridade e sobreposição

Quando regiões se sobrepõem, a de maior prioridade é processada primeiro. Isso é importante quando duas regiões têm flags conflitantes.

### Definir prioridade

```
/regiao spawn prioridade 10
/regiao loja prioridade 5
```

Se um jogador estiver dentro das duas regiões ao mesmo tempo, as flags da região "spawn" (prioridade 10) são executadas antes das da "loja" (prioridade 5).

Ambas as regiões terão suas flags executadas — a prioridade apenas define a ordem.

---

## Configuração

O arquivo `config.yml` é gerado automaticamente. As principais seções são:

### Cooldowns padrão

```yaml
cooldowns:
  playsound: 5         # segundos
  console_command: 10
  title: 15
  teleport: 10
```

### Tempos do título

```yaml
title:
  fadeIn: 10     # ticks (0.5 segundo)
  stay: 70       # ticks (3.5 segundos)
  fadeOut: 20    # ticks (1 segundo)
```

### Visualizador de partículas

```yaml
visualizer:
  particle: FLAME
  duration: 5       # segundos
  density: 0.5      # distância entre partículas
```

### Backup automático

```yaml
backup:
  enabled: true
  interval: 300   # segundos (5 minutos)
```

### Mensagens

Todas as mensagens do plugin são configuráveis na seção `messages` do `config.yml`. Suportam cores HEX e legacy.

---

## Permissões

| Permissão | O que permite | Padrão |
|---|---|---|
| `regionflag.use` | Listar e ver info de regiões | Todos |
| `regionflag.create` | Criar e deletar regiões | OP |
| `regionflag.flag.playsound` | Configurar sons | OP |
| `regionflag.flag.console_command` | Configurar comandos | OP |
| `regionflag.flag.title` | Configurar títulos | OP |
| `regionflag.flag.teleport` | Configurar teleporte | OP |
| `regionflag.admin` | Tudo acima + reload, debug, visualizar | OP |

---

## Perguntas frequentes

### As regiões são salvas quando o servidor desliga?

Sim. As regiões são salvas automaticamente quando:
- Você cria, deleta ou modifica uma região
- O servidor é desligado normalmente
- O backup automático é executado (a cada 5 minutos por padrão)

### O plugin funciona no Folia?

Sim. Todas as operações usam os schedulers corretos do Folia (EntityScheduler, RegionScheduler, etc). No Paper comum, esses schedulers são executados na main thread normalmente.

### O PlayerMoveEvent não causa lag?

O plugin só processa o evento quando o jogador muda de **bloco** (não quando apenas olha ao redor). Além disso, usa um cache baseado em chunks para encontrar regiões em O(1), então o impacto em performance é mínimo.

### Posso ter várias flags na mesma região?

Sim. Uma região pode ter sons, comandos, título e teleporte ao mesmo tempo. Todas as flags são executadas quando o jogador entra.

### Como adiciono uma nova flag personalizada?

O sistema de flags é extensível. Para criar uma nova:

1. Crie uma classe que estenda `RegionFlag`
2. Implemente os métodos `onEnter()`, `onExit()`, `handleCommand()`, etc
3. Registre no `RegionFlagPlugin.registerDefaultFlags()`:
   ```java
   flagRegistry.registerFlag(new MinhaFlag(this));
   ```

### Os cooldowns são por jogador?

Sim. Cada jogador tem seu próprio cooldown independente para cada flag de cada região.
