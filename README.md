# SIM Inventory

Portal web independente para cadastro, identificação e controle de estoque de chips SIM. Este projeto é separado do aplicativo Android SIM Inspector.

## Publicação
Habilite **Settings → Pages → Build and deployment → GitHub Actions**. O workflow em `.github/workflows/deploy-pages.yml` publica automaticamente a pasta `web/` ao enviar alterações para `main`.

Endereço esperado: https://meta30bm-prog.github.io/sim-inventory/

## Funcionalidades
- Painel com totais de chips não ativados, ativados e utilizados.
- Cadastro e edição por número, operadora, responsável e ICCID.
- Organização em caixas e 28 compartimentos na Caixa 01 (7 dias × 4 turnos), com 40 posições por compartimento.
- Busca por número, responsável, operadora ou ICCID.
- Histórico de movimentações.
- Exportação CSV e backup/restauração JSON.
- Interface responsiva para computador e celular.

## Armazenamento e limitações
Esta versão MVP salva os dados no armazenamento local do navegador (`localStorage`) e **não sincroniza entre dispositivos ou navegadores**. Limpar os dados do navegador pode apagá-los; exporte backups regularmente. Ainda não há banco de dados central, login ou integração com o Android.

As posições 01–40 são referências lógicas. Como os chips ficam soltos, para identificar fisicamente cada unidade é necessário usar divisórias, envelopes ou suportes numerados.

## Próximas etapas recomendadas
1. Backend/API HTTPS e PostgreSQL.
2. Autenticação de um administrador.
3. Identificador único e QR Code por chip.
