# SIM Inventory

Portal web independente para cadastro, identificação e controle de estoque de chips SIM. Este projeto é separado do aplicativo Android SIM Inspector.

## Arquitetura Vercel

- Frontend estático em `web/`.
- API serverless em `api/`.
- PostgreSQL via integração Neon no Vercel Marketplace.
- Login administrativo por senha configurada como variável de ambiente.
- Estado do inventário persistido no PostgreSQL (JSONB), não no navegador.

## Publicação no Vercel

1. Importe `meta30bm-prog/sim-inventory` em https://vercel.com/new.
2. Use a raiz do repositório (não selecione a pasta `web` como Root Directory).
3. Em https://vercel.com/marketplace/neon, instale a integração Neon e conecte-a ao projeto SIM Inventory. Escolha um plano conforme sua necessidade; há opção inicial gratuita, sujeita aos limites do provedor.
4. Em **Settings → Environment Variables**, configure:
   - `ADMIN_PASSWORD`: uma senha administrativa forte e exclusiva.
   - `SESSION_SECRET`: uma chave aleatória longa (pelo menos 32 bytes).
   A integração Neon deverá adicionar `DATABASE_URL` automaticamente.
5. Faça um novo deploy depois de conectar o banco e configurar as variáveis.

O primeiro acesso solicita a senha administrativa. A tabela `sim_inventory_state` é criada automaticamente na primeira leitura/escrita autenticada.

## Migração dos dados locais

Depois que o projeto estiver publicado no Vercel e o banco estiver conectado, abra o novo endereço no mesmo navegador em que os chips já estão cadastrados e entre com a senha administrativa. Se o banco estiver vazio, o aplicativo envia automaticamente os dados locais desse navegador para o PostgreSQL. Se você já tiver um backup JSON, restaure-o depois de entrar.

**Importante:** a migração automática só consegue copiar os dados do navegador em que o inventário antigo foi usado. Faça também um backup JSON antes da mudança.

## Funcionalidades

- Painel com totais de chips não ativados, ativados e utilizados.
- Cadastro e edição por número, operadora, responsável e ICCID.
- Organização em caixas e 28 compartimentos na Caixa 01 (7 dias × 4 turnos), com 40 posições por compartimento.
- Busca por número, responsável, operadora ou ICCID.
- Histórico de movimentações.
- Exportação CSV e backup/restauração JSON.
- Interface responsiva para computador e celular.

As posições 01–40 são referências lógicas. Como os chips ficam soltos, para identificar fisicamente cada unidade é necessário usar divisórias, envelopes ou suportes numerados.

## Segurança

Não coloque senhas, chaves ou dados reais de chips no código-fonte. Configure os segredos somente nas variáveis de ambiente do Vercel. O acesso à API do inventário exige sessão autenticada.

## Desenvolvimento

Instale as dependências com `npm install`. Para executar localmente, configure `DATABASE_URL`, `ADMIN_PASSWORD` e `SESSION_SECRET` no ambiente e use a CLI do Vercel (`vercel dev`).
