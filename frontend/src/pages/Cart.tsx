import { useEffect } from "react";
import { useCartStore } from "../store/cartStore";
import { useAuthStore } from "../store/authStore";
import { useNavigate } from "react-router-dom"; //  Navigations-Hook für Checkout-Seite

export default function Cart() {
    const {token} = useAuthStore();
    const userEmail = useAuthStore((state) => state.tokenEmail);
    const {items, totalPrice, fetchCart, updateItemQuantity, removeItem, clearCart} = useCartStore();
    const navigate = useNavigate();

    useEffect(() => {
        if (token && userEmail) {
            fetchCart(token, userEmail);
        }
    }, [token, userEmail, fetchCart]);
    return (
        <div className="page-container">
            <div className={`cart-container ${items.length === 0 ? "hidden" : ""}`}>
                <h2 className="cart-title">🛒 Mein Warenkorb</h2>

                {items.length === 0 ? (
                    <p className="cart-empty">Dein Warenkorb ist leer.</p>
                ) : (
                    <div className="cart-list">
                        {items.map((item) => (
                            <div key={item.productId} className="cart-item">
                                <img
                                    src={(Array.isArray(item.images) && item.images.length > 0) ? item.images[0] : "/placeholder.png"}
                                    alt={item.name}
                                    className="cart-item-image"/>


                                <div className="cart-item-info">
                                    <h3 className="cart-item-name">{item.name}</h3>
                                    <p className="cart-item-price">{item.price.toFixed(2)} €</p>
                                    <div className="cart-quantity">
                                        <button
                                            onClick={() => updateItemQuantity(token!, userEmail!, item.productId, Math.max(item.quantity - 1, 1))}
                                            className="cart-btn">➖
                                        </button>
                                        <span className="cart-quantity-text">{item.quantity}</span>
                                        <button
                                            onClick={() => updateItemQuantity(token!, userEmail!, item.productId, item.quantity + 1)}
                                            className="cart-btn">➕
                                        </button>
                                    </div>
                                </div>

                                <button onClick={() => removeItem(token!, userEmail!, item.productId)}
                                        className="cart-remove-btn">🗑 Entfernen
                                </button>
                            </div>
                        ))}

                        <div className="cart-summary">
                            <h3 className="cart-total">Gesamt: {totalPrice.toFixed(2)} €</h3>

                            <div className="cart-actions">
                                <button onClick={() => clearCart(token!, userEmail!)}
                                        className="cart-clear-btn">🗑 Warenkorb leeren
                                </button>

                                <button onClick={() => navigate("/checkout")}
                                        className="cart-checkout-btn">
                                    Bestellung abschließen
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>

        </div>
    );
}