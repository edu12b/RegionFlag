# RegionFlag

Plugin de regiões para servidores Minecraft Paper/Folia (1.21.x). Permite criar áreas no mapa e associar ações automáticas quando jogadores entram ou saem delas — como tocar sons, executar comandos, exibir títulos na tela ou teleportar.

Desenvolvido com foco em performance e compatibilidade total com Folia (multithreading por região).

---

## Requisitos

- Java 21+
- Paper 1.21.x ou Folia 1.21.x
- (Opcional) PlaceholderAPI — para usar placeholders como `%player_name%`

## Instalação

1. Baixe o arquivo `RegionFlag-1.0.0.jar`
2. Coloque na pasta `plugins/` do seu servidor
3. Reinicie o servidor
4. O arquivo `config.yml` será gerado automaticamente na pasta `plugins/RegionFlag/`

---

## Como funciona

O fluxo básico é:

1. Vá até um canto da área desejada e digite `/regiao pos1`
2. Vá até o canto oposto e digite `/regiao pos2`
3. Crie a região com `/regiao criar <nome>`
4. Adicione flags (ações) à região criada

Quando um jogador entrar na região, as ações configuradas serão executadas automaticamente.

---

## Comandos

### Gerenciamento de regiões

| Comando | Descrição |
|---|---|
| `/regiao pos1` | Define o primeiro ponto da seleção |
| `/regiao pos2` | Define o segundo ponto da seleção |
| `/regiao criar <nome>` | Cria uma região com a seleção atual |
| `/regiao deletar <nome>` | Remove uma região |
| `/regiao listar` | Lista as regiões do mundo atual |
| `/regiao info <nome>` | Mostra detalhes de uma região |
| `/regiao <nome> prioridade <n>` | Define a prioridade (para sobreposição) |

### Flags (ações)

| Comando | O que faz |
|---|---|
| `/regiao <nome> playsound <som>` | Toca um som ao entrar |
| `/regiao <nome> console_command <comando>` | Executa um comando no console ao entrar |
| `/regiao <nome> title <título> ; <subtítulo>` | Exibe um título na tela ao entrar |
| `/regiao <nome> teleport <x> <y> <z> [yaw] [pitch] [mundo]` | Teleporta o jogador ao entrar |

### Administração

| Comando | Descrição |
|---|---|
| `/regiao visualizar <nome>` | Mostra as bordas da região com partículas |
| `/regiao reload` | Recarrega configuração e regiões |
| `/regiao debug` | Ativa/desativa mensagens de debug |

---

## Exemplos práticos

### Criar uma região de spawn e tocar um som

```
/regiao pos1
(ande até o outro canto)
/regiao pos2
/regiao criar spawn
/regiao spawn playsound minecraft:entity.experience_orb.pickup
```

### Exibir um título colorido ao entrar

```
/regiao spawn title &#FF0000Bem-vindo ao Spawn ; &#00FF00Aproveite o servidor!
```

Resultado: o jogador vê "Bem-vindo ao Spawn" em vermelho e "Aproveite o servidor!" em verde.

### Executar um comando no console

```
/regiao spawn console_command give %player_name% diamond 1
```

Toda vez que alguém entrar na região, recebe 1 diamante.

### Teleportar o jogador

```
/regiao portal teleport 100 64 200 0 0 world
```

Ao entrar na região "portal", o jogador é enviado para as coordenadas 100, 64, 200.

---

## Permissões

| Permissão | Descrição | Padrão |
|---|---|---|
| `regionflag.use` | Comandos básicos (listar, info) | Todos |
| `regionflag.create` | Criar e deletar regiões | OP |
| `regionflag.admin` | Acesso completo (inclui todas abaixo) | OP |
| `regionflag.flag.playsound` | Configurar sons | OP |
| `regionflag.flag.console_command` | Configurar comandos | OP |
| `regionflag.flag.title` | Configurar títulos | OP |
| `regionflag.flag.teleport` | Configurar teleporte | OP |

---

## Cores

O plugin aceita dois formatos de cor:

- **Códigos legacy**: `&a` (verde), `&c` (vermelho), `&l` (negrito), etc.
- **Cores HEX**: `&#FF0000` (vermelho), `&#00FF00` (verde), `&#7C3AED` (roxo)

Exemplos:

```
/regiao spawn title &aBem-vindo ; &7Divirta-se
/regiao spawn title &#FF6B35Olá ; &#4ECDC4Subtítulo personalizado
```

---

## Estrutura dos arquivos

```
plugins/RegionFlag/
├── config.yml              <- Configurações e mensagens
├── regions/
│   ├── world.yml           <- Regiões do mundo "world"
│   ├── world_nether.yml    <- Regiões do nether
│   └── ...
└── backups/                <- Backups automáticos
```

---

## PlaceholderAPI

Se o PlaceholderAPI estiver instalado, você pode usar qualquer placeholder nos comandos e títulos:

```
/regiao spawn title Olá, %player_name% ; Bem-vindo de volta!
/regiao spawn console_command msg %player_name% Você entrou no spawn
```

---

## Compilando do código-fonte

```bash
git clone <repositório>
cd RegionFlag
mvn clean package
```

O JAR será gerado em `target/RegionFlag-1.0.0.jar`.

---

## Licença

Desenvolvido por **Eduardo12B**.
