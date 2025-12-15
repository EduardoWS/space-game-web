# Space Game - Backend API

Este diretório contém a API auxiliar do jogo **Space Game**, desenvolvida em **Python** utilizando **FastAPI**.

Embora o frontend do jogo se comunique diretamente com o Firebase (Firestore) para a maioria das operações (leitura/escrita de scores), este backend serve como uma camada adicional para:
1.  **Health Check**: O jogo "pinga" este servidor para garantir que os serviços online estão ativos.
2.  **Validação de Scores (Opcional)**: Pode ser usado para validar submissões via HTTP se necessário.
3.  **Administração**: Scripts de manutenção do banco de dados (ex: limpar scores antigos).

---

## 🛠️ Tecnologias

*   **Python 3.9+**
*   **FastAPI**: Framework web moderno e rápido.
*   **Uvicorn**: Servidor ASGI.
*   **Firebase Admin SDK**: Para interagir com o Firestore com privilégios de administrador.

---

## 🚀 Como Rodar Localmente

### Pré-requisitos
*   Python instalado.
*   Credenciais do Firebase (`serviceAccountKey.json`).

### Configuração

1.  **Acesse a pasta do backend**:
    ```bash
    cd backend
    ```

2.  **Crie um Ambiente Virtual (.venv)**:
    ```bash
    python -m venv .venv
    ```

3.  **Ative o Ambiente Virtual**:
    *   **Windows**:
        ```bash
        .venv\Scripts\activate
        ```
    *   **Linux/Mac**:
        ```bash
        source .venv/bin/activate
        ```

4.  **Instale as Dependências**:
    ```bash
    pip install -r requirements.txt
    ```

5.  **Credenciais do Firebase**:
    *   Baixe sua chave de serviço do Console do Firebase (Project Settings > Service Accounts).
    *   Renomeie o arquivo para `serviceAccountKey.json`.
    *   Coloque-o **dentro desta pasta `backend/`**.

    *> ⚠️ **Importante**: Nunca commite o arquivo `serviceAccountKey.json` no Git!*

---

## ▶️ Executando a API

Para rodar o servidor de desenvolvimento com *hot-reload*:

```bash
uvicorn main:app --reload
```
O servidor iniciará em: `http://127.0.0.1:8000`

### Endpoints Disponíveis

*   `GET /`: Retorna o status da API (Health Check).
*   `GET /scores`: Retorna os Top 10 scores do banco de dados.
*   `POST /scores`: Recebe um novo score (JSON: `{ "playerName": "...", "score": 123 }`).

---

## ☁️ Deploy (Render)

Esta API está configurada para rodar no **Render.com**.

1.  Crie um novo **Web Service** no Render.
2.  Conecte ao seu repositório.
3.  **Build Command**: `pip install -r backend/requirements.txt`
4.  **Start Command**: `python backend/main.py`
5.  **Variáveis de Ambiente**:
    Em produção, não usamos o arquivo JSON. Em vez disso, configuramos uma variável de ambiente:
    *   `FIREBASE_CREDENTIALS`: Cole o conteúdo minificado do seu `serviceAccountKey.json`.

---
