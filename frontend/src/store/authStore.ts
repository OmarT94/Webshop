import { create } from "zustand";
import {jwtDecode} from "jwt-decode";


type AuthState = {
    token: string | null;
    isAdmin: boolean;
    tokenEmail: string | null;
    setToken: (token: string) => void;
    logout: () => void;
    restoreSession: () => void;
};

export const useAuthStore = create<AuthState>((set) => ({
    token: localStorage.getItem("token"),
    isAdmin: false,
    tokenEmail: localStorage.getItem("tokenEmail"),

    setToken: (token) => {
        localStorage.setItem("token", token);
        const decoded: any = jwtDecode(token);
        set({
            token,
            isAdmin: decoded.role === "ROLE_ADMIN",
            tokenEmail: decoded.email,
        });
        localStorage.setItem("tokenEmail", decoded.email); // 🆕 E-Mail speichern
    },

    logout: () => {
        localStorage.removeItem("token");
        localStorage.removeItem("tokenEmail");
        set({ token: null, isAdmin: false, tokenEmail: null });
    },

    restoreSession: () => {
        const storedToken = localStorage.getItem("token");
        const storedEmail = localStorage.getItem("tokenEmail");

        if (storedToken && storedEmail) {
            const decoded: any = jwtDecode(storedToken);
            set({
                token: storedToken,
                isAdmin: decoded.role === "ROLE_ADMIN",
                tokenEmail: storedEmail, //  E-Mail aus `localStorage`
            });
        }
    },
}));
