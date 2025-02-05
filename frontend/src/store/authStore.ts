import { create } from "zustand";
import { jwtDecode } from "jwt-decode";

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
    tokenEmail: localStorage.getItem("tokenEmail") || null,

    setToken: (token) => {
        try {
            const decoded: any = jwtDecode(token);
            console.log(" Decoded Token Inhalt:", decoded); // 🔍 Zeigt alle Felder

            if (!decoded.email) {
                console.error(" Fehler: Token enthält keine `email`-Eigenschaft!");
                console.warn(" Vielleicht heißt das Feld `sub` oder `username`?");
                return;
            }

            localStorage.setItem("token", token);
            localStorage.setItem("tokenEmail", decoded.email);

            set({
                token,
                isAdmin: decoded.role === "ROLE_ADMIN",
                tokenEmail: decoded.email,
            });

            console.log(" Token gespeichert:", localStorage.getItem("token"));
            console.log(" Email gespeichert:", localStorage.getItem("tokenEmail"));
        } catch (error) {
            console.error(" Fehler beim Decodieren des Tokens:", error);
        }
    },



    logout: () => {
        localStorage.removeItem("token");
        localStorage.removeItem("tokenEmail");
        set({ token: null, isAdmin: false, tokenEmail: null });
    },

    restoreSession: () => {
        console.log(" restoreSession gestartet...");

        const storedToken = localStorage.getItem("token");
        const storedEmail = localStorage.getItem("tokenEmail");

        console.log(" Gefundener Token:", storedToken);
        console.log(" Gefundene Email:", storedEmail);

        if (storedToken && storedEmail) {
            try {
                const decoded: any = jwtDecode(storedToken);
                set({
                    token: storedToken,
                    isAdmin: decoded.role === "ROLE_ADMIN",
                    tokenEmail: storedEmail,
                });
                console.log("Sitzung erfolgreich wiederhergestellt:", decoded);
            } catch (error) {
                console.error(" Fehler beim Wiederherstellen der Sitzung:", error);
            }
        } else {
            console.warn(" Kein Token oder Email in localStorage gefunden.");
        }
    },


}));
