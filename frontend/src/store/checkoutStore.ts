import { create } from "zustand";
import {Address, checkout} from "../api/orders";
import { PaymentMethod } from "../api/orders";

type CheckoutState = {
    paymentMethod: PaymentMethod;
    shippingAddress: Address;
    setPaymentMethod: (method: PaymentMethod) => void;
    setShippingAddress: (address: Address) => void;
    placeOrder: (token: string, userEmail: string) => Promise<void>;
};

export const useCheckoutStore = create<CheckoutState>((set, get) => ({
    paymentMethod: PaymentMethod.PAYPAL, // Standardwert
    shippingAddress: { street: "", city: "", postalCode: "", country: "" },

    setPaymentMethod: (method) => set({ paymentMethod: method }),
    setShippingAddress: (address) => set({ shippingAddress: address }),

    placeOrder: async (token, userEmail) => {
        const { shippingAddress, paymentMethod } = get();
        await checkout(token, userEmail, shippingAddress, paymentMethod);
    },
}));
