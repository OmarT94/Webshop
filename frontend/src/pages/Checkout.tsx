import { useState } from "react";
import { useCartStore } from "../store/cartStore";
import { useAuthStore } from "../store/authStore";
import { checkout } from "../api/orders";
import { useNavigate } from "react-router-dom";

export default function Checkout() {
    const { token } = useAuthStore();
    const userEmail = useAuthStore((state) => state.tokenEmail);
    const { items, totalPrice, clearCart } = useCartStore();
    const navigate = useNavigate();

    const [address, setAddress] = useState({
        street: "",
        city: "",
        postalCode: "",
        country: "",
    });

    const handleCheckout = async () => {
        if (!token || !userEmail) return;
        try {
            await checkout(token, userEmail, address);
            clearCart(token, userEmail);
            alert("Bestellung erfolgreich!");
            navigate("/orders");
        } catch (error) {
            console.error("Fehler beim Checkout:", error);
            alert("Fehler beim Bestellen.");
        }
    };

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold">🛒 Bestellübersicht</h2>
            {items.length === 0 ? (
                <p className="text-gray-500 mt-4">Dein Warenkorb ist leer.</p>
            ) : (
                <div className="mt-6">
                    <h3 className="text-xl font-semibold">Lieferadresse</h3>
                    <input type="text" placeholder="Straße" value={address.street}
                           onChange={(e) =>
                               setAddress({ ...address, street: e.target.value })} />
                    <input type="text" placeholder="Stadt" value={address.city}
                           onChange={(e) =>
                               setAddress({ ...address, city: e.target.value })} />
                    <input type="text" placeholder="PLZ" value={address.postalCode}
                           onChange={(e) =>
                               setAddress({ ...address, postalCode: e.target.value })} />
                    <input type="text" placeholder="Land" value={address.country}
                           onChange={(e) =>
                               setAddress({ ...address, country: e.target.value })} />

                    <h3 className="text-xl font-bold mt-4">Gesamt: {totalPrice.toFixed(2)} €</h3>
                    <button className="p-2 bg-green-500 text-white rounded mt-2"
                            onClick={handleCheckout}> Bestellung abschließen</button>
                </div>
            )}
        </div>
    );
}
