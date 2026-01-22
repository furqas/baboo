# Frontend de Upload de Vídeo - Baboo

Frontend básico para upload de vídeos em chunks.

## Como usar

1. Abra o arquivo `index.html` em um navegador
2. Configure o User ID (padrão: user123)
3. Configure as resoluções desejadas (padrão: 720p,1080p)
4. Selecione um arquivo de vídeo
5. Clique em "Iniciar Upload"

## Funcionamento

1. **Initiate**: Envia informações do arquivo para `/api/v1/uploads/video/initiate`
2. **Upload Chunks**: Divide o vídeo em chunks de 10MB e envia cada chunk para `/api/v1/uploads/video/chunk`
3. **Progress**: Mostra o progresso do upload em tempo real

## Endpoints utilizados

- POST `/api/v1/uploads/video/initiate` - Inicia o upload
- POST `/api/v1/uploads/video/chunk` - Envia cada chunk

## Configuração

Por padrão, aponta para `http://localhost:8081`. Altere `API_BASE_URL` em `upload.js` se necessário.
