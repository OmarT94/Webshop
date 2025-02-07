import axios from "axios";
import {OrderItem} from "./orders.ts";

const API_URL = "/api/cart";

export type Cart = {
    id: string;
    userEmail: string;
    items: OrderItem[];
};

//  Hilfsfunktion für den Auth-Header mit Fehlervermeidung
const getAuthHeader = (token: string | null) => {
    if (!token) {
        throw new Error("Kein Token vorhanden – Benutzer ist nicht eingeloggt!");
    }
    return { Authorization: `Bearer ${token}`, "Content-Type": "application/json" };
};

//  Warenkorb abrufen
export const getCart = async (token: string, userEmail: string): Promise<Cart> => {
    try {
        const response = await axios.get(`${API_URL}/${userEmail}`, {
            headers: getAuthHeader(token),
        });
        return response.data;
    } catch (error) {
        console.error(" Fehler beim Abrufen des Warenkorbs:", error);
        throw error;
    }
};

//  Produkt in den Warenkorb legen
export const addToCart = async (token: string, userEmail: string, product: OrderItem) => {
    try {
        const response = await axios.post(`${API_URL}/${userEmail}/add`, product, {
            headers: getAuthHeader(token),
        });
        return response.data;
    } catch (error) {
        console.error(" Fehler beim Hinzufügen zum Warenkorb:", error);
        throw error;
    }
};

//  Menge aktualisieren
export const updateQuantity = async (token: string, userEmail: string, productId: string, quantity: number) => {
    try {
        const response = await axios.put(`${API_URL}/${userEmail}/update/${productId}?quantity=${quantity}`, {}, {
            headers: getAuthHeader(token),
        });
        return response.data;
    } catch (error) {
        console.error(" Fehler beim Aktualisieren der Menge:", error);
        throw error;
    }
};

//  Artikel entfernen
export const removeItem = async (token: string, userEmail: string, productId: string) => {
    try {
        await axios.delete(`${API_URL}/${userEmail}/remove/${productId}`, {
            headers: getAuthHeader(token),
        });
    } catch (error) {
        console.error(" Fehler beim Entfernen des Artikels:", error);
        throw error;
    }
};

//  Warenkorb leeren
export const clearCart = async (token: string, userEmail: string) => {
    try {
        await axios.delete(`${API_URL}/${userEmail}/clear`, {
            headers: getAuthHeader(token),
        });
    } catch (error) {
        console.error("Fehler beim Leeren des Warenkorbs:", error);
        throw error;
    }
};
