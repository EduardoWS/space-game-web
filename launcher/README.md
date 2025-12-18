# Space Game Launcher

O painel de controle e ponto de entrada oficial para o **Space Game**. Fornece autenticação simplificada, gerenciamento de perfil e acesso às últimas novidades do universo do jogo.

## 🚀 Funcionalidades

*   **Dashboard Interativo**: Visualize as "Mission Briefings" (Release Notes) com detalhes das atualizações (v1.0, v1.1, v1.2, etc.).
*   **Gerenciamento de Identidade**:
    *   Crie e gerencie seu "Callsign" (Username) único.
    *   Verique o status da sua conta e credenciais de segurança.
*   **Integração Perfeita**: Autenticação compartilhada com o jogo principal (Single Sign-On via Firebase).

## 🛠️ Tecnologias

Este projeto foi construído com uma stack moderna para garantir performance e manutenibilidade:

*   **Core**: React 19 + TypeScript
*   **Build Tool**: Vite v7
*   **Estilização**: CSS Modules / Variáveis Globais (Design System "Glassmorphism")
*   **Backend Integration**: Firebase Authentication & Firestore

## 📦 Como Rodar Localmente

O Launcher reside na pasta `launcher/` da raiz do projeto.

1.  **Instale as dependências**:
    ```bash
    cd launcher
    npm install
    ```

2.  **Inicie o servidor de desenvolvimento**:
    ```bash
    npm run dev
    ```
    O launcher estará disponível em `http://localhost:5173`.

3.  **Build para Produção**:
    Para gerar os arquivos estáticos que serão servidos junto com o jogo:
    ```bash
    npm run build
    ```
    Os arquivos serão gerados na pasta `dist/`.

## 🔗 Estrutura

*   `src/components`: Componentes UI reutilizáveis (Login, Dashboard, Register).
*   `src/context`: Gerenciamento de estado global (AuthContext).
*   `src/data`: Arquivos estáticos de dados (ex: `v1.json` para release notes).
*   `public/`: Assets estáticos.
