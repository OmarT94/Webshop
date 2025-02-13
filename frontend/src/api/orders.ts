import axios from "axios";

const API_URL = "/api/orders";

export type OrderItem = {
    productId: string;
    name: string;
    imageBase64: string;
    quantity: number;
    price: number;
};

export type Address = {
    street: string;
    city: string;
    postalCode: string;
    country: string;
};

export enum PaymentStatus {
    PAID = "PAID",
    PENDING = "PENDING",
    REFUNDED = "REFUNDED"
}

export enum OrderStatus {
    PROCESSING = "PROCESSING",
    SHIPPED = "SHIPPED",
    CANCELLED = "CANCELLED",
    RETURN_REQUESTED="RETURN_REQUESTED",
    RETURNED="RETURNED",
}

export type Order = {
    id: string;
    userEmail: string;
    items: OrderItem[]; //  Eigener Typ für Bestellartikel
    totalPrice: number;
    shippingAddress: Address;
    paymentStatus: PaymentStatus; //  Enum für Zahlungsstatus
    orderStatus: OrderStatus; //  Enum für Bestellstatus
    paymentMethod: "PAYPAL" | "KLARNA" | "CREDIT_CARD" | "BANK_TRANSFER" | "SOFORT" | "SEPA";
    stripePaymentIntentId: string;

};

//  Hilfsfunktion für den Auth-Header
const getAuthHeader = () => {
    const token = localStorage.getItem("token");
    return token? { Authorization: `Bearer ${token}` }:{};
};

//  Bestellung aufgeben (mit Stripe)
export const checkout = async (
    token: string,
    userEmail: string,
    paymentIntentId: string,
    paymentMethod: string,
    shippingAddress: Address
): Promise<Order> => {
    try {
        const response = await axios.post(
            `${API_URL}/${userEmail}/checkout`,
            shippingAddress,  //  Nur shippingAddress im Body
            {
                headers: { Authorization: `Bearer ${token}` },
                params: { paymentIntentId, paymentMethod } //  paymentIntentId als Query-Parameter senden
            }
        );
        return response.data;
    } catch (error) {
        console.error(" Fehler beim Checkout:", error);
        throw new Error(" Bestellung konnte nicht abgeschlossen werden.");
    }
};


//  Bestellungen eines Nutzers abrufen
export const getUserOrders = async (userEmail: string): Promise<Order[]> => {
   try{
    const response = await axios.get(`${API_URL}/${userEmail}`, {
        headers: {
            ...getAuthHeader(),
            "Content-Type": "application/json",
        },
    });
    return response.data;
   }catch(error){
       console.error(" Fehler beim Checkout:", error);
       throw new Error("Bestellung konnte nicht abgeschlossen werden.");
   }
};

//  Bestellung stornieren (nur für Nutzer)
export const cancelOrder = async (orderId: string) => {
    try {
        const response = await axios.delete(`${API_URL}/${orderId}/cancel`, {
            headers: {
                ...getAuthHeader(),
                "Content-Type": "application/json",
            },
        });
        return response.data; //  Gibt Erfolgsmeldung zurück
    } catch (error) {
        console.error("Fehler beim Stornieren der Bestellung:", error);
        throw new Error("Bestellung konnte nicht storniert werden.");
    }
};

//  Rückgabe beantragen
export const requestReturn = async (token: string, orderId: string) => {
    try {
        console.log("API-Call für `requestReturn`: Token:", token);
        console.log("API-Call für `requestReturn`: orderId:", orderId);

        const response = await axios.put(
            `${API_URL}/${orderId}/return_request`,
            {}, // Kein Body benötigt
            {
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            }
        );

        console.log(" Erfolgreiche Rückgabe-Anfrage:", response.data);
        return true;
    } catch (error: any) {
        console.error("Fehler bei requestReturn:", error.response?.data || error.message);
        return false;
    }
};


//  Admin genehmigt Rückgabe
export const approveReturn = async (token: string, orderId: string): Promise<boolean> => {
    const response = await axios.put(`${API_URL}/${orderId}/approve-return`, {}, {
        headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
};


// Alle Bestellungen abrufen (nur für Admins)
export const getAllOrders = async (): Promise<Order[]> => {
    const response = await axios.get(API_URL, {
        headers: getAuthHeader(),
    });
    return response.data;
};

// Bestellstatus aktualisieren (nur für Admins)
export const updateOrderStatus = async (orderId: string, status: string): Promise<Order> => {
    const response = await axios.put(`${API_URL}/${orderId}/status`, {}, {
        headers: {
            ...getAuthHeader(),
            "Content-Type": "application/json",
        },
        params: { status },
    });
    return response.data;
};

//  Zahlungsstatus aktualisieren (nur für Admins)
export const updatePaymentStatus = async (orderId: string, paymentStatus: string): Promise<Order> => {
    const response = await axios.put(`${API_URL}/${orderId}/payment`, {}, {
        headers: {
            ...getAuthHeader(),
            "Content-Type": "application/json",
        },
        params: { paymentStatus },
    });
    return response.data;
};

//  Lieferadresse aktualisieren (nur für Admins)
export const updateShippingAddress = async (orderId: string, newAddress: Address): Promise<Order> => {
    const response = await axios.put(`${API_URL}/${orderId}/address`, newAddress, {
        headers: {
            ...getAuthHeader(),
            "Content-Type": "application/json",
        },
    });
    return response.data;
};

//  Bestellung löschen (nur für Admins)
export const deleteOrder = async (orderId: string): Promise<void> => {
    await axios.delete(`${API_URL}/${orderId}`, {
        headers: getAuthHeader(),
    });
};


//  Bestellungen nach E-Mail suchen
export const searchOrdersByEmail = async (email: string): Promise<any[]> => {
    const token = localStorage.getItem("token");
    if (!token) {
        throw new Error(" Kein Auth-Token verfügbar!");
    }
    const response = await axios.get(`${API_URL}/search/email`, {
        params: { email },
        headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
};

//  Bestellungen nach Status suchen
export const searchOrdersByStatus = async (status: string): Promise<any[]> => {
    const token = localStorage.getItem("token");
    if (!token) {
        throw new Error(" Kein Auth-Token verfügbar!");
    }
    const response = await axios.get(`${API_URL}/search/status`, {
        params: { status },
        headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
};

//  Bestellungen nach Zahlungsstatus suchen
export const searchOrdersByPaymentStatus = async (paymentStatus: string): Promise<any[]> => {
    const token = localStorage.getItem("token");
    if (!token) {
        throw new Error(" Kein Auth-Token verfügbar!");
    }
    const response = await axios.get(`${API_URL}/search/paymentStatus`, {
        params: { paymentStatus },
        headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
};
