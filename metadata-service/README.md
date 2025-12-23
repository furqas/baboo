## Metadata service

this service must be responsible for only receiving the metadata of videos and playlists
and provide CRUD operations for them.

### Endpoints

#### Videos

POST   /api/v1/videos                    # Criar metadata inicial
GET    /api/v1/videos/:id                # Buscar por ID
PATCH  /api/v1/videos/:id                # Atualizar metadata
DELETE /api/v1/videos/:id                # Deletar vídeo
GET    /api/v1/videos/:id/stats          # Estatísticas

#### Busca e Descoberta
GET    /api/v1/videos?q=query            # Busca por texto
GET    /api/v1/videos?category=gaming    # Filtrar por categoria
GET    /api/v1/videos?userId=xxx         # Vídeos de um canal
GET    /api/v1/videos/trending           # Vídeos em alta

#### Playlists
POST   /api/v1/playlists                 # Criar playlist
GET    /api/v1/playlists/:id             # Buscar playlist
PATCH  /api/v1/playlists/:id             # Atualizar
DELETE /api/v1/playlists/:id             # Deletar
POST   /api/v1/playlists/:id/videos      # Adicionar vídeo
DELETE /api/v1/playlists/:id/videos/:vid # Remover vídeo
PUT    /api/v1/playlists/:id/reorder     # Reordenar vídeos

#### Relacionamentos
GET    /api/v1/users/:id/videos          # Vídeos de um usuário
GET    /api/v1/users/:id/playlists       # Playlists de um usuário
´´´