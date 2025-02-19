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

export const useCartStore = create<CartState>((set) => ({
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
            //  Artikel sofort in die UI setzen (ohne Wartezeit)
            set((state) => ({
                items: [...state.items, item],
                totalPrice: state.totalPrice + item.price * item.quantity,
            }));

            //  API-Anfrage im Hintergrund senden
            await addToCart(token, userEmail, item);
        } catch (error) {
            console.error("Fehler beim Hinzufügen zum Warenkorb:", error);
        }
    },


    updateItemQuantity: async (token, userEmail, productId, quantity) => {
        set((state) => {
            const updatedItems = state.items.map((item) =>
                item.productId === productId ? { ...item, quantity } : item
            );
            return {
                items: updatedItems,
                totalPrice: updatedItems.reduce((sum, item) => sum + item.price * item.quantity, 0), // ✅ `totalPrice` direkt aktualisieren
            };
        });

        await updateQuantity(token, userEmail, productId, quantity);
    },



    removeItem: async (token, userEmail, productId) => {
        try {
            //  Sofort das Produkt aus dem UI entfernen
            set((state) => ({
                items: state.items.filter((item) => item.productId !== productId),
                totalPrice: state.items
                    .filter((item) => item.productId !== productId)
                    .reduce((sum, item) => sum + item.price * item.quantity, 0),
            }));

            //  API-Anfrage im Hintergrund senden
            await removeItem(token, userEmail, productId);
        } catch (error) {
            console.error("Fehler beim Entfernen des Artikels:", error);
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
