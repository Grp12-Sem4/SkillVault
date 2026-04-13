import { createContext, useContext, useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';

const AuthContext = createContext(undefined);
const TOKEN_STORAGE_KEY = 'token';
const AUTH_LOGOUT_EVENT = 'auth:logout';

function decodeUserFromToken(token) {
  try {
    const decodedToken = jwtDecode(token);
    const expiresAt = typeof decodedToken.exp === 'number' ? decodedToken.exp * 1000 : null;

    if (expiresAt && expiresAt <= Date.now()) {
      return null;
    }

    return {
      email: decodedToken.email ?? decodedToken.sub ?? null,
      role: decodedToken.role ?? decodedToken.roles ?? null,
      sub: decodedToken.sub ?? decodedToken.email ?? null,
      exp: decodedToken.exp ?? null,
      iat: decodedToken.iat ?? null,
    };
  } catch {
    return null;
  }
}

function restoreAuthState() {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);

  if (!token) {
    return {
      user: null,
      isAuthenticated: false,
    };
  }

  const decodedUser = decodeUserFromToken(token);

  if (!decodedUser) {
    localStorage.removeItem(TOKEN_STORAGE_KEY);

    return {
      user: null,
      isAuthenticated: false,
    };
  }

  return {
    user: decodedUser,
    isAuthenticated: true,
  };
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => restoreAuthState().user);
  const [isAuthenticated, setIsAuthenticated] = useState(() => restoreAuthState().isAuthenticated);

  const login = (token) => {
    const decodedUser = decodeUserFromToken(token);

    if (!decodedUser) {
      logout();
      return false;
    }

    localStorage.setItem(TOKEN_STORAGE_KEY, token);
    setUser(decodedUser);
    setIsAuthenticated(true);

    return true;
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    setUser(null);
    setIsAuthenticated(false);
  };

  useEffect(() => {
    const restoredState = restoreAuthState();
    setUser(restoredState.user);
    setIsAuthenticated(restoredState.isAuthenticated);

    const handleLogout = () => {
      setUser(null);
      setIsAuthenticated(false);
    };

    const handleStorage = (event) => {
      if (event.key === TOKEN_STORAGE_KEY) {
        const nextState = restoreAuthState();
        setUser(nextState.user);
        setIsAuthenticated(nextState.isAuthenticated);
      }
    };

    window.addEventListener(AUTH_LOGOUT_EVENT, handleLogout);
    window.addEventListener('storage', handleStorage);

    return () => {
      window.removeEventListener(AUTH_LOGOUT_EVENT, handleLogout);
      window.removeEventListener('storage', handleStorage);
    };
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }

  return context;
}

export default AuthContext;
