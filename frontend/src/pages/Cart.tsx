import { useEffect } from "react";
import { useCartStore } from "../store/cartStore";
import { useAuthStore } from "../store/authStore";
import { useNavigate } from "react-router-dom"; //  Navigations-Hook für Checkout-Seite

export default function Cart() {
    const { token } = useAuthStore();
    const userEmail = useAuthStore((state) => state.tokenEmail);
    const { items, totalPrice, fetchCart, updateItemQuantity, removeItem, clearCart } = useCartStore();
    const navigate = useNavigate(); //  Navigation aktivieren

    useEffect(() => {
        if (token && userEmail) {
            fetchCart(token, userEmail);
        }
    }, [token, userEmail, fetchCart]);

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold">🛒 Mein Warenkorb</h2>

            {items.length === 0 ? (
                <p className="text-gray-500 mt-4">Dein Warenkorb ist leer.</p>
            ) : (
                <div className="mt-6">
                    {items.map((item) => (
                        <div key={item.productId} className="flex items-center gap-4 border-b py-4">
                            <img src={item.imageBase64} alt={item.name} className="w-20 h-20 object-cover" />
                            <div className="flex-1">
                                <h3 className="text-lg font-semibold">{item.name}</h3>
                                <p className="text-gray-600">{item.price.toFixed(2)} €</p>
                                <div className="flex items-center gap-2">
                                    <button onClick={() => updateItemQuantity(token!, userEmail!, item.productId, Math.max(item.quantity - 1, 1))}
                                            className="p-2 bg-gray-300 rounded">➖</button>
                                    <span className="font-bold">{item.quantity}</span>
                                    <button onClick={() => updateItemQuantity(token!, userEmail!, item.productId, item.quantity + 1)}
                                            className="p-2 bg-gray-300 rounded">➕</button>
                                </div>
                            </div>
                            <button onClick={() => removeItem(token!, userEmail!, item.productId)}
                                    className="p-2 bg-red-500 text-white rounded">🗑 Entfernen</button>
                        </div>
                    ))}

                    <div className="mt-6 flex flex-col items-center border-t pt-4">
                        <h3 className="text-xl font-bold">Gesamt: {totalPrice.toFixed(2)} €</h3>

                        <div className="flex gap-4 mt-4">
                            <button onClick={() => clearCart(token!, userEmail!)}
                                    className="p-2 bg-red-500 text-white rounded">🗑 Warenkorb leeren
                            </button>

                            {/*  Checkout-Button hinzugefügt */}
                            <button
                                onClick={() => {
                                    console.log("Navigiere zur Checkout-Seite...");
                                    navigate("/checkout");
                                }}
                                className="p-2 bg-green-500 text-white rounded"
                            >
                                Bestellung abschließen
                            </button>

                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
