import { useState, useEffect } from "react";
import { useCartStore } from "../store/cartStore";
import { useAuthStore } from "../store/authStore";
import { checkout } from "../api/orders";
import { loadStripe } from "@stripe/stripe-js";
import { Elements, useStripe, useElements, CardElement } from "@stripe/react-stripe-js";
import { useNavigate } from "react-router-dom";


//  Stripe öffentliches API-Key aus Umgebungsvariablen holen
const stripeKey = import.meta.env.VITE_STRIPE_PUBLIC_KEY;

if (!stripeKey) {
    console.error(" Stripe Public Key fehlt!");
}

const stripePromise = loadStripe(stripeKey);

{/*
 Dieser Code sendet eine Anfrage an das Backend, um eine Stripe-Zahlung zu starten.
 Backend muss einen clientSecret für die Zahlung zurückgeben, den Stripe nutzt.
*/}
const fetchClientSecret = async () => {
    const token = useAuthStore.getState().token;
    const totalPrice = useCartStore.getState().totalPrice;

    if (!token || totalPrice === 0) {
        console.error("Token oder totalPrice fehlt!");
        return null;
    }

    try {
        const response = await fetch("/api/stripe/create-payment-intent", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`
            },
            body: JSON.stringify({
                amount: totalPrice * 100, // Betrag in Cent
                currency: "eur",
                paymentMethodType: "card"
            })
        });

        const data = await response.json();
        return data.clientSecret;
    } catch (error) {
        console.error("Fehler beim Abrufen des Client Secret:", error);
        return null;
    }
};


{/* Stripe Elements als Wrapper geladen */}
export default function Checkout() {
    return (
        <Elements stripe={stripePromise}>
            <CheckoutForm />
        </Elements>
    );
}

function CheckoutForm() {
    const stripe = useStripe();
    const elements = useElements();
    const navigate = useNavigate();

    const { token } = useAuthStore();
    const userEmail = useAuthStore((state) => state.tokenEmail);
    const {  totalPrice, fetchCart, clearCart } = useCartStore();

    const [shippingAddress, setShippingAddress] = useState({
        street: "",
        city: "",
        postalCode: "",
        country: "",
    });

    const [paymentMethod, setPaymentMethod] = useState("CREDIT_CARD");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (token && userEmail) {
            fetchCart(token, userEmail);
        }
    }, [token, userEmail, fetchCart]);

    const handlePayment = async () => {
        if (!stripe || !elements) return;

        setLoading(true);
        setError(null);

        try {
            //  Client Secret für Zahlung abrufen
            const clientSecret = await fetchClientSecret();

            // Stripe-Zahlung ausführen
            const { paymentIntent, error } = await stripe.confirmCardPayment(clientSecret, {

                payment_method: {
                    card: elements.getElement(CardElement)!,
                },
            });

            //  Fehlerhandling für Stripe-Zahlung
            if (error) {
                setError(error.message || "Zahlung fehlgeschlagen.");
                setLoading(false);
                return;
            }

            //  Falls Zahlung erfolgreich, Bestellung speichern
            if (paymentIntent?.id) {
                await checkout(token!, userEmail!, paymentIntent.id, paymentMethod, shippingAddress);
                clearCart(token!, userEmail!);
                navigate("/profile"); //  Weiterleitung zur Bestellübersicht
            } else {
                setError("Zahlung fehlgeschlagen.");
            }

            // eslint-disable-next-line @typescript-eslint/no-unused-vars
        } catch (err) {
            setError("Ein Fehler ist aufgetreten.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold">🛒 Checkout</h2>

            <div className="mt-6 border p-4">
                <h3 className="font-semibold"> Lieferadresse</h3>
                <input
                    type="text"
                    placeholder="Straße"
                    value={shippingAddress.street}
                    onChange={(e) =>
                        setShippingAddress({ ...shippingAddress, street: e.target.value })}
                    className="w-full border p-2 mt-2"
                />
                <input
                    type="text"
                    placeholder="Stadt"
                    value={shippingAddress.city}
                    onChange={(e) =>
                        setShippingAddress({ ...shippingAddress, city: e.target.value })}
                    className="w-full border p-2 mt-2"
                />
                <input
                    type="text"
                    placeholder="PLZ"
                    value={shippingAddress.postalCode}
                    onChange={(e) =>
                        setShippingAddress({ ...shippingAddress, postalCode: e.target.value })}
                    className="w-full border p-2 mt-2"
                />
                <input
                    type="text"
                    placeholder="Land"
                    value={shippingAddress.country}
                    onChange={(e) =>
                        setShippingAddress({ ...shippingAddress, country: e.target.value })}
                    className="w-full border p-2 mt-2"
                />
            </div>

            <div className="mt-6 border p-4">
                <h3 className="font-semibold">💳 Zahlungsmethode</h3>
                <select
                    value={paymentMethod}
                    onChange={(e) =>
                        setPaymentMethod(e.target.value)}
                    className="w-full border p-2 mt-2"
                >
                    <option value="CREDIT_CARD">💳 Kreditkarte</option>
                    <option value="PAYPAL">🅿️ PayPal</option>
                    <option value="KLARNA">🔄 Klarna</option>
                    <option value="SEPA">🏦 SEPA-Lastschrift</option>
                </select>
            </div>

            <div className="mt-6 border p-4">
                <h3 className="font-semibold">💳 Kartenzahlung</h3>
                <CardElement className="border p-2" />
            </div>

            <div className="mt-6 flex justify-between items-center">
                <h3 className="text-xl font-bold">Gesamt: {totalPrice.toFixed(2)} €</h3>
                <button
                    onClick={handlePayment}
                    className="p-2 bg-green-500 text-white rounded"
                    disabled={loading || !stripe}
                >
                    {loading ? "Zahlung wird verarbeitet..." : "🛍 Jetzt kaufen"}
                </button>
            </div>

            {error && <p className="text-red-500 mt-4">{error}</p>}
        </div>
    );
}
