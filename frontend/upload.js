const API_BASE_URL = 'http://localhost:8081/api/v1';
const CHUNK_SIZE = 10 * 1024 * 1024; // 10MB

document.getElementById('uploadBtn').addEventListener('click', async () => {
  const fileInput = document.getElementById('videoFile');
  const userIdInput = document.getElementById('userId');
  const resolutionsInput = document.getElementById('resolutions');

  const file = fileInput.files[0];
  if (!file) {
    alert('Por favor, selecione um arquivo de vídeo');
    return;
  }

  const userId = userIdInput.value.trim();
  if (!userId) {
    alert('Por favor, informe o User ID');
    return;
  }

  const resolutionsStr = resolutionsInput.value.trim();
  const resolutions = resolutionsStr ? resolutionsStr.split(',').map(r => r.trim()) : ['720p'];

  await uploadVideo(file, userId, resolutions);
});

async function uploadVideo(file, userId, resolutions) {
  updateStatus('Iniciando upload...');

  try {
    const initiateResponse = await initiateUpload(file, userId, resolutions);
    updateStatus(`Upload iniciado. ID: ${initiateResponse.uploadId}`);
    updateDetails(`Video ID: ${initiateResponse.videoId}<br>Total de chunks: ${initiateResponse.totalChunks}`);

    const totalChunks = initiateResponse.totalChunks;

    console.log(totalChunks)

    for (let chunkNumber = 1; chunkNumber <= totalChunks; chunkNumber++) {
      const start = (chunkNumber - 1) * CHUNK_SIZE;
      const end = Math.min(start + CHUNK_SIZE, file.size);
      const chunk = file.slice(start, end);

      updateStatus(`Enviando chunk ${chunkNumber} de ${totalChunks}...`);

      console.log(`com tamanho de ${chunk.size}`)

      await uploadChunk(
        initiateResponse.uploadId,
        chunkNumber,
        totalChunks,
        chunk,
        file.name
      );

      const progress = (chunkNumber / totalChunks) * 100;
      updateProgress(progress);
    }

    updateStatus('Upload concluído com sucesso!');
    updateDetails(`Upload finalizado!<br>Video ID: ${initiateResponse.videoId}<br>Upload ID: ${initiateResponse.uploadId}`);

  } catch (error) {
    updateStatus(`Erro: ${error.message}`);
    console.error('Erro no upload:', error);
  }
}

async function initiateUpload(file, userId, resolutions) {
  const request = {
    fileName: file.name,
    fileSize: file.size,
    contentType: file.type,
    userId: userId,
    resolutions: resolutions
  };

  const response = await fetch(`${API_BASE_URL}/uploads/video/initiate`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(request)
  });

  if (!response.ok) {
    throw new Error(`Erro ao iniciar upload: ${response.status} ${response.statusText}`);
  }

  console.log("Initiate finished")

  return await response.json();
}

async function uploadChunk(uploadId, chunkNumber, totalChunks, chunkBlob, fileName) {
  const formData = new FormData();
  formData.append('file', chunkBlob, fileName);

  const url = new URL(`${API_BASE_URL}/uploads/video/chunk`);
  url.searchParams.append('uploadId', uploadId);
  url.searchParams.append('chunkNumber', chunkNumber.toString());
  url.searchParams.append('totalChunks', totalChunks.toString());

  const response = await fetch(url.toString(), {
    method: 'POST',
    body: formData
  });

  if (!response.ok) {
    throw new Error(`Erro ao enviar chunk ${chunkNumber}: ${response.status} ${response.statusText}`);
  }

  return await response.json();
}

function updateStatus(message) {
  document.getElementById('status').textContent = message;
}

function updateProgress(percentage) {
  document.getElementById('progressBar').value = percentage;
}

function updateDetails(html) {
  document.getElementById('details').innerHTML = html;
}
