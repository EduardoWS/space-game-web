# Space Game

Bem-vindo ao **Space Game**, um jogo de nave estilo arcade desenvolvido em **Java** com **LibGDX**, compilado para Web usando **GWT** e integrado com **Firebase** para autenticação e recordes globais.


## 🎮 Sobre o Jogo

Assuma o comando da sua nave e defenda a galáxia contra hordas de alienígenas! Acumule pontos, sobreviva o máximo que puder e dispute o topo do ranking global.

### Principais Funcionalidades
*   **Jogabilidade Arcade**: Controles simples e ação frenética.
*   **Novo Sistema de Combate (v1.2)**: "Charged Shot" para destruir múltiplos inimigos e sistema de combos.
*   **Visual Aprimorado (v1.2)**: Backgrounds dinâmicos e skins de aliens com efeitos de sangue verde.
*   **Sistema de Login**: Crie sua conta ou entre com o Google para salvar seu progresso.
*   **Ranking Global**: Veja sua posição entre os melhores comandantes da galáxia.
*   **HUD Inteligente**: Feedback visual de recursos e visualizador de música.

---

## 🛠️ Tecnologias Utilizadas

*   **Linguagem**: Java (Core Game Logic).
*   **Framework**: LibGDX (Desenvolvimento de Jogos).
*   **Web Toolkit**: GWT (Google Web Toolkit) para transpilar Java para JavaScript.
*   **Frontend Web**: HTML5, CSS3 (Interface de Login/UI).
*   **Backend/Infraestrutura**:
    *   **Firebase Authentication**: Gerenciamento de usuários (Email/Senha e Google).
    *   **Cloud Firestore**: Banco de dados NoSQL para salvar perfis e scores em tempo real.
    *   **Firebase Hosting**: Hospedagem da aplicação web.
    *   **FastAPI (Python)**: Backend auxiliar para validações e health-check (hospedado no Render).

---

## 🚀 Como Rodar Localmente

### Pré-requisitos
*   **Java JDK 11** ou superior instalado.
*   **Git** instalado.
*   **Node.js / npm** (para Firebase CLI).

### Passos

1.  **Clone o repositório**:
    ```bash
    git clone https://github.com/seu-usuario/space-game-web.git
    cd space-game-web
    ```

2.  **Inicie o Servidor de Desenvolvimento GWT**:
    Abra o terminal na pasta raiz do projeto e execute:
    ```bash
    ./gradlew html:superDev
    ```
    *No Windows Powershell: `.\gradlew html:superDev`*

3.  **Acesse o Jogo**:
    Abra seu navegador e vá para:
    [http://localhost:8080/html](http://localhost:8080/html)

    *Nota: Na primeira vez, pode demorar alguns minutos para compilar.*

---

## 📦 Como Buildar (Compilar para Produção)

Para gerar os arquivos finais otimizados para web:

1.  Execute o comando de distribuição:
    ```bash
    ./gradlew html:dist
    ```
    
2.  Os arquivos gerados estarão em: `html/build/dist/`

---

## ☁️ Como Fazer Deploy

O projeto está configurado para o **Firebase Hosting**.

1.  **Login no Firebase**:
    ```bash
    firebase login
    ```

2.  **Deploy**:
    Certifique-se de ter rodado o build (`html:dist`) antes.
    ```bash
    firebase deploy
    ```

3.  O jogo estará disponível na URL fornecida pelo Firebase (ex: `https://space-game-web.web.app`).

---

## 📂 Estrutura do Projeto

*   **core/**: Código-fonte principal do jogo (Java). Compartilhado entre todas as plataformas.
*   **html/**: Código específico para a versão Web (GWT) e arquivos estáticos (`webapp/`).
*   **backend/**: API Python auxiliar (FastAPI). *Veja o README dentro da pasta para mais detalhes.*
*   **assets/**: Imagens, sons e fontes do jogo.

---

## 📜 Licença

Este projeto é de código aberto. Sinta-se à vontade para contribuir!
