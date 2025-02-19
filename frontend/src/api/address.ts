import axios from "axios";
import {Address} from "./orders.ts";

const API_URL = "/api/users"; // Basis-URL für Nutzer-Endpunkte

//  Abrufen aller Adressen des Nutzers
export const getAddresses = async (email: string) => {
    const token = localStorage.getItem("token");
    if (!token) {
        console.error(" Kein Token gefunden! Ist der Benutzer eingeloggt?");
        return;
    }

    try {
        console.log(`Abrufen von Adressen für: ${email}`);
        const response = await axios.get(`${API_URL}/${email}/addresses`, {
            headers: { Authorization: `Bearer ${token}` },
        });

        console.log("Adressen erfolgreich geladen:", response.data);

        return response.data.map((address: any) => ({
            ...address,
            id: address.id || "", // Falls `id` fehlt, als leeren String initialisieren
        }));
    } catch (error) {
        console.error(" Fehler beim Abrufen der Adressen:", error);
        throw error;
    }
};


//  Neue Adresse hinzufügen
export const addAddress = async (email: string, address: any) => {
    const token = localStorage.getItem("token");  // Token holen
    if (!token) {
        console.error("Kein Token gefunden! Ist der Benutzer eingeloggt?");
        return;
    }

    try {
        console.log(` API-Anfrage an: ${API_URL}/${email}/addresses`);
        console.log(" Sende Daten:", address);

        const response = await axios.post(`${API_URL}/${email}/addresses`, address, {
            headers: { Authorization: `Bearer ${token}` },
        });

        console.log(" Adresse erfolgreich hinzugefügt:", response.data);
        return response.data;
    } catch (error: any) {
        console.error(" Fehler beim Hinzufügen der Adresse:", error.response?.data || error.message);
        throw error;
    }
};

//  Adresse aktualisieren
export const updateAddress = async (email: string, addressId: string, address: Address) => {
    if (!addressId || addressId.trim() === "") {
        console.error("Ungültige Address-ID:", addressId);
        return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
        console.error(" Kein Token gefunden!");
        return;
    }

    try {
        console.log(` UPDATE Anfrage an: /api/users/${email}/addresses/${addressId}`);
        console.log(" Sende Daten:", address);

        const response = await axios.put(`${API_URL}/${email}/addresses/${addressId}`, address, {
            headers: { Authorization: `Bearer ${token}` },
        });

        console.log(" Adresse erfolgreich aktualisiert:", response.data);
        return response.data;
    } catch (error: any) {
        console.error("API-Fehler beim Aktualisieren der Adresse:", error.response?.data || error.message);
        throw error;
    }
};


//  Adresse löschen
export const deleteAddress = async (email: string, addressId: string) => {
    const token = localStorage.getItem("token");
    if (!token) {
        console.error(" Kein Token gefunden!");
        return;
    }

    try {
        console.log(`📡 DELETE Anfrage an API: /api/users/${email}/addresses/${addressId}`);

        const response = await axios.delete(`${API_URL}/${email}/addresses/${addressId}`, {
            headers: { Authorization: `Bearer ${token}` },
        });

        console.log(" Adresse erfolgreich gelöscht:", response.data);
        return response.data;
    } catch (error: any) {
        console.error(" API-Fehler beim Löschen der Adresse:", error.response?.data || error.message);
        throw error;
    }
};












