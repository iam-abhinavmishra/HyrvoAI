const API_BASE_URL = 'http://localhost:8080';

async function handleResponse(response) {
  const contentType = response.headers.get('content-type');

  let data;

  if (
    contentType &&
    contentType.includes('application/json')
  ) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {

    if (
      typeof data === 'object' &&
      data?.message
    ) {
      throw new Error(data.message);
    }

    if (
      typeof data === 'object' &&
      data?.detail
    ) {
      throw new Error(data.detail);
    }

    if (
      typeof data === 'object' &&
      data?.errors &&
      Array.isArray(data.errors)
    ) {
      const messages = data.errors
        .map(
          (error) =>
            error.defaultMessage ||
            error.message
        )
        .filter(Boolean);

      if (messages.length > 0) {
        throw new Error(
          messages.join(', ')
        );
      }
    }

    throw new Error(
      typeof data === 'string' && data
        ? data
        : `Request failed with status ${response.status}`
    );
  }

  return data;
}

export async function loginUser(
  email,
  password
) {
  const response = await fetch(
    `${API_BASE_URL}/api/auth/login`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email,
        password,
      }),
    }
  );

  return handleResponse(response);
}

export async function sendChatMessage(
  token,
  message,
  sessionId = null
) {
  const response = await fetch(
    `${API_BASE_URL}/api/chat`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        message,
        sessionId,
      }),
    }
  );

  return handleResponse(response);
}

export async function getChatSessions(
  token
) {
  const response = await fetch(
    `${API_BASE_URL}/api/chat/sessions`,
    {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  return handleResponse(response);
}

export async function getChatMessages(
  token,
  sessionId
) {
  const response = await fetch(
    `${API_BASE_URL}/api/chat/sessions/${sessionId}/messages`,
    {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  return handleResponse(response);
}

export async function getAdminDocuments(
  token
) {
  const response = await fetch(
    `${API_BASE_URL}/api/admin/documents`,
    {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  return handleResponse(response);
}

export async function uploadAdminDocument(
  token,
  file,
  title,
  department,
  version,
  accessLevel
) {
  const formData = new FormData();

  formData.append('file', file);
  formData.append('title', title);
  formData.append('department', department);
  formData.append('version', version);
  formData.append('accessLevel', accessLevel);

  const response = await fetch(
    `${API_BASE_URL}/api/admin/documents/upload`,
    {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: formData,
    }
  );

  return handleResponse(response);
}

export async function sendPublicChatMessage(
  message,
  widgetPublicKey,
  sessionId = null
) {
  const response = await fetch(
    `${API_BASE_URL}/api/public/chat`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        message,
        widgetPublicKey,
        sessionId,
      }),
    }
  );

  return handleResponse(response);
}

export async function deactivateAdminDocument(
  token,
  documentId
) {
  const response = await fetch(
    `${API_BASE_URL}/api/admin/documents/${documentId}`,
    {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  return handleResponse(response);
}