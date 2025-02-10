import { useState } from "react";

import { useCartStore } from "../store/cartStore";
import { useAuthStore } from "../store/authStore";
import { PaymentMethod } from "../api/orders";
import { useNavigate } from "react-router-dom";
import {useCheckoutStore} from "../store/checkoutStore.ts";

export default function Checkout() {
    const { token } = useAuthStore();
    const userEmail = useAuthStore((state) => state.tokenEmail);
    const { items, totalPrice, clearCart } = useCartStore();
    const { paymentMethod, shippingAddress, setPaymentMethod, setShippingAddress, placeOrder } = useCheckoutStore();
    const navigate = useNavigate();

    const [loading, setLoading] = useState(false);

    const handleCheckout = async () => {
        if (!token || !userEmail) return;
        setLoading(true);
        try {
            await placeOrder(token, userEmail);
            clearCart(token, userEmail);
            alert("✅ Bestellung erfolgreich abgeschlossen!");
            navigate("/orders");
        } catch (error) {
            console.error("Fehler beim Checkout:", error);
            alert(" Fehler beim Bestellen.");
        }
        setLoading(false);
    };

    return (
        <div className="p-6 max-w-lg mx-auto">
            <h2 className="text-2xl font-bold text-center">🛒 Checkout</h2>

            {items.length === 0 ? (
                <p className="text-gray-500 mt-4 text-center">Dein Warenkorb ist leer.</p>
            ) : (
                <div className="mt-6">
                    <h3 className="text-lg font-semibold">Lieferadresse</h3>
                    <input type="text" placeholder="Straße" value={shippingAddress.street}
                           onChange={(e) => setShippingAddress({ ...shippingAddress, street: e.target.value })}
                           className="w-full p-2 border rounded mt-2" />
                    <input type="text" placeholder="Stadt" value={shippingAddress.city}
                           onChange={(e) => setShippingAddress({ ...shippingAddress, city: e.target.value })}
                           className="w-full p-2 border rounded mt-2" />
                    <input type="text" placeholder="PLZ" value={shippingAddress.postalCode}
                           onChange={(e) => setShippingAddress({ ...shippingAddress, postalCode: e.target.value })}
                           className="w-full p-2 border rounded mt-2" />
                    <input type="text" placeholder="Land" value={shippingAddress.country}
                           onChange={(e) => setShippingAddress({ ...shippingAddress, country: e.target.value })}
                           className="w-full p-2 border rounded mt-2" />

                    <h3 className="text-lg font-semibold mt-4">Zahlungsmethode</h3>
                    <select value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value as PaymentMethod)}
                            className="w-full p-2 border rounded mt-2">
                        {Object.values(PaymentMethod).map((method) => (
                            <option key={method} value={method}>{method}</option>
                        ))}
                    </select>

                    <h3 className="text-xl font-bold mt-4">Gesamt: {totalPrice.toFixed(2)} €</h3>
                    <button className="w-full p-2 bg-green-500 text-white rounded mt-2"
                            onClick={handleCheckout} disabled={loading}>
                        {loading ? "🕐 Bestellung wird verarbeitet..." : " Bestellung abschließen"}
                    </button>
                </div>
            )}
        </div>
    );
}
