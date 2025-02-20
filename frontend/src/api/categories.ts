import axios from "axios";
import {useAuthStore} from "../store/authStore.ts";

const API_URL = "/api/categories";



// Alle Kategorien abrufen
export const getCategories = async () => {
    try {
        const token = useAuthStore.getState().token; // Hole Token
        if (!token) {
            throw new Error("Kein Token gefunden! Bist du eingeloggt?");
        }

        const response = await axios.get(API_URL, {
            headers: {
                Authorization: `Bearer ${token}` // Token mitsenden
            }
        });

        return response.data;
    } catch (error: any) {
        console.error("Fehler beim Abrufen der Kategorien:", error);
        throw error;
    }
};


// Neue Kategorie hinzufügen (Fix für Authorization-Header)
export const addCategory = async (name: string) => {
    try {
        const token = useAuthStore.getState().token; // Hole Token
        if (!token) {
            throw new Error("Kein Token gefunden! Bist du eingeloggt?");
        }

        const response = await axios.post(`${API_URL}/add`, { name }, { // `name` als Objekt senden!
            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        return response.data;
    } catch (error: any) {
        console.error("Fehler beim Hinzufügen der Kategorie:", error);
        throw error;
    }
};

