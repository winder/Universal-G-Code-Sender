export interface AuthStatus {
  authEnabled: boolean;
  authenticated: boolean;
  username?: string;
}

export const getAuthStatus = async (): Promise<AuthStatus> => {
  const response = await fetch("/api/v1/auth/status");
  if (!response.ok) {
    throw new Error("Failed to check auth status");
  }
  return response.json();
};

export const login = async (
  username: string,
  password: string
): Promise<AuthStatus> => {
  const response = await fetch("/api/v1/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.error || "Invalid username or password");
  }
  return response.json();
};

export const logout = async (): Promise<void> => {
  await fetch("/api/v1/auth/logout", { method: "POST" });
};
