import {create} from "zustand";
import { getCart, addToCart, updateQuantity, removeItem, clearCart } from "../api/cart";
import { OrderItem } from "../api/orders.ts";


type CartState = {
    items: OrderItem[];
    totalPrice: number;
    fetchCart: (token: string, userEmail: string) => Promise<void>;
    addItem: (token: string, userEmail: string, item: OrderItem) => Promise<void>;
    updateItemQuantity: (token: string, userEmail: string, productId: string, quantity: number) => Promise<void>;
    removeItem: (token: string, userEmail: string, productId: string) => Promise<void>;
    clearCart: (token: string, userEmail: string) => Promise<void>;
};

export const useCartStore = create<CartState>((set, get) => ({
    items: [],
    totalPrice: 0,

    fetchCart: async (token, userEmail) => {  //  Token hinzugefügt
        try {
            const cart = await getCart(token, userEmail);
            set({
                items: cart.items,
                totalPrice: cart.items.reduce((sum, item) => sum + item.price * item.quantity, 0),
            });
        } catch (error) {
            console.error(" Fehler beim Laden des Warenkorbs:", error);
        }
    },

    addItem: async (token, userEmail, item) => {
        try {
            await addToCart(token, userEmail, item);
            get().fetchCart(token, userEmail);
        } catch (error) {
            console.error(" Fehler beim Hinzufügen zum Warenkorb:", error);
        }
    },

    updateItemQuantity: async (token, userEmail, productId, quantity) => {
        try {
            await updateQuantity(token, userEmail, productId, quantity);
            get().fetchCart(token, userEmail);
        } catch (error) {
            console.error(" Fehler beim Aktualisieren der Menge:", error);
        }
    },

    removeItem: async (token, userEmail, productId) => {
        try {
            await removeItem(token, userEmail, productId);
            get().fetchCart(token, userEmail);
        } catch (error) {
            console.error(" Fehler beim Entfernen des Artikels:", error);
        }
    },

    clearCart: async (token, userEmail) => {
        try {
            await clearCart(token, userEmail);
            set({ items: [], totalPrice: 0 });
        } catch (error) {
            console.error(" Fehler beim Leeren des Warenkorbs:", error);
        }
    },
}));
