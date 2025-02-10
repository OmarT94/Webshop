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
}

export enum PaymentMethod {
    PAYPAL = "PAYPAL",
    KLARNA = "KLARNA",
    CREDIT_CARD = "CREDIT_CARD",
    BANK_TRANSFER = "BANK_TRANSFER",
}

export enum OrderStatus {
    PROCESSING = "PROCESSING",
    SHIPPED = "SHIPPED",
    CANCELLED = "CANCELLED",
}

export type Order = {
    id: string;
    userEmail: string;
    items: OrderItem[]; //  Eigener Typ für Bestellartikel
    totalPrice: number;
    shippingAddress: Address;
    paymentStatus: PaymentStatus; //  Enum für Zahlungsstatus
    orderStatus: OrderStatus; //  Enum für Bestellstatus
    paymentMethod: PaymentMethod;
};

//  Hilfsfunktion für den Auth-Header
const getAuthHeader = () => {
    const token = localStorage.getItem("token");
    return token? { Authorization: `Bearer ${token}` }:{};
};

// Bestellungen eingeben
export const checkout = async (
    token: string, userEmail: string, shippingAddress: Address,paymentMethod: string) => {
    const response = await axios.post(`${API_URL}/${userEmail}/checkout`, {shippingAddress, paymentMethod}, {
        headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
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
